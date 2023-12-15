package fe.uribuilder

import org.apache.hc.core5.net.PercentCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UriParserTest {
    @Test
    fun testSuccess() {
        val result = UriParser.parseUri("https://google.com/henlo/fren")
        assertTrue(result is UriParseResult.ParsedUri)
        assertEquals(result.host, "google.com")
        assertEquals(result.host, "/henlo/fren")
    }

    @Test
    fun testFailure() {
        val result =
            UriParser.parseUri("https://nam12.safelinks.protection.outlook.com/?url=https%3A%2F%2Fwww.washingtonpost.com%2Ftechnology%2F2023%2F12%2F13%2Ftesla-recall-autopilot%2F%3Fmc_cid%stop_it%26mc_eid%aint_no_tracking_here&data=05%7C02%7C%sorry_dawg%not_retarded%7C1%7C0%wide_peepo%7CUnknown%dont_fucking_track_me%3D%7C3000%7C%7C%7C&sdata=e3q5md214A8NGdW%lule%3D&reserved=0")
        assertTrue(result is UriParseResult.ParserFailure)
    }

    @Test
    fun test() {
        println(PercentCodec.decode("foo-ä-€.html", Charsets.UTF_8))
    }


    @Test
    fun testRfc5987EncodingDecoding() {
        val params = mapOf(
            "foo-ä-€.html" to "foo-%C3%A4-%E2%82%AC.html",
            "世界ーファイル 2.jpg" to "%E4%B8%96%E7%95%8C%E3%83%BC%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%202.jpg",
            "foo.jpg" to "foo.jpg",
            "simple" to "simple",  // Unreserved characters
            "reserved/chars?" to "reserved%2Fchars%3F",  // Reserved characters
            "" to "",  // Empty string
            "space test" to "space%20test",  // String with space
            "ümlaut" to "%C3%BCmlaut" // Non-ASCII characters
        )

        params.forEach { (input, expected) ->
            assertEquals(expected, PercentCodec.encode(input, Charsets.UTF_8))
            assertEquals(input, PercentCodec.decode(expected, Charsets.UTF_8))
        }
    }

    @Test
    fun verifyRfc5987EncodingandDecoding() {
        val s = "!\"$£%^&*()_-+={[}]:@~;'#,./<>?\\|✓éèæðŃœ"
        assertEquals(PercentCodec.decode(PercentCodec.encode(s, Charsets.UTF_8), Charsets.UTF_8), s)
    }
//
//    @Test
//    fun verifyRfc5987EncodingandDecoding() {
//        val s = "!\"$£%^&*()_-+={[}]:@~;'#,./<>?\\|✓éèæðŃœ"
//        assertThat(PercentCodec.RFC5987.decode(PercentCodec.RFC5987.encode(s)), CoreMatchers.equalTo(s))
//    }
}

