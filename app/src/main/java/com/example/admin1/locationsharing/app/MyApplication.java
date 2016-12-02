package com.example.admin1.locationsharing.app;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.example.admin1.locationsharing.BuildConfig;
import com.example.admin1.locationsharing.db.dao.DaoMaster;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.services.UserDataService;
import com.example.admin1.locationsharing.utils.Constants;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by admin1 on 30/11/16.
 */

public class MyApplication extends Application {
    private static SharedPreferencesData sharedPreferencesData;
    private Retrofit retrofit;
    private SQLiteDatabase database;
    private static DaoMaster daoMaster;
    private static Context applicationContext;
    private static MyApplication instance;
    private static Context context;
    private DaoMaster.DevOpenHelper devOpenHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeVariables();
        buildRetrofitClient();
        initializeDatabase(this);
        if (!BuildConfig.IS_PRODUCTION) {
            saveDataDb();
        }
    }
    public void saveDataDb() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.admin1.locationsharing.temp//databases//" +
                        Constants.DB_NAME;
                String backupDBPath = Constants.DB_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initializeVariables(){
        instance = this;
        sharedPreferencesData = new SharedPreferencesData(this);
        applicationContext = this.getApplicationContext();

    }

    private void initializeDatabase(Context context) {
        devOpenHelper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
    }

    public DaoSession getWritableDaoSession(Context context) {
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    /** Returns the Redable Dao Session*/
    public DaoSession getReadableDaoSession(Context context){
        SQLiteDatabase database = devOpenHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    public UserDataService getUserDataService(){
        return retrofit.create(UserDataService.class);
    }
    private void buildRetrofitClient() {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.HOST).build();
    }
    public boolean isConnectedToInterNet() {
        ConnectivityManager connectivity = (ConnectivityManager) getCurrentActivityContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
        }
        return false;
    }
    public Context getCurrentActivityContext() {
        if (context == null) {
            return applicationContext;
        } else {
            return context;
        }
    }
    public void setCurrentActivityContext(Context mContext) {
        context = mContext;
    }
    public static synchronized MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }


}
