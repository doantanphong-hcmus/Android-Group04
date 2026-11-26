package com.tangkinhcac.app.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BookReaderRepositoryTest {
    @Test
    fun `parses real spine order and preserves verse line breaks`() {
        val epub = File.createTempFile("reader-test", ".epub")
        try {
            ZipOutputStream(epub.outputStream()).use { zip ->
                fun entry(
                    name: String,
                    content: String,
                ) {
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                entry(
                    "META-INF/container.xml",
                    """<container xmlns="urn:oasis:names:tc:opendocument:xmlns:container">""" +
                        """<rootfiles><rootfile full-path="OEBPS/content.opf"/></rootfiles></container>""",
                )
                entry(
                    "OEBPS/content.opf",
                    """<opf:package xmlns:opf="http://www.idpf.org/2007/opf"><opf:manifest>""" +
                        """<opf:item id="second" href="two.xhtml"/>""" +
                        """<opf:item id="first" href="one.xhtml"/></opf:manifest>""" +
                        """<opf:spine><opf:itemref idref="first"/><opf:itemref idref="second"/>""" +
                        """</opf:spine></opf:package>""",
                )
                entry(
                    "OEBPS/one.xhtml",
                    """<!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml"><body>""" +
                        """<h1>Mở đầu</h1><p>Trăm năm trong cõi người ta,<br/>""" +
                        """Chữ tài chữ mệnh khéo là ghét nhau.</p></body></html>""",
                )
                entry("OEBPS/two.xhtml", "<html><body><h1>Tiếp theo</h1><p>Đoạn thứ hai.</p></body></html>")
            }

            val chapters = BookReaderRepository.parseEpub(epub)
            assertEquals(listOf("Mở đầu", "Tiếp theo"), chapters.map { it.title })
            assertTrue(chapters.first().text.contains("ta,\nChữ tài"))
            assertTrue(chapters.first().text.startsWith("Trăm năm"))
        } finally {
            epub.delete()
        }
    }
}
