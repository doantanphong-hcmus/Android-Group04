package com.tangkinhcac.app.feature.library

import com.tangkinhcac.app.R
import java.text.Normalizer
import java.util.Locale

internal data class LibraryBook(
    val id: String,
    val title: String,
    val originalTitle: String,
    val alternativeTitles: List<String>,
    val author: String,
    val authorOriginalName: String,
    val country: String,
    val originalLanguage: String,
    val publicationPeriod: String,
    val genres: List<String>,
    val structure: String,
    val historicalSetting: String,
    val synopsis: String,
    val keywords: List<String>,
    val coverResId: Int,
    val rating: String,
    val reviewCount: String,
    val isVip: Boolean,
)

internal object LibrarySearchCatalog {
    val books =
        listOf(
            LibraryBook(
                id = "tay-du-ky",
                title = "Tây Du Ký",
                originalTitle = "西遊記",
                alternativeTitles = listOf("Journey to the West", "Xiyou Ji", "Tây Du Kí"),
                author = "Ngô Thừa Ân",
                authorOriginalName = "吳承恩 (Wu Cheng'en)",
                country = "Trung Quốc",
                originalLanguage = "Tiếng Trung Quốc",
                publicationPeriod = "Cuối thế kỷ XVI, thời Minh; bản in sớm nhất còn biết từ năm 1592",
                genres = listOf("Tiểu thuyết chương hồi", "Kỳ ảo", "Phiêu lưu", "Thần thoại"),
                structure = "100 hồi",
                historicalSetting =
                    "Hành trình thỉnh kinh từ nhà Đường sang Tây Trúc, đan xen thế giới thần, Phật và yêu quái.",
                synopsis =
                    "Đường Tăng cùng Tôn Ngộ Không, Trư Bát Giới và Sa Tăng vượt qua nhiều thử thách để đến Tây Trúc " +
                        "thỉnh kinh, trong một câu chuyện kết hợp phiêu lưu, châm biếm và tư tưởng Phật–Đạo.",
                keywords =
                    listOf(
                        "Tôn Ngộ Không",
                        "Đường Tăng",
                        "Trư Bát Giới",
                        "Sa Tăng",
                        "thỉnh kinh",
                        "Tây Trúc",
                        "Phật giáo",
                        "Đạo giáo",
                        "nhà Minh",
                    ),
                coverResId = R.drawable.cover_tay_du_ky,
                rating = "4.8",
                reviewCount = "856",
                isVip = true,
            ),
            LibraryBook(
                id = "kim-ngao-tan-thoai",
                title = "Kim Ngao Tân Thoại",
                originalTitle = "金鰲新話 (금오신화)",
                alternativeTitles = listOf("Geumo Sinhwa", "Kŭmo Sinhwa", "New Tales of Mount Geumo"),
                author = "Kim Thời Tập",
                authorOriginalName = "김시습 (金時習, Kim Si-seup)",
                country = "Hàn Quốc",
                originalLanguage = "Hán văn",
                publicationPeriod = "Thập niên 1460, đầu thời Joseon",
                genres = listOf("Tập truyện truyền kỳ", "Kỳ ảo", "Lãng mạn", "Châm biếm xã hội"),
                structure = "5 truyện còn lưu truyền",
                historicalSetting =
                    "Không gian Triều Tiên thời Joseon cùng những cõi siêu nhiên như âm phủ và long cung.",
                synopsis =
                    "Năm truyện còn lại kể về những con người cô độc gặp gỡ ma quỷ, tiên giới và các cõi khác, qua " +
                        "đó phản ánh tình yêu, mất mát và sự bất mãn trước xã hội đương thời.",
                keywords =
                    listOf(
                        "Manboksa",
                        "Yi Saeng",
                        "Bubyeok Pavilion",
                        "Nam Diêm Phù Châu",
                        "Long Cung",
                        "ma quỷ",
                        "Joseon",
                        "truyện lạ",
                    ),
                coverResId = R.drawable.cover_kim_ngao_tan_thoai,
                rating = "4.7",
                reviewCount = "316",
                isVip = false,
            ),
            LibraryBook(
                id = "truyen-ky-man-luc",
                title = "Truyền Kỳ Mạn Lục",
                originalTitle = "傳奇漫錄",
                alternativeTitles =
                    listOf("Tân biên Truyền kỳ mạn lục", "Casual Records of Transmitted Strange Tales"),
                author = "Nguyễn Dữ",
                authorOriginalName = "阮嶼",
                country = "Việt Nam",
                originalLanguage = "Hán văn",
                publicationPeriod = "Thế kỷ XVI; lời tựa còn lưu truyền được viết năm 1547",
                genres = listOf("Tập truyện truyền kỳ", "Kỳ ảo", "Dã sử", "Châm biếm xã hội"),
                structure = "4 quyển, 20 truyện",
                historicalSetting =
                    "Nhiều địa danh và giai đoạn lịch sử Việt Nam, kết hợp đời sống thực với thế giới thần linh.",
                synopsis =
                    "Hai mươi truyện kỳ lạ khai thác số phận con người, đạo lý và những bất công trong xã hội phong " +
                        "kiến qua các yếu tố ma quỷ, thần tiên và truyền thuyết dân gian.",
                keywords =
                    listOf(
                        "Vũ Nương",
                        "Ngô Tử Văn",
                        "Từ Thức",
                        "Chuyện người con gái Nam Xương",
                        "Chuyện chức phán sự đền Tản Viên",
                        "truyện lạ",
                        "văn học trung đại",
                    ),
                coverResId = R.drawable.cover_truyen_ky_man_luc,
                rating = "4.9",
                reviewCount = "1.1k",
                isVip = false,
            ),
            LibraryBook(
                id = "truyen-kieu",
                title = "Truyện Kiều",
                originalTitle = "斷腸新聲 (Đoạn trường tân thanh)",
                alternativeTitles = listOf("Kim Vân Kiều", "The Tale of Kiều", "The Story of Kiều"),
                author = "Nguyễn Du",
                authorOriginalName = "阮攸",
                country = "Việt Nam",
                originalLanguage = "Tiếng Việt, chữ Nôm",
                publicationPeriod = "Đầu thế kỷ XIX",
                genres = listOf("Truyện thơ Nôm", "Lục bát", "Bi kịch", "Lãng mạn"),
                structure = "3.254 câu thơ lục bát",
                historicalSetting = "Trung Quốc thời Minh, xoay quanh mười lăm năm lưu lạc của Thúy Kiều.",
                synopsis =
                    "Thúy Kiều bán mình cứu cha rồi trải qua mười lăm năm lưu lạc. Tác phẩm đặt tài năng, tình yêu " +
                        "và nhân phẩm của con người trước sức ép của đồng tiền và lễ giáo phong kiến.",
                keywords =
                    listOf(
                        "Thúy Kiều",
                        "Kim Trọng",
                        "Từ Hải",
                        "Hoạn Thư",
                        "Mã Giám Sinh",
                        "bán mình chuộc cha",
                        "tài mệnh tương đố",
                        "chữ Nôm",
                    ),
                coverResId = R.drawable.cover_truyen_kieu,
                rating = "4.9",
                reviewCount = "1.2k",
                isVip = false,
            ),
            LibraryBook(
                id = "hoang-le-nhat-thong-chi",
                title = "Hoàng Lê Nhất Thống Chí",
                originalTitle = "皇黎一統志",
                alternativeTitles =
                    listOf(
                        "An Nam nhất thống chí",
                        "Lê quý ngoại sử",
                        "Records of the Unification of Imperial Lê",
                    ),
                author = "Ngô Gia Văn Phái",
                authorOriginalName = "吳家文派",
                country = "Việt Nam",
                originalLanguage = "Hán văn",
                publicationPeriod = "Cuối thế kỷ XVIII – đầu thế kỷ XIX",
                genres = listOf("Tiểu thuyết lịch sử", "Tiểu thuyết chương hồi", "Dã sử"),
                structure = "17 hồi: 7 hồi chính biên và 10 hồi tục biên",
                historicalSetting =
                    "Bắc Hà cuối thời Lê–Trịnh, phong trào Tây Sơn và giai đoạn đầu triều Nguyễn.",
                synopsis =
                    "Tác phẩm ghi lại những biến động từ lúc quyền lực họ Trịnh suy sụp, quân Tây Sơn tiến ra Bắc " +
                        "Hà, Quang Trung đại phá quân Thanh đến khi nhà Nguyễn thống nhất đất nước.",
                keywords =
                    listOf(
                        "Ngô Thì Chí",
                        "Ngô Thì Du",
                        "Quang Trung",
                        "Nguyễn Huệ",
                        "Lê Chiêu Thống",
                        "Tây Sơn",
                        "quân Thanh",
                        "Lê Trịnh",
                    ),
                coverResId = R.drawable.cover_hoang_le_nhat_thong_chi,
                rating = "4.8",
                reviewCount = "742",
                isVip = false,
            ),
            LibraryBook(
                id = "tam-quoc-dien-nghia",
                title = "Tam Quốc Diễn Nghĩa",
                originalTitle = "三國演義",
                alternativeTitles =
                    listOf("Tam Quốc chí thông tục diễn nghĩa", "Romance of the Three Kingdoms", "Sanguozhi Yanyi"),
                author = "La Quán Trung",
                authorOriginalName = "羅貫中 (Luo Guanzhong)",
                country = "Trung Quốc",
                originalLanguage = "Tiếng Trung Quốc",
                publicationPeriod =
                    "Thế kỷ XIV; bản 120 hồi phổ biến được Mao Tôn Cương hiệu đính vào thế kỷ XVII",
                genres = listOf("Tiểu thuyết lịch sử", "Tiểu thuyết chương hồi", "Dã sử", "Chiến tranh"),
                structure = "120 hồi trong bản lưu hành phổ biến",
                historicalSetting = "Cuối Đông Hán và thời Tam Quốc, khoảng thế kỷ II–III.",
                synopsis =
                    "Tác phẩm tái hiện quá trình tan rã của nhà Hán và cuộc tranh hùng giữa Ngụy, Thục, Ngô qua " +
                        "chiến tranh, mưu lược chính trị, lòng trung nghĩa và tham vọng thống nhất thiên hạ.",
                keywords =
                    listOf(
                        "Lưu Bị",
                        "Quan Vũ",
                        "Trương Phi",
                        "Tào Tháo",
                        "Gia Cát Lượng",
                        "Xích Bích",
                        "Ngụy Thục Ngô",
                        "kết nghĩa vườn đào",
                    ),
                coverResId = R.drawable.cover_tam_quoc_dien_nghia,
                rating = "4.9",
                reviewCount = "2.8k",
                isVip = true,
            ),
            LibraryBook(
                id = "thuy-hu",
                title = "Thủy Hử",
                originalTitle = "水滸傳",
                alternativeTitles =
                    listOf("Thủy Hử truyện", "Water Margin", "Outlaws of the Marsh", "All Men Are Brothers"),
                author = "Thi Nại Am",
                authorOriginalName = "施耐庵 (Shi Nai'an)",
                country = "Trung Quốc",
                originalLanguage = "Tiếng Trung Quốc",
                publicationPeriod = "Khoảng cuối thời Nguyên – đầu thời Minh",
                genres = listOf("Tiểu thuyết chương hồi", "Anh hùng", "Phiêu lưu", "Dã sử"),
                structure = "Có các hệ bản 70, 100 và 120 hồi; bản cổ đầy đủ sớm nhất gồm 100 hồi",
                historicalSetting = "Cuối thời Bắc Tống, chủ yếu tại vùng đầm Lương Sơn.",
                synopsis =
                    "Một trăm lẻ tám hảo hán vì nhiều nguyên nhân bị đẩy ra ngoài vòng pháp luật rồi tụ nghĩa tại " +
                        "Lương Sơn Bạc, chống quan lại tham nhũng trước khi đứng trước lựa chọn chiêu an.",
                keywords =
                    listOf(
                        "108 hảo hán",
                        "Lương Sơn Bạc",
                        "Tống Giang",
                        "Lỗ Trí Thâm",
                        "Võ Tòng",
                        "Lâm Xung",
                        "chiêu an",
                        "Bắc Tống",
                    ),
                coverResId = R.drawable.cover_thuy_hu,
                rating = "4.7",
                reviewCount = "692",
                isVip = false,
            ),
            LibraryBook(
                id = "hong-cat-dong-truyen",
                title = "Hồng Cát Đồng Truyện",
                originalTitle = "홍길동전 (洪吉童傳)",
                alternativeTitles =
                    listOf("Truyện Hồng Gil-dong", "Hong Gildongjeon", "The Story of Hong Gildong"),
                author = "Hứa Quân",
                authorOriginalName = "허균 (許筠, Heo Gyun)",
                country = "Hàn Quốc",
                originalLanguage = "Tiếng Hàn, chữ Hangul",
                publicationPeriod =
                    "Tương truyền được sáng tác đầu thế kỷ XVII; bản gốc chưa được tìm thấy và tác quyền vẫn còn tranh luận",
                genres = listOf("Tiểu thuyết anh hùng", "Phiêu lưu", "Kỳ ảo", "Châm biếm xã hội"),
                structure = "Lưu truyền qua nhiều dị bản chép tay, khắc in và in chữ rời",
                historicalSetting = "Triều Joseon với chế độ phân biệt con chính thất và con thứ.",
                synopsis =
                    "Hồng Gil-dong, người con thứ không được xã hội thừa nhận, rời gia đình, lãnh đạo Hoạt Bần Đảng " +
                        "giúp người nghèo và cuối cùng xây dựng một vương quốc lý tưởng tại Yuldo.",
                keywords =
                    listOf(
                        "Hong Gil-dong",
                        "Hoạt Bần Đảng",
                        "Hwalbindang",
                        "Yuldo",
                        "con thứ",
                        "phân biệt thân phận",
                        "Robin Hood Hàn Quốc",
                        "Joseon",
                    ),
                coverResId = R.drawable.cover_hong_cat_dong_truyen,
                rating = "4.6",
                reviewCount = "284",
                isVip = true,
            ),
        )

