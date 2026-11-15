package com.moonflix.adblock

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlocker {

    private val AD_DOMAINS = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "adnxs.com",
        "adsrvr.org",
        "adform.net",
        "advertising.com",
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "propu.sh",
        "juicyads.com",
        "exoclick.com",
        "exosrv.com",
        "trafficjunky.net",
        "adsterra.com",
        "highperformanceformat.com",
        "topcreativeformat.com",
        "profitabledisplaynetwork.com",
        "hilltopads.net",
        "clickadu.com",
        "admaven.com",
        "mgid.com",
        "taboola.com",
        "outbrain.com",
        "revcontent.com",
        "criteo.com",
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "casalemedia.com",
        "smartadserver.com",
        "bidswitch.net",
        "sharethrough.com",
        "33across.com",
        "yandex.ru",
        "mc.yandex.ru",
        "tsyndicate.com",
        "realsrv.com",
        "a-ads.com",
        "coinzilla.com",
        "bitmedia.io",
        "monetag.com",
        "alwingulla.com",
        "onclkds.com",
        "whos.amung.us",
        "histats.com",
        "statcounter.com"
    )

    private val AD_PATH_KEYWORDS = listOf(
        "/ads/", "/ad/", "/advert", "/banner", "/popunder", "/popup",
        "ads.js", "adserver", "pagead", "show_ads", "vast.xml", "vmap"
    )

    private val ALLOWED_HOSTS = hashSetOf(
        "moonflix.website",
        "www.moonflix.website",
        "moonflix.site",
        "www.moonflix.site"
    )

    fun isAdUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            return false
        }
        val host = uri.host?.lowercase() ?: return false

        // Check exact domain or subdomain match against known ad domains
        for (adDomain in AD_DOMAINS) {
            if (host == adDomain || host.endsWith(".$adDomain")) {
                return true
            }
        }

        // Check common ad path patterns
        val lowerUrl = url.lowercase()
        for (keyword in AD_PATH_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                return true
            }
        }

        return false
    }

    fun isAllowedNavigation(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        if (isAdUrl(url)) return false
        val host = Uri.parse(url).host?.lowercase() ?: return false
        return ALLOWED_HOSTS.any { host == it || host.endsWith(".$it") }
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    const val COSMETIC_ADBLOCK_JS = """
        (function() {
            // Block window.open popups
            window.open = function() { return null; };

            // Remove common ad elements and overlays
            const selectors = [
                'iframe[src*="ads"]',
                'iframe[src*="doubleclick"]',
                'div[class*="ad-"]',
                'div[class*="ads-"]',
                'div[id*="ad-"]',
                'div[id*="ads-"]',
                '.advertisement',
                '.ad-banner',
                '.popup-overlay',
                'ins.adsbygoogle'
            ];

            function cleanAds() {
                selectors.forEach(sel => {
                    document.querySelectorAll(sel).forEach(el => el.remove());
                });
            }

            cleanAds();
            const observer = new MutationObserver(cleanAds);
            if (document.body) {
                observer.observe(document.body, { childList: true, subtree: true });
            }
        })();
    """
}
