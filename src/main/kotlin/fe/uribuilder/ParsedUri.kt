package fe.uribuilder

import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.net.*
import org.apache.hc.core5.util.TextUtils
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.Charset
import java.util.*

data class ParsedUri(
    val scheme: String?,
    val encodedSchemeSpecificPart: String?,
    val encodedAuthority: String?,
    val uri: URI,
    val host: String?,
    val port: Int,
    val encodedUserInfo: String?,
    val userInfo: String?,
    val encodedPath: String?,
    val pathSegments: List<String>,
    val pathRootless: Boolean,
    val encodedQuery: String?,
    val queryParams: List<NameValuePair>,
    val encodedFragment: String?,
    val fragment: String?,
    val charset: Charset,
) {
    val fragments by lazy {
        fragment?.split("&")?.asSequence()?.map { it.split("=") }
            ?.associateTo(LinkedHashMap()) { if (it.size == 2) it[0] to it[1] else it[0] to null } ?: mutableMapOf()
    }

    @Throws(URISyntaxException::class)
    fun build(): URI {
        return URI(buildString())
    }

    private fun formatPath(
        buf: StringBuilder,
        segments: Iterable<String?>,
        rootless: Boolean,
        charset: Charset?
    ) {
        for ((i, segment) in segments.withIndex()) {
            if (i > 0 || !rootless) {
                buf.append(UriParser.PATH_SEPARATOR)
            }

            PercentCodec.encode(buf, segment, charset)
        }
    }

    private fun formatQuery(
        buf: StringBuilder, params: Iterable<NameValuePair>, charset: Charset?,
        blankAsPlus: Boolean
    ) {
        for ((i, parameter) in params.withIndex()) {
            if (i > 0) {
                buf.append(UriParser.QUERY_PARAM_SEPARATOR)
            }

            PercentCodec.encode(buf, parameter.name, charset, blankAsPlus)
            if (parameter.value != null) {
                buf.append(UriParser.PARAM_VALUE_SEPARATOR)
                PercentCodec.encode(buf, parameter.value, charset, blankAsPlus)
            }
        }
    }

    private fun buildString(): String {
        return buildString {
            if (scheme != null) {
                append(scheme).append(':')
            }

            if (encodedSchemeSpecificPart != null) {
                append(encodedSchemeSpecificPart)
            } else {
                val authoritySpecified: Boolean = if (encodedAuthority != null) {
                    append("//").append(encodedAuthority)
                    true
                } else if (host != null) {
                    append("//")
                    if (encodedUserInfo != null) {
                        append(encodedUserInfo).append("@")
                    } else if (userInfo != null) {
                        val idx = userInfo.indexOf(':')
                        if (idx != -1) {
                            PercentCodec.encode(this, userInfo.substring(0, idx), charset)
                            append(':')
                            PercentCodec.encode(this, userInfo.substring(idx + 1), charset)
                        } else {
                            PercentCodec.encode(this, userInfo, charset)
                        }
                        append("@")
                    }

                    if (InetAddressUtils.isIPv6Address(host)) {
                        append("[").append(host).append("]")
                    } else {
                        append(PercentCodec.encode(host, charset))
                    }
                    if (port >= 0) {
                        append(":").append(port)
                    }

                    true
                } else false

                if (encodedPath != null) {
                    if (authoritySpecified && !TextUtils.isEmpty(encodedPath) && !encodedPath.startsWith("/")) {
                        append('/')
                    }

                    append(encodedPath)
                } else if (pathSegments.isNotEmpty()) {
                    formatPath(
                        this,
                        pathSegments, !authoritySpecified && pathRootless, charset
                    )
                }

                if (encodedQuery != null) {
                    append("?").append(encodedQuery)
                } else if (queryParams.isNotEmpty()) {
                    append("?")
                    formatQuery(this, queryParams, charset, false)
                }
            }

            if (encodedFragment != null) {
                append("#").append(encodedFragment)
            } else if (fragment != null) {
                append("#")
                PercentCodec.encode(this, fragment, charset)
            }
        }
    }

    fun isPathEmpty(): Boolean {
        return pathSegments.isEmpty() && encodedPath.isNullOrEmpty()
    }

    fun getPath(): String {
        return buildString {
            for (segment in pathSegments) {
                append('/').append(segment)
            }
        }
    }

    fun isQueryEmpty(): Boolean {
        return queryParams.isEmpty() && encodedQuery == null
    }

    fun getFirstQueryParam(name: String): NameValuePair? {
        return queryParams.firstOrNull { name == it.name }
    }

    override fun toString(): String {
        return buildString()
    }
}