    fun filterBooks(
        country: String? = null,
        isVip: Boolean? = null,
    ): List<LibraryBook> =
        books.filter { book ->
            (country == null || book.country == country) &&
                (isVip == null || book.isVip == isVip)
        }

    fun search(query: String): List<LibraryBook> {
        val normalizedQuery = query.normalizedForSearch()
        if (normalizedQuery.isBlank()) {
            return books
        }

        val identityMatches =
            books.filter { book ->
                book.identityValues().any { it.normalizedForSearch().contains(normalizedQuery) }
            }
        if (identityMatches.isNotEmpty()) {
            return identityMatches
        }

        val queryTokens = normalizedQuery.searchTokens()
        return books.filter { book ->
            val searchableTokens = book.searchableValues().flatMap { it.normalizedForSearch().searchTokens() }
            queryTokens.all { queryToken ->
                searchableTokens.any { searchableToken ->
                    val isPrefixMatch = queryToken.length >= MIN_PREFIX_LENGTH && searchableToken.startsWith(queryToken)
                    searchableToken == queryToken || isPrefixMatch
                }
            }
        }
    }

    private fun LibraryBook.identityValues(): List<String> =
        buildList {
            add(title)
            add(originalTitle)
            addAll(alternativeTitles)
            add(author)
            add(authorOriginalName)
        }

    private fun LibraryBook.searchableValues(): List<String> =
        buildList {
            add(id)
            add(title)
            add(originalTitle)
            addAll(alternativeTitles)
            add(author)
            add(authorOriginalName)
            add(country)
            add(originalLanguage)
            add(publicationPeriod)
            addAll(genres)
            add(structure)
            add(historicalSetting)
            add(synopsis)
            addAll(keywords)
        }

    private fun String.normalizedForSearch(): String =
        Normalizer
            .normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .replace('đ', 'd')

    private fun String.searchTokens(): List<String> =
        replace(NON_ALPHANUMERIC, " ").split(WHITESPACE).filter(String::isNotBlank)

    private const val MIN_PREFIX_LENGTH = 3
    private val COMBINING_MARKS = Regex("\\p{M}+")
    private val NON_ALPHANUMERIC = Regex("[^\\p{L}\\p{N}]+")
    private val WHITESPACE = Regex("\\s+")
}
