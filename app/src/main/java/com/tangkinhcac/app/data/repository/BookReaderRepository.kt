package com.tangkinhcac.app.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

data class ReaderChapter(
    val title: String,
    val text: String,
)

enum class ReaderFailure {
    SIGN_IN,
    UNAVAILABLE,
    ACCESS_DENIED,
    NETWORK,
    INVALID_FILE,
    SERVER,
}

class ReaderLoadException(
    val reason: ReaderFailure,
    cause: Throwable? = null,
) : IOException(reason.name, cause)

class BookReaderRepository(
    context: Context,
    retrofit: Retrofit,
    private val mediaClient: OkHttpClient,
) {
    private val api = retrofit.create(EpubApi::class.java)
    private val directory = File(context.filesDir, "epubs")
    private val preferences = context.getSharedPreferences("reader_cache", Context.MODE_PRIVATE)

    suspend fun load(bookSlug: String): List<ReaderChapter> =
        withContext(Dispatchers.IO) {
            require(bookSlug.matches(Regex("[a-z0-9-]+")))
            directory.mkdirs()
            val cached = File(directory, "$bookSlug.epub")
            val response =
                try {
                    api.descriptor(bookSlug).execute()
                } catch (error: IOException) {
                    // A previously verified edition remains readable without a network connection.
                    if (cached.isFile && preferences.contains(bookSlug)) {
                        return@withContext try {
                            parseEpub(cached)
                        } catch (parseError: Exception) {
                            throw ReaderLoadException(ReaderFailure.INVALID_FILE, parseError)
                        }
                    }
                    throw ReaderLoadException(ReaderFailure.NETWORK, error)
                }
            if (!response.isSuccessful) {
                val reason =
                    when (response.code()) {
                        401 -> ReaderFailure.SIGN_IN
                        403 -> ReaderFailure.ACCESS_DENIED
                        404 -> ReaderFailure.UNAVAILABLE
                        else -> ReaderFailure.SERVER
                    }
                throw ReaderLoadException(reason)
            }
            val descriptor = response.body()?.data ?: throw ReaderLoadException(ReaderFailure.SERVER)

            val cacheIdentity = "${descriptor.fileId}:${descriptor.version}:${descriptor.checksum}"
            if (!cached.isFile || preferences.getString(bookSlug, null) != cacheIdentity) {
                val temporary = File(directory, "$bookSlug.partial")
                try {
                    val request = Request.Builder().url(descriptor.downloadUrl).build()
                    mediaClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw ReaderLoadException(
                                if (response.code == 403) ReaderFailure.ACCESS_DENIED else ReaderFailure.SERVER,
                            )
                        }
                        val body = response.body ?: throw ReaderLoadException(ReaderFailure.SERVER)
                        val digest = MessageDigest.getInstance("SHA-256")
                        var count = 0L
                        temporary.outputStream().use { output ->
                            body.byteStream().use { input ->
                                val buffer = ByteArray(8192)
                                while (true) {
                                    val size = input.read(buffer)
                                    if (size < 0) break
                                    digest.update(buffer, 0, size)
                                    output.write(buffer, 0, size)
                                    count += size
                                    if (count > descriptor.size) {
                                        throw ReaderLoadException(
                                            ReaderFailure.INVALID_FILE,
                                            IOException("Downloaded EPUB exceeds descriptor size"),
                                        )
                                    }
                                }
                            }
                        }
                        val checksum = digest.digest().joinToString("") { "%02x".format(it) }
                        if (count != descriptor.size) {
                            throw ReaderLoadException(
                                ReaderFailure.INVALID_FILE,
                                IOException("Downloaded EPUB size differs from descriptor"),
                            )
                        }
                        if (!checksum.equals(descriptor.checksum, ignoreCase = true)) {
                            throw ReaderLoadException(
                                ReaderFailure.INVALID_FILE,
                                IOException("Downloaded EPUB SHA-256 differs from descriptor"),
                            )
                        }
                    }
                    val chapters =
                        try {
                            parseEpub(temporary)
                        } catch (error: Exception) {
                            throw ReaderLoadException(ReaderFailure.INVALID_FILE, error)
                        }
                    Files.move(temporary.toPath(), cached.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    preferences.edit(commit = true) { putString(bookSlug, cacheIdentity) }
                    return@withContext chapters
                } finally {
                    temporary.delete()
                }
            }
            try {
                parseEpub(cached)
            } catch (error: Exception) {
                throw ReaderLoadException(ReaderFailure.INVALID_FILE, error)
            }
        }

    companion object {
        internal fun parseEpub(file: File): List<ReaderChapter> =
            ZipFile(file).use { zip ->
                val container = xml(zip, "META-INF/container.xml")
                val packagePath =
                    container
                        .getElementsByTagNameNS("*", "rootfile")
                        .item(0)
                        ?.attributes
                        ?.getNamedItem("full-path")
                        ?.nodeValue ?: throw IOException("Missing EPUB package")
                val packageDocument = xml(zip, packagePath)
                val basePath = packagePath.substringBeforeLast('/', "")
                val manifest = packageDocument.getElementsByTagNameNS("*", "item")
                val items =
                    (0 until manifest.length).associate { index ->
                        val item = manifest.item(index) as Element
                        item.getAttribute("id") to item.getAttribute("href")
                    }
                val spine = packageDocument.getElementsByTagNameNS("*", "itemref")
                val chapters =
                    (0 until spine.length).mapNotNull { index ->
                        val id = (spine.item(index) as Element).getAttribute("idref")
                        val href = items[id]?.substringBefore('#') ?: return@mapNotNull null
                        val path = if (basePath.isEmpty()) href else "$basePath/$href"
                        val document = xml(zip, path)
                        val body = document.getElementsByTagNameNS("*", "body").item(0) ?: return@mapNotNull null
                        val title = firstHeading(body)?.trim().orEmpty().ifEmpty { "Phần ${index + 1}" }
                        val text = collectText(body).trim().removePrefix(title).trimStart()
                        if (text.isBlank()) null else ReaderChapter(title, text)
                    }
                if (chapters.isEmpty()) throw IOException("EPUB has no readable chapters")
                chapters
            }

        private fun xml(
            zip: ZipFile,
            path: String,
        ) = (zip.getEntry(path) ?: throw IOException("Missing EPUB entry: $path")).let { entry ->
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            factory.isExpandEntityReferences = false
            // Android's XML implementation does not support every JAXP feature (including
            // FEATURE_SECURE_PROCESSING); the resolver still blocks external resources.
            listOf(
                "http://xml.org/sax/features/external-general-entities",
                "http://xml.org/sax/features/external-parameter-entities",
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
            ).forEach { feature -> runCatching { factory.setFeature(feature, false) } }
            val builder = factory.newDocumentBuilder()
            builder.setEntityResolver { _, _ -> InputSource(StringReader("")) }
            zip.getInputStream(entry).use { builder.parse(it) }
        }

        private fun firstHeading(node: Node): String? {
            if (node.nodeName.substringAfter(':').lowercase() in setOf("h1", "h2", "h3")) return node.textContent
            for (index in 0 until node.childNodes.length) {
                firstHeading(node.childNodes.item(index))?.let { return it }
            }
            return null
        }

        private fun collectText(root: Node): String {
            val result = StringBuilder()

            fun visit(node: Node) {
                val tag = node.nodeName.substringAfter(':').lowercase()
                if (tag in setOf("script", "style", "nav")) return
                if (node.nodeType == Node.TEXT_NODE) {
                    result.append(node.nodeValue.replace("\r\n", "\n").replace(Regex("[ \\t]+"), " "))
                    return
                }
                if (tag == "br") result.append('\n')
                for (index in 0 until node.childNodes.length) visit(node.childNodes.item(index))
                if (tag in setOf("p", "div", "section", "li", "h1", "h2", "h3")) result.append("\n\n")
            }
            visit(root)
            return result.toString().replace(Regex(" *\\n *"), "\n").replace(Regex("\n{3,}"), "\n\n")
        }
    }
}

private interface EpubApi {
    @GET("books/{book}/epub")
    fun descriptor(
        @Path("book") book: String,
    ): Call<EpubEnvelope>
}

private data class EpubEnvelope(
    val data: EpubDescriptor,
)

private data class EpubDescriptor(
    @SerializedName("file_id") val fileId: Long,
    val version: Int,
    val size: Long,
    @SerializedName("checksum_sha256") val checksum: String,
    @SerializedName("download_url") val downloadUrl: String,
)
