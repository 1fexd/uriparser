package fe.uribuilder

import org.apache.hc.core5.net.URIBuilder
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun parseUri(inputUrl: String): Pair<URI, URIBuilder> {
    var url = inputUrl

    val firstHashIndex = url.indexOf("#")
    if (firstHashIndex > -1 && url.indexOf("#", firstHashIndex + 1) > -1) {
        url = url.substring(0, firstHashIndex) + "#" + URLEncoder.encode(
            url.substring(firstHashIndex + 1),
            StandardCharsets.UTF_8
        )
    }

    val uriObj = URI(url)
    return uriObj to URIBuilder(uriObj)
}
