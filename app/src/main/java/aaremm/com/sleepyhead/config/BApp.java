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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aaremm.com.sleepyhead.R;
import aaremm.com.sleepyhead.object.Station;

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

    public static void setSPString(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getInstance());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
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
        hasMapStation();
        createStationList();
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


    public static final String LOCATION_BROADCAST = "aaremm.com.sleepyhead.android.locaction.broadcast";
    public static final String USER_ACTIVITY_BROADCAST = "aaremm.com.sleepyhead.android.useractivity.broadcast";
    public static final String INTENT_ACTION = "action";
    public static final String LC_ACTION = "locchanged";
    public static final String GEOFENCES_ACTION = "set_geofence";
    public static final String MOVING_ACTION = "moving_activity";
    public static final String STILL_ACTION = "still_activity";


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

    public void resetBusAlarm() {
        setSPBoolean(WA_ALARM_STATUS, false);
        setSPBoolean(WA_NOT_NEEDED_STATUS, false);
        setSPBoolean("busalarm",false);
    }

    public Integer[] lineColors = new Integer[]{
            R.color.red,
            R.color.lightGreen,
            R.color.yellow,
            R.color.purple,
            R.color.voilet,
            R.color.magenta,
            R.color.orange,
            R.color.blue,
            R.color.lightblue,
            R.color.mauve,
            R.color.brown,
            R.color.green,
            R.color.pink,
            R.color.blue,
            R.color.blue,
            R.color.aqua,
    };

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

    public String[] line7 = {"Meilan Lake",
            "Luonan Xincun",
            "Panguang Road",
            "Liuhang",
            "Gucun Park",
            "Qihua Road",
            "Shanghai University",
            "Nanchen Road",
            "Shangda Road",
            "Changzhong Road",
            "Dachang Town",
            "Xingzhi Road",
            "Dahuasan Road",
            "Xincun Road",
            "Langao Road",
            "Zhenping Road",
            "Changshou Road",
            "Changping Road",
            "Jing'an Temple",
            "Changshu Road",
            "Zhaojiabang Road",
            "Dong'an Road",
            "Middle Longhua Road",
            "Houtan",
            "Changqing Road",
            "Yaohua Road",
            "Yuntai Road",
            "West Gaoke Road",
            "South Yanggao Road",
            "Jinxiu Road",
            "Fanghua Road",
            "Longyang Road",
            "Huamu Road"
    };

    public String[] line8 = {"Shiguang Road",
            "Nenjiang Road",
            "Xiangyin Road",
            "Huangxing Park",
            "Middle Yanji Road",
            "Huangxing Road",
            "Jiangpu Road",
            "Anshan Xincun",
            "Siping Road",
            "Quyang Road",
            "Hongkou Football Stadium",
            "North Xizang Road",
            "Zhongxing Road",
            "Qufu Road",
            "People's Square",
            "Dashijie",
            "Laoximen",
            "Lujiabang Road",
            "South Xizang Road",
            "China Art Museum",
            "Yaohua Road",
            "Chengshan Road",
            "Yangsi",
            "Oriental Sports Center",
            "Lingzhao Xincun",
            "Luheng Road",
            "Pujiang Town",
            "Jiangyue Road",
            "Lianhang Road",
            "Shendu Highway"
    };

    public String[] line9 = {"Songjiang South Railway Station",
            "Zuibaichi Park",
            "Songjiang Sports Center",
            "Songjiang Xincheng",
            "Songjiang University Town",
            "Dongjing",
            "Sheshan",
            "Sijing",
            "Jiuting",
            "Zhongchun Road",
            "Qibao",
            "Xingzhong Road",
            "Hechuan Road",
            "Caohejing Hi-Tech Park",
            "Guilin Road",
            "Yishan Road",
            "Xujiahui",
            "Zhaojiabang Road",
            "Jiashan Road",
            "Dapuqiao",
            "Madang Road",
            "Lujiabang Road",
            "Xiaonanmen",
            "Shangcheng Road",
            "Century Avenue",
            "Middle Yanggao Road"
    };

    public String[] line10 = {
            "Hangzhong Road",
            "Ziteng Road",
            "Longbai Xincun",
            "Hongqiao Railway Station",
            "Hongqiao Airport Terminal 2",
            "Hongqiao Airport Terminal 1",
            "Shanghai Zoo",
            "Longxi Road",
            "Shuicheng Road",
            "Yili Road",
            "Songyuan Road",
            "Hongqiao Road",
            "Jiaotong University",
            "Shanghai Library",
            "South Shaanxi Road",
            "Xintiandi",
            "Laoximen",
            "Yuyuan Garden",
            "East Nanjing Road",
            "Tiantong Road",
            "North Sichuan Road",
            "Hailun Road",
            "Youdian Xincun",
            "Siping Road",
            "Tongji University",
            "Guoquan Road",
            "Wujiaochang",
            "Jiangwan Stadium",
            "Sanmen Road",
            "East Yingao Road",
            "Xinjiangwancheng"
    };

    public String[] line11 = {"Luoshan Road",
            "Yuqiao",
            "Pusan Road",
            "East Sanlin",
            "Sanlin",
            "Oriental Sports Center",
            "Longyao Road",
            "Yunjin Road",
            "Longhua",
            "Shanghai Swimming Center",
            "Xujiahui",
            "Jiaotong University",
            "Jiangsu Road",
            "Longde Road",
            "Caoyang Road",
            "Fengqiao Road",
            "Zhenru",
            "Shanghai West Railway Station",
            "Liziyuan",
            "Qilianshan Road",
            "Wuwei Road",
            "Taopu Xincun",
            "Nanxiang",
            "Malu",
            "Jiading Xincheng",
            "Baiyin Road",
            "West Jiading",
            "North Jiading",
            "Shanghai Circuit",
            "East Changji Road",
            "Shanghai Automobile City",
            "Anting",
            "Zhaofeng Road",
            "Guangming Road",
            "Huaqiao"
    };

    public String[] line12 = {"Qufu Road",
            "Tiantong Road",
            "International Cruise Terminal",
            "Tilanqiao",
            "Dalian Road",
            "Jiangpu Park",
            "Ningguo Road",
            "Longchang Road",
            "Aiguo Road",
            "Fuxing Island",
            "Donglu Road",
            "Jufeng Road",
            "North Yanggao Road",
            "Jinjing Road",
            "Shenjiang Road",
            "Jinhai Road"
    };

    public String[] line13 = {"Jinyun Road",
            "West Jinshajiang Road",
            "Fengzhuang",
            "South Qilianshan Road",
            "Zhenbei Road",
            "Daduhe Road",
            "Jinshajiang Road"
    };

    public String[] line16 = {"Luoshan Road",
            "East Zhoupu",
            "Heshahangcheng",
            "East Hangtou",
            "Xinchang",
            "Wild Animal Park",
            "Huinan",
            "East Huinan",
            "Shuyuan",
            "Lingang Avenue",
            "Dishui Lake"
    };

    public HashMap<String, List<Integer>> stations = new HashMap<String, List<Integer>>();

    public void hasMapStation() {
        for (int i = 1; i < 15; i++) {
            String[] temp = new String[0];
            int lineN = i;
            if (i == 14)
                lineN = 16;
            switch (i) {
                case 1: {
                    temp = line1;
                    break;
                }
                case 2: {
                    temp = line2;
                    break;
                }
                case 3: {
                    temp = line3;
                    break;
                }
                case 4: {
                    temp = line4;
                    break;
                }
                case 5: {
                    temp = line5;
                    break;
                }
                case 6: {
                    temp = line6;
                    break;
                }
                case 7: {
                    temp = line7;
                    break;
                }
                case 8: {
                    temp = line8;
                    break;
                }
                case 9: {
                    temp = line9;
                    break;
                }
                case 10: {
                    temp = line10;
                    break;
                }
                case 11: {
                    temp = line11;
                    break;
                }
                case 12: {
                    temp = line12;
                    break;
                }
                case 13: {
                    temp = line13;
                    break;
                }
                case 14: {
                    temp = line16;
                    break;
                }
            }
            int l = temp.length;
            for (int j = 0; j < l; j++) {
                List<Integer> lines = new ArrayList<Integer>();
                if (stations.containsKey(temp[j])) {
                    lines = stations.get(temp[j]);
                }
                lines.add(lineN);
                stations.put(temp[j], lines);
            }
        }
    }

    public List<Station> stationList = new ArrayList<Station>();

    public void createStationList() {

        for (Map.Entry<String, List<Integer>> entry : stations.entrySet()) {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();
            stationList.add(new Station(key, value));
        }
    }

    public List<Station> getAllStations() {
        return stationList;
    }

    public List<String> getStationsOnLineNos(List<Integer> lineNos, String s) {
        List<String> destStations = new ArrayList<String>();
        for (int i = 0; i < lineNos.size(); i++) {
            switch (lineNos.get(i)) {
                case 1: {
                    destStations.addAll(Arrays.asList(line1));
                    break;
                }
                case 2: {
                    destStations.addAll(Arrays.asList(line2));
                    break;
                }
                case 3: {
                    destStations.addAll(Arrays.asList(line3));
                    break;
                }
                case 4: {
                    destStations.addAll(Arrays.asList(line4));
                    break;
                }
                case 5: {
                    destStations.addAll(Arrays.asList(line5));
                    break;
                }
                case 6: {
                    destStations.addAll(Arrays.asList(line6));
                    break;
                }
                case 7: {
                    destStations.addAll(Arrays.asList(line7));
                    break;
                }
                case 8: {
                    destStations.addAll(Arrays.asList(line8));
                    break;
                }
                case 9: {
                    destStations.addAll(Arrays.asList(line9));
                    break;
                }
                case 10: {
                    destStations.addAll(Arrays.asList(line10));
                    break;
                }
                case 11: {
                    destStations.addAll(Arrays.asList(line11));
                    break;
                }
                case 12: {
                    destStations.addAll(Arrays.asList(line12));
                    break;
                }
                case 13: {
                    destStations.addAll(Arrays.asList(line13));
                    break;
                }
                case 14: {
                    destStations.addAll(Arrays.asList(line16));
                    break;
                }
            }
        }
        destStations.remove(s);
        return destStations;
    }

    public List<String> getStationListAtLineNoFromTO(int lineNo, String source, String dest) {
        List<String> temp = getStationListAtLineNo(lineNo);
        int si = temp.indexOf(source);
        int di = temp.indexOf(dest);
        int starti = si < di ? si : di;
        int endi = (si > di ? si : di);
        if(lineNo != 4) {
            temp = temp.subList(starti, endi+1);
        }else{
            int str = Math.abs(endi-starti);
            int lop = Math.abs(temp.size()-endi)+starti;
            if(str<=lop){
                temp = temp.subList(starti, endi+1);
            }else{
                List<String> temp1 = temp.subList(0,starti+1);
                Collections.reverse(temp1);
                List<String> temp2 = temp.subList(endi,temp.size());
                Collections.reverse(temp2);
                temp = new ArrayList<String>(temp1);
                temp.addAll(temp2);
            }
        }
        if (si > di) {
            Collections.reverse(temp);
        }
        return temp;
    }

    public List<String> getStationListAtLineNo(int lineNo) {
        switch (lineNo) {
            case 1: {
                return Arrays.asList(line1);
            }
            case 2: {
                return Arrays.asList(line2);
            }
            case 3: {
                return Arrays.asList(line3);
            }
            case 4: {
                return Arrays.asList(line4);
            }
            case 5: {
                return Arrays.asList(line5);
            }
            case 6: {
                return Arrays.asList(line6);
            }
            case 7: {
                return Arrays.asList(line7);
            }
            case 8: {
                return Arrays.asList(line8);
            }
            case 9: {
                return Arrays.asList(line9);
            }
            case 10: {
                return Arrays.asList(line10);
            }
            case 11: {
                return Arrays.asList(line11);
            }
            case 12: {
                return Arrays.asList(line12);
            }
            case 13: {
                return Arrays.asList(line13);
            }
            case 16: {
                return Arrays.asList(line16);
            }
        }
        return null;
    }

    public int getCurrentStationNo() {
        return getSPInteger("cstation");
    }

    public void setCurrentStationNo() {
        setSPInteger("cstation", getSPInteger("cstation") + 1);
    }

    public void setCurrentActivity(int a) { // 0- still 1- moving
        setSPInteger("cactivity", a);
    }

    public int getCurrentActivity() { // 0- still 1- moving
        return getSPInteger("cactivity");
    }

    public void setStatus(int i) {
        setSPInteger("status", i);
    }

    public int getStatus() {
        return getSPInteger("status");
    }

    public void setAlarmStationNo(int i) {
        setSPInteger("alarmSN", i);
    }

    public int getAlarmStationNo() {
        return getSPInteger("alarmSN");
    }

    public String getDestMetroStationName() {
        return getSPString("metroDest");
    }

    public void setDestMetroStationName(String name) {
        setSPString("metroDest", name);
    }

    public void resetMetroAlarm() {
        setSPInteger("cstation", 0);
        setSPInteger("cactivity", 0);
        setSPInteger("status", 0);
        setSPInteger("alarmSN", -1);
        setSPBoolean("metroalarm",false);
    }

    public void setCurrentStationNo(int position) {
        setSPInteger("cstation", position);
    }
}


