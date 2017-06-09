package com.riktam.mapmate.locationsharing.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.widget.Toast;

import com.riktam.mapmate.locationsharing.BuildConfig;
import com.riktam.mapmate.locationsharing.R;
import com.riktam.mapmate.locationsharing.db.dao.DaoMaster;
import com.riktam.mapmate.locationsharing.db.dao.DaoSession;
import com.riktam.mapmate.locationsharing.interfaces.PositiveClick;
import com.riktam.mapmate.locationsharing.receiver.BootCompletedIntentReceiver;
import com.riktam.mapmate.locationsharing.retrofitservices.RetrofitApiServices;
import com.riktam.mapmate.locationsharing.utils.Constants;
import com.riktam.mapmate.locationsharing.utils.CustomLog;
import com.riktam.mapmate.locationsharing.utils.SetupRetrofit;
import com.riktam.mapmate.locationsharing.utils.SharedPreferencesData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import retrofit2.Retrofit;

/**
 * Created by admin1 on 30/11/16.
 */

public class MyApplication extends Application {
    public SharedPreferencesData sharedPreferencesData;
    public Retrofit retrofit;
    private static Context applicationContext;
    private static MyApplication instance;
    private static Context context;
    private DaoMaster.DevOpenHelper devOpenHelper;
    private ProgressDialog mProgressDialog;
    public RetrofitApiServices retrofitApiServices = null;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeVariables();
        initializeDatabase(this);
        retrofitApiServices = SetupRetrofit.createService(RetrofitApiServices.class);
        if (!BuildConfig.IS_PRODUCTION) {
            saveDataDb();
        }
        scheduleAlarm();
    }

    public void saveDataDb() {
        CustomLog.d("MyApplication", "SaveDB");
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.admin1.locationsharing//databases//" +
                        Constants.DB_NAME;
                String backupDBPath = "location_sharing";
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
            CustomLog.e("MyApplication : ", e.getMessage());
        }
    }

    public void initializeVariables() {
        instance = this;
        sharedPreferencesData = new SharedPreferencesData();
        applicationContext = this.getApplicationContext();

    }

    private void initializeDatabase(Context context) {
        devOpenHelper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
    }


    public DaoSession getWritableDaoSession(Context context) {
        if (devOpenHelper == null) {
            initializeDatabase(context);
        }
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    /**
     * Returns the Redable Dao Session
     */
    public DaoSession getReadableDaoSession(Context context) {
        if (devOpenHelper == null) {
            initializeDatabase(context);
        }
        SQLiteDatabase database = devOpenHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    /**
     * Setup a recurring alarm every half hour
     */

    public void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), BootCompletedIntentReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, BootCompletedIntentReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HALF_HOUR , pIntent);
    }


    public boolean isConnectedToInterNet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            CustomLog.d("Network status", "" + activeNetworkInfo.isConnectedOrConnecting());
            return activeNetworkInfo.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    public static Context getCurrentActivityContext() {
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

    public void showToast(String message) {
        Toast.makeText(getCurrentActivityContext(), message, Toast.LENGTH_LONG).show();
    }

    public void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getCurrentActivityContext(), R.style
                .ProgressDialogStyle);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * Hides progress bar
     */
    public void hideProgressDialog() {
        CustomLog.d("Progress", "hide");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Shows progress bar
     */
    public void showProgressDialog(String title, String description) {

        CustomLog.d("Progress", "Show");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getCurrentActivityContext(), R.style
                .ProgressDialogStyle);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(description);
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void updateTextInProgressDialog(String text) {
        try {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.setMessage(text);
            }
        } catch (Exception e) {
            CustomLog.e("Update Message Error", e.getMessage());
        }
    }

    public void showAlert(String alertMessage) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext(), R
                .style.AlertDialogStyle);
        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.alert));
        // Setting Dialog Message
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener
                () {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    public void showAlertWithPositiveButton(String alertMessage, String positiveText,
                                            PositiveClick positiveClick) {
        showAlertWithPositiveButton(null, alertMessage, positiveText, positiveClick);
    }


    public void showAlertWithPositiveNegativeButton(String title, String alertMessage, String
            negativeText, String positiveText, final PositiveClick positiveClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext(), R
                .style.AlertDialogStyle);
        if (title != null) {
            alertDialog.setTitle(title);
        } else {
            alertDialog.setTitle(getString(R.string.alert));
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                positiveClick.onClick();
            }
        });
        alertDialog.show();
    }

    /**
     * @param title         (String) : text in title
     * @param alertMessage  (String) : alertMessage
     * @param positiveText  (String) : text in positive button
     * @param positiveClick (PositiveClick) : positiveClick
     */
    public void showAlertWithPositiveButton(String title, String alertMessage, String positiveText,
                                            final PositiveClick positiveClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext
                (), R.style.AlertDialogStyle);
        if (title != null) {
            alertDialog.setTitle(title);
        } else {
            alertDialog.setTitle(getString(R.string.alert));
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        if (!positiveText.equals(getString(R.string.ok))) {
            alertDialog.setNegativeButton(getString(R.string.ok), new DialogInterface
                    .OnClickListener
                    () {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        alertDialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                positiveClick.onClick();
            }
        });
        alertDialog.show();
    }
}
