package aaremm.com.sleepyhead.config;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BApp extends Application {
    // app
    private static BApp instance;
    private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 20;
    private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static int DISK_IMAGECACHE_QUALITY = 80;  //PNG is lossless so quality is ignored but must be provided
    public static SharedPreferences sp;
    // Debugging tag for the application
    public static final String APPTAG = "look";
    public static final String SWITCH = "switch";

    public static String USER_ACTION_YES = "Yes";
    public static String USER_ACTION_NO = "No";


    public AudioManager manager;

    /**
     * Create main application
     */
    public BApp() {

    }

    /**
     * Create main application
     *
     * @param context
     */
    public BApp(final Context context) {
        this();
        attachBaseContext(context);
    }


    public static void setSP(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }


    public static void setSPBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public static void setSPInteger(String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static Integer getSPInteger(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getInt(key, 0); // 0 - professor 1 - student
    }

    public static String getSPString(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getString(key, "");
    }

    public static Boolean getSPBoolean(String key) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        return sharedPreferences.getBoolean(key, false);
    }

/*
    public ArrayList<Typeface> getTypeface(int index) {
        String fontPath;
        Typeface tf;
        ArrayList<Typeface> tfs = new ArrayList<Typeface>();
        switch (index) {
            case 3:
                fontPath = "fonts/Lato-Reg.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Bol.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Lig.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/Lato-Bla.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                return tfs;
            case 4:
                fontPath = "fonts/OpenSans-Regular.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Bold.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Semibold.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                fontPath = "fonts/OpenSans-Light.ttf";
                tf = Typeface.createFromAsset(getAssets(), fontPath);
                tfs.add(tf);
                return tfs;
        }
        return null;
    }*/


    public String getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;

                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("HashKey", something);
                return something;
            }
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Create main application
     *
     * @param instrumentation
     */
    public BApp(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static BApp getInstance() {

        if (instance == null) {
            instance = new BApp();
            return instance;

        } else {
            return instance;
        }
    }

    public boolean isAlarmNeeded() {
        if (getSPBoolean(WA_ALARM_STATUS)) {
            return false;
        }
        if (getSPBoolean(WA_NOT_NEEDED_STATUS) && (System.currentTimeMillis() - getSPInteger(WA_NOT_NEEDED_TIME_VALUE)) < 1000 * 60 * 60 * 3) {
            return false;
        }
        return true;
    }

    public static String WA_ALARM_STATUS = "WA_ALARM_STATUS";
    public static String WA_NOT_NEEDED_STATUS = "WA_VOLUNTARILY_NOT_NEEDED";
    public static String WA_NOT_NEEDED_TIME_VALUE = "WA_VOLUNTARILY_NOT_NEEDED_TIME";

    public void setSPLong(String key, long value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyCPqPECtWcEJkpNb3TlK_T5IJcDmOZy-Io";

    public ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:cn");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    public LatLng currentLocation;

    public void setCurrentLocation(LatLng loc) {
        currentLocation = loc;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }


    public static final String BROADCAST = "aaremm.com.sleepyhead.android.locaction.broadcast";
    public static final String INTENT_ACTION = "action";
    public static final String LC_ACTION = "locchanged";
    public static final String GEOFENCES_ACTION = "set_geofence";


    public LatLng geofenceLL;

    public void setGeofenceLL(LatLng latlng) {
        geofenceLL = latlng;
    }

    public LatLng getGeofenceLL() {
        return geofenceLL;
    }

    public String destAdress;

    public void setDestinationAddr(String destAddress) {
        this.destAdress = destAddress;
    }

    public String getDestAdress() {
        return destAdress;
    }

    public float geofenceRadius;

    public void setGeofenceRadius(float geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
    }

    public float getGeofenceRadius() {
        return geofenceRadius;
    }

    public void resetAlarm() {
        setSPBoolean(WA_ALARM_STATUS, false);
        setSPBoolean(WA_NOT_NEEDED_STATUS, false);
    }

    public String[] line1 = {"Xinzhuang",
            "Waihuanlu",
            "Lianhua Road",
            "Jinjiang Park",
            "Shanghai South Railway Station",
            "Caobao Road",
            "Shanghai Indoor Stadium",
            "Xujiahui",
            "Hengshan Road",
            "Changshu Road",
            "South Shaanxi Road",
            "South Huangpi Road",
            "People's Square",
            "Xinzha Road",
            "Hanzhong Road",
            "Shanghai Railway Station",
            "North Zhongshan Road",
            "Yanchang Road",
            "Shanghai Circus World",
            "Wenshui Road",
            "Pengpu Xincun",
            "Gongkang Road",
            "Tonghe Xincun",
            "Hulan Road",
            "Gongfu Xincun",
            "Bao'an Highway",
            "West Youyi Road",
            "Fujin Road"};

    public String[] line2 = {"East Xujing",
            "Hongqiao Railway Station",
            "Hongqiao Airport Terminal 2",
            "Songhong Road",
            "Beixinjing",
            "Weining Road",
            "Loushanguan Road",
            "Zhongshan Park",
            "Jiangsu Road",
            "Jing'an Temple",
            "West Nanjing Road",
            "People's Square",
            "East Nanjing Road",
            "Lujiazui",
            "Dongchang Road",
            "Century Avenue",
            "Shanghai Science & Technology Museum",
            "Century Park",
            "Longyang Road",
            "Zhangjiang Hi-Tech Park",
            "Jinke Road",
            "Guanglan Road",
            "Tangzhen",
            "Middle Chuangxin Road",
            "East Huaxia Road",
            "Chuansha",
            "Lingkong Road",
            "Yuandong Avenue",
            "Haitiansan Road",
            "Pudong International Airport"};

    public String[] line3 = {"Shanghai South Railway Station",
            "Shilong Road",
            "Longcao Road",
            "Caoxi Road",
            "Yishan Road",
            "Hongqiao Road",
            "West Yan'an Road",
            "Zhongshan Park",
            "Jinshajiang Road",
            "Caoyang Road",
            "Zhenping Road",
            "Zhongtan Road",
            "Shanghai Railway Station",
            "Baoshan Road",
            "Dongbaoxing Road",
            "Hongkou Football Stadium",
            "Chifeng Road",
            "Dabaishu",
            "Jiangwan Town",
            "West Yingao Road",
            "South Changjiang Road",
            "Songfa Road",
            "Zhanghuabang",
            "Songbin Road",
            "Shuichan Road",
            "Baoyang Road",
            "Youyi Road",
            "Tieli Road",
            "North Jiangyang Road"
    };
    //loop
    public String[] line4 = {"Yishan Road",
            "Hongqiao Road",
            "West Yan'an Road",
            "Zhongshan Park",
            "Jinshajiang Road",
            "Caoyang Road",
            "Zhenping Road",
            "Zhongtan Road",
            "Shanghai Railway Station",
            "Baoshan Road",
            "Hailun Road",
            "Linping Road",
            "Dalian Road",
            "Yangshupu Road",
            "Pudong Avenue",
            "Century Avenue",
            "Pudian Road",
            "Lancun Road",
            "Tangqiao",
            "Nanpu Bridge",
            "South Xizang Road",
            "Luban Road",
            "Damuqiao Road",
            "Dong'an Road",
            "Shanghai Stadium",
            "Shanghai Indoor Stadium"
    };

    public String[] line5 = {
            "Xinzhuang",
            "Chunshen Road",
            "Yindu Road",
            "Zhuanqiao",
            "Beiqiao",
            "Jianchuan Road",
            "Dongchuan Road",
            "Jinping Road",
            "Huaning Road",
            "Wenjing Road",
            "Minhang Development Zone"
    };

    public String[] line6 = {"Gangcheng Road",
            "North Waigaoqiao Free Trade Zone",
            "Hangjin Road",
            "South Waigaoqiao Free Trade Zone",
            "Zhouhai Road",
            "Wuzhou Avenue",
            "Dongjing Road",
            "Jufeng Road",
            "Wulian Road",
            "Boxing Road",
            "Jinqiao Road",
            "Yunshan Road",
            "Deping Road",
            "Beiyangjing Road",
            "Minsheng Road",
            "Yuanshen Stadium",
            "Century Avenue",
            "Pudian Road",
            "Lancun Road",
            "Shanghai Children's Medical Center",
            "Linyi Xincun",
            "West Gaoke Road",
            "Dongming Road",
            "Gaoqing Road",
            "West Huaxia Road",
            "Shangnan Road",
            "South Lingyan Road",
            "Oriental Sports Center"
    };

    public String[] line7 = {

    };
}


