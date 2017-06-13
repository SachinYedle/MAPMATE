package com.riktam.mapmate.locationsharing.utils;

/**
 * Created by admin1 on 2/12/16.
 */

public class Constants {
    public static final String DB_NAME = "locationsharing.db";

    public static final int LOCATION_INTERVAL = 1000 * 60 * 5;//5 min
    public static final int START_SHARING = 1;
    public static final int STOP_SHARING = 0;

    public static String CLIENT_ID = "1064015057946-eel9c0la7rv2g1puvp0g6t9cl42dr1rq.apps.googleusercontent.com";
    // Use your own client id

    public static String CLIENT_SECRET = "wcWmMA-7NerLTHThze4NurYA";
    // Use your own client secret

    public static String REDIRECT_URI = "http://localhost";
    public static String GRANT_TYPE = "authorization_code";
    public static String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    public static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    public static String OAUTH_SCOPE = "https://www.googleapis.com/auth/contacts.readonly";

    public static final String CONTACTS_URL = "https://www.google.com/m8/feeds/contacts/default/full?max-results=3000&popular=true";
    public static final int MAX_NB_CONTACTS = 1000;
    public static final String APP = "Map Mate";
}
