package co.faxapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.preference.PreferenceManager;

import com.parse.ConfigCallback;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.ui.Countries;
import com.phaxio.Phaxio;

import co.faxapp.db.HelperFactory;
import co.faxapp.model.FaxItem;
import co.faxapp.model.FaxToEmail;
import co.faxapp.model.SavedNumber;
import co.faxapp.model.UserProfile;
import co.faxapp.util.Log;

public class App extends Application {
    private static final String TAG = App.class.getName();

    private ColorStateList defColors;
    private ParseConfig config;
    private UserProfile userProfile;
    private static boolean activityVisible;
    private static String userCountry;
    private boolean unlimited = false;
    private boolean isFaxToEmail = false;

    @Override
    public void onCreate() {
        super.onCreate();
        HelperFactory.setHelper(getApplicationContext());
        ParseObject.registerSubclass(UserProfile.class);
        ParseObject.registerSubclass(SavedNumber.class);
        ParseObject.registerSubclass(FaxItem.class);
        ParseObject.registerSubclass(FaxToEmail.class);
        Parse.initialize(this, "pFkelC8FSHI3qUWJeviv5P1ZFCqOzyF2kdOcbh3T", "EDmxM7uoVqGC63kdFze4ZvZwvwwhFbkURVZyb8bl");
        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
        fetchParseConfig();
    }

    @Override
    public void onTerminate() {
        HelperFactory.releaseHelper();
        super.onTerminate();
    }

    public int getDefaultFreePages() {
        return config.getInt("FreePagesForNewUser");
    }

    public void fetchParseConfig() {

        config = ParseConfig.getCurrentConfig();

        // Set the current time, to flag that the operation started and prevent double fetch
        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig parseConfig, ParseException e) {
                if (e == null) {
                    // Yay, retrieved successfully
                    config = parseConfig;
                } else {
                    // Fetch failed, reset the time
                    Log.e(TAG, "Config not loaded, will use current");
                }
            }
        });
    }

    public String getBase64EncodedPublicKey() {
        return config.getString("base64EncodedPublicKey");
    }

    public void setPhaxioKeys() {
        String key;
        String secret;
        if (isTestMode()) {
            key = config.getString("Phaxio_apiKey_TEST");
            secret = config.getString("Phaxio_apiSecret_TEST");
        } else {
            key = config.getString("Phaxio_apiKey_LIVE");
            secret = config.getString("Phaxio_apiSecret_LIVE");
        }
        Phaxio.apiKey = key;
        Phaxio.apiSecret = secret;
    }

    public int getPremiumPagesCount() {
        try {
            return getUserProfile().getPaidPages();
        } catch (Exception e) {
            Log.e(TAG,e.getMessage(),e);
            return 0;
        }
    }

    public int getAviablePagesCount() {
        return getPremiumPagesCount() + getAvailableFreePages();
    }

    public boolean isTestMode() {
        return false;
    }

    public ColorStateList getDefColors() {
        return defColors;
    }

    public void setDefColors(ColorStateList defColors) {
        this.defColors = defColors;
    }

    public int getCountry() {
        try {
            String country = ParseUser.getCurrentUser().getString("country");
            return Countries.get().getCountryPositionByName(country);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage(),e);
            return 0;
        }
    }

    public int getAvailableFreePages() {
        return getUserProfile().getFreePages();
    }

    public String getCustomHeader() {
        if (getPremiumPagesCount() == 0) {
            return null;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!sharedPref.getBoolean("customHeader", false)) {
            return null;
        } else {
            String textHeader = sharedPref.getString("textHeader", "");
            if (textHeader.isEmpty()) return null;
            Log.i(TAG, "header:" + textHeader);
            return textHeader;
        }
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        ParseUser user = ParseUser.getCurrentUser();
        if (user.get("profile")==null) {
            user.put("profile", userProfile);
            user.saveInBackground();
        }
    }

    public UserProfile getUserProfile() {
        if (userProfile==null) {
            final ParseUser currentUser = ParseUser.getCurrentUser();
            ParseQuery<UserProfile> query = UserProfile.getQuery();
            query.whereEqualTo("user", currentUser);
            try {
                userProfile = query.getFirst();
            } catch (Exception e) {
                Log.e(TAG,e.getMessage(),e);
            }

        }
        return userProfile;
    }

    public void saveFreePages(int i) {
        getUserProfile().setFreePages(i);
        getUserProfile().saveInBackground();
    }

    public void setPremiumPagesCount(int premiumPagesCount) {
        getUserProfile().setPaidPages(premiumPagesCount);
        getUserProfile().saveInBackground();
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static String getUserCountry() {
        return userCountry;
    }

    public static void setUserCountry(String userCountry) {
        App.userCountry = userCountry;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public void setUnlimited(boolean unlimited) {
        this.unlimited = unlimited;
    }

    public boolean isFaxToEmail() {
        return isFaxToEmail;
    }

    public void setIsFaxToEmail(boolean isFaxToEmail) {
        this.isFaxToEmail = isFaxToEmail;
    }

    public String getMyFaxEmail() {
        return config.getString("MyFaxDistEmail");
    }

    public String getMyFaxPassword() {
        return config.getString("MyFaxDistPassword");
    }
}
