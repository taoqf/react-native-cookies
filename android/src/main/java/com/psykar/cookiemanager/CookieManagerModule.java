package com.psykar.cookiemanager;

import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CookieManagerModule extends ReactContextBaseJavaModule {

    private static final String COOKIE_HEADER = "Cookie";
    private static final String VERSION_ZERO_HEADER = "Set-cookie";

    private static final String COOKIE_DOMAIN_PROPERTY = "Domain";
    private static final String COOKIE_PATH_PROPERTY = "Path";
    private static final String COOKIE_EXPIRES_PROPERTY = "Expires";

    private static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private ForwardingCookieHandler cookieHandler;

    public CookieManagerModule(ReactApplicationContext context) {
        super(context);
        this.cookieHandler = new ForwardingCookieHandler(context);
    }

    public String getName() {
        return "RNCookieManagerAndroid";
    }

    @ReactMethod
    public void set(ReadableMap cookie, boolean useWebKit, final Promise promise) throws Exception {
        try {
            String cookieName = this.confirmCookieProperty( "name", cookie, true );
            String cookieValue = this.confirmCookieProperty( "value", cookie, true );
            String cookieOrigin = this.confirmCookieProperty( "origin", cookie, true );
            String cookieDomain = this.confirmCookieProperty( "domain", cookie, false );
            String cookiePath = this.confirmCookieProperty( "path", cookie, false );
            String cookieVersion = this.confirmCookieProperty( "version", cookie, false );
            String cookieExpiration = this.confirmCookieProperty( "expiration", cookie, false );

            String cookieString = cookieName + '=' + cookieValue + ';';

            if( !cookieDomain.isEmpty() ) {
                cookieString += ' ' + COOKIE_DOMAIN_PROPERTY + '=' + cookieDomain + ';';
            }
            if( !cookiePath.isEmpty() ) {
                cookieString += ' ' + COOKIE_PATH_PROPERTY + '=' + cookiePath + ';';
            }
            if( !cookieExpiration.isEmpty() ) {
                cookieString += ' ' + COOKIE_EXPIRES_PROPERTY + '=' + this.convertToUTC( cookieExpiration ) + ';';
            }

            this.setCookie( cookieOrigin, cookieString );
            promise.resolve( true );
        } catch( Exception e ) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setFromResponse(String url, String value, final Promise promise) throws URISyntaxException, IOException {
        Map headers = new HashMap<String, List<String>>();
        // Pretend this is a header
        headers.put("Set-Cookie", Collections.singletonList(value));
        URI uri = new URI(url);
        try {
            this.cookieHandler.put(uri, headers);
            promise.resolve(true);
        } catch (IOException e) {
            promise.resolve(false);
        }
    }

    @ReactMethod
    public void getFromResponse(String url, Promise promise) throws URISyntaxException, IOException {
        promise.resolve(url);
    }

    @ReactMethod
    public void getAll(boolean useWebKit, Promise promise) throws Exception {
        throw new Exception("Cannot get all cookies on android, try getCookieHeader(url)");
    }

    @ReactMethod
    public void get(String url, boolean useWebKit, Promise promise) throws URISyntaxException, IOException {
        URI uri = new URI(url);

        Map<String, List<String>> cookieMap = this.cookieHandler.get(uri, new HashMap());
        // If only the variables were public
        List<String> cookieList = cookieMap.get("Cookie");
        WritableMap map = Arguments.createMap();
        if (cookieList != null) {
            String[] cookies = cookieList.get(0).split(";");
            for (int i = 0; i < cookies.length; i++) {
                String[] cookie = cookies[i].split("=", 2);
                if (cookie.length > 1) {
                    map.putString(cookie[0].trim(), cookie[1]);
                }
            }
        }
        promise.resolve(map);
    }

    @ReactMethod
    public void clearAll(boolean useWebKit, final Promise promise) {
        this.cookieHandler.clearCookies(new Callback() {
            public void invoke(Object... args) {
                promise.resolve(null);
            }
        });
    }

    private void setCookie( String url, String value ) throws URISyntaxException, IOException {
        URI uri = new URI(url);
        Map<String, List<String>> cookieMap = new HashMap<>();
        cookieMap.put( VERSION_ZERO_HEADER, Collections.singletonList(value) );
        this.cookieHandler.put( uri, cookieMap );
    }

    private String confirmCookieProperty( String propName, ReadableMap cookieObj, Boolean isNecessary ) throws Exception {
        String propValue = cookieObj.hasKey( propName ) ? cookieObj.getString( propName ) : "";

        if( propValue != null && !propValue.isEmpty() ) {
            return propValue;
        } else {
            if( isNecessary ) {
                throw new Exception( propName + " property of cookie obj must have a value" );
            } else {
                return "";
            }
        }
    }

	private String convertToUTC(String timestamp) throws ParseException, Exception {
		SimpleDateFormat parser = new SimpleDateFormat( ISO_DATE_FORMAT_PATTERN );
		Date parsedDate = parser.parse(timestamp);
		SimpleDateFormat utcFormatter = new SimpleDateFormat( "E, dd MMM YYYY HH:mm:ss" );
		return utcFormatter.format(parsedDate) + " GMT";
   }
}
