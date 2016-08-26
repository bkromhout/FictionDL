package bkromhout.fdl.util;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of OkHttp's {@code CookieJar}. Only keeps cookies per-run.
 * @author Brenden Kromhout
 */
public class CookieMonster implements CookieJar {
    /**
     * Singleton instance.
     */
    private static CookieMonster INSTANCE = null;
    /**
     * Cookies held by the cookie monster.
     */
    private final HashSet<Cookie> cookies;

    /**
     * Get the {@link CookieMonster} instance.
     * @return Instance.
     */
    public static CookieMonster get() {
        if (INSTANCE == null) INSTANCE = new CookieMonster();
        return INSTANCE;
    }

    // C is for Cookie.
    private CookieMonster() {
        this.cookies = new HashSet<>();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        this.cookies.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();

            if (currentCookie.expiresAt() < System.currentTimeMillis())
                it.remove();
            else if (currentCookie.matches(url))
                validCookies.add(currentCookie);
        }

        return validCookies;
    }
}
