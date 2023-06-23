package fe.uribuilder

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun URI.urlWithoutParamsAndHash(): URI {
    var newUrl = toString()
    if (rawQuery?.isNotEmpty() == true) {
        newUrl = newUrl.replace("?$rawQuery", "")
    }

    if (rawFragment?.isNotEmpty() == true) {
        newUrl = newUrl.replace("#$rawFragment", "")
    }

    return URI(newUrl)
}

fun parseUri(uriString: String): URI {
    var uri = uriString

    val firstHashIndex = uri.indexOf("#")
    if (firstHashIndex > -1 && uri.indexOf("#", firstHashIndex + 1) > -1) {
        uri = uri.substring(0, firstHashIndex) + "#" + URLEncoder.encode(
            uri.substring(firstHashIndex + 1),
            StandardCharsets.UTF_8
        )
    }

    return URI(uri)
}
