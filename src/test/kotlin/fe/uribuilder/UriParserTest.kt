package fe.uribuilder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UriParserTest {
    @Test
    fun testSuccess() {
        val result = UriParser.parseUri("https://google.com/henlo/fren")
        assertTrue(result is UriParseResult.ParsedUri)
        assertEquals(result.host , "google.com")
        assertEquals(result.host , "/henlo/fren")
    }

    @Test
    fun testFailure() {
        val result = UriParser.parseUri("https://nam12.safelinks.protection.outlook.com/?url=https%3A%2F%2Fwww.washingtonpost.com%2Ftechnology%2F2023%2F12%2F13%2Ftesla-recall-autopilot%2F%3Fmc_cid%stop_it%26mc_eid%aint_no_tracking_here&data=05%7C02%7C%sorry_dawg%not_retarded%7C1%7C0%wide_peepo%7CUnknown%dont_fucking_track_me%3D%7C3000%7C%7C%7C&sdata=e3q5md214A8NGdW%lule%3D&reserved=0")
        assertTrue(result is UriParseResult.ParserFailure)
    }
}
