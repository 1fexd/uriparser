package fe.uribuilder

import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.apache.hc.core5.http.message.ParserCursor
import org.apache.hc.core5.net.InetAddressUtils
import org.apache.hc.core5.net.PercentCodec
import org.apache.hc.core5.net.URIAuthority
import org.apache.hc.core5.util.Tokenizer
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

object UriParser {
    const val QUERY_PARAM_SEPARATOR = '&'
    const val PARAM_VALUE_SEPARATOR = '='
    const val PATH_SEPARATOR = '/'

    private val QUERY_PARAM_SEPARATORS: BitSet = BitSet(256).apply {
        set(QUERY_PARAM_SEPARATOR.code)
        set(PARAM_VALUE_SEPARATOR.code)
    }

    private val QUERY_VALUE_SEPARATORS: BitSet = BitSet(256).apply {
        set(PARAM_VALUE_SEPARATOR.code)
    }

    private val PATH_SEPARATORS: BitSet = BitSet(256).apply { set(QUERY_PARAM_SEPARATOR.code) }

    private fun parsePath(s: CharSequence, charset: Charset): List<String> {
        return splitPath(s).map {
            PercentCodec.decode(it, charset)
        }
    }

    private fun splitPath(s: CharSequence?): List<String> {
        if (s == null) {
            return emptyList()
        }

        val cursor = ParserCursor(0, s.length)
        // Skip leading separator
        if (cursor.atEnd()) {
            return ArrayList(0)
        }

        if (PATH_SEPARATORS[s[cursor.pos].code]) {
            cursor.updatePos(cursor.pos + 1)
        }

        val list = mutableListOf<String>()
        val buf = StringBuilder()
        while (true) {
            if (cursor.atEnd()) {
                list.add(buf.toString())
                break
            }
            val current = s[cursor.pos]

            if (PATH_SEPARATORS[current.code]) {
                list.add(buf.toString())
                buf.setLength(0)
            } else {
                buf.append(current)
            }

            cursor.updatePos(cursor.pos + 1)
        }

        return list
    }

    private fun parseQuery(s: CharSequence?, charset: Charset?, plusAsBlank: Boolean): List<NameValuePair> {
        if (s == null) {
            return emptyList()
        }

        val tokenParser: Tokenizer = Tokenizer.INSTANCE
        val cursor = ParserCursor(0, s.length)
        val list = mutableListOf<NameValuePair>()

        while (!cursor.atEnd()) {
            val name = tokenParser.parseToken(s, cursor, QUERY_PARAM_SEPARATORS)
            var value: String? = null

            if (!cursor.atEnd()) {
                val delim = s[cursor.pos].code
                cursor.updatePos(cursor.pos + 1)
                if (delim == PARAM_VALUE_SEPARATOR.code) {
                    value = tokenParser.parseToken(s, cursor, QUERY_VALUE_SEPARATORS)
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.pos + 1)
                    }
                }
            }

            if (name.isNotEmpty()) {
                list.add(
                    BasicNameValuePair(
                        PercentCodec.decode(name, charset, plusAsBlank),
                        PercentCodec.decode(value, charset, plusAsBlank)
                    )
                )
            }
        }
        return list
    }

    fun parseUri(uri: URI, charset: Charset = StandardCharsets.UTF_8): ParsedUri {
        val scheme = uri.scheme
        val encodedSchemeSpecificPart = uri.rawSchemeSpecificPart
        val encodedAuthority = uri.rawAuthority
        val uriHost: String? = uri.host
        // URI.getHost incorrectly returns bracketed (encoded) IPv6 values. Brackets are an
        // encoding detail of the URI and not part of the host string.
        var host = if (uriHost != null && InetAddressUtils.isIPv6URLBracketedAddress(uriHost)) uriHost.substring(
            1,
            uriHost.length - 1
        ) else uriHost
        var port = uri.port
        var encodedUserInfo = uri.rawUserInfo
        var userInfo = uri.userInfo
        if (encodedAuthority != null && host == null) {
            try {
                val uriAuthority = URIAuthority.parse(encodedAuthority)
                encodedUserInfo = uriAuthority.userInfo
                userInfo = PercentCodec.decode(uriAuthority.userInfo, charset)
                host = PercentCodec.decode(uriAuthority.hostName, charset)
                port = uriAuthority.port
            } catch (ignore: URISyntaxException) {
            }
        }

        val encodedPath = uri.rawPath
        val pathSegments = parsePath(uri.rawPath, charset)
        val pathRootless = uri.rawPath == null || !uri.rawPath.startsWith("/")
        val encodedQuery = uri.rawQuery

        val queryParams = parseQuery(uri.rawQuery, charset, false)

        val encodedFragment = uri.rawFragment
        val fragment = uri.fragment

        return ParsedUri(
            scheme,
            encodedSchemeSpecificPart,
            encodedAuthority,
            uri,
            host,
            port,
            encodedUserInfo,
            userInfo,
            encodedPath,
            pathSegments,
            pathRootless,
            encodedQuery,
            queryParams,
            encodedFragment,
            fragment,
            charset
        )
    }
}
