package co.faxapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phaxio.status.FaxStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.faxapp.db.HelperFactory;
import co.faxapp.model.FaxEntity;
import co.faxapp.model.FaxItem;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class CheckStatusService extends IntentService {
    private static final String TAG = CheckStatusService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;

    public CheckStatusService() {
        super("CheckStatusService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        App mApp = (App) getApplication();
        if (intent != null) {
            try {
                boolean changes = false;
                int successPages = 0;
                int failPages = 0;
                List<String> successNumbers = new ArrayList<>();
                List<String> failNumbers = new ArrayList<>();
                List<FaxEntity> faxEntities = HelperFactory.getHelper().getFaxEntityDao().getInProgressList();
                if (faxEntities.size() == 0) {
                    NotifyAlarm mNotifyAlarm = new NotifyAlarm();
                    mNotifyAlarm.cancelAlarm(this);
                }
                for (FaxEntity faxEntity : faxEntities) {
                    mApp.setPhaxioKeys();
                    FaxStatus faxStatus = Tools.getFaxStatus(faxEntity.getPhaxioId());
                    if (faxStatus != null && (faxStatus == FaxStatus.SUCCESS || faxStatus == FaxStatus.FAILURE)) {
                        Log.i(TAG, "status: " + faxStatus.getApiName());
                        ParseQuery<FaxItem> query = FaxItem.getQuery();
                        query.whereEqualTo("user", ParseUser.getCurrentUser());
                        query.whereEqualTo("phaxioId", faxEntity.getPhaxioId());
                        if (faxStatus == FaxStatus.SUCCESS) {
                            faxEntity.setStatus(2);
                            successPages += Tools.getFaxPagesCount(faxEntity);
                            successNumbers.add(faxEntity.getPhoneNumber());
                            query.getFirstInBackground(new GetCallback<FaxItem>() {
                                @Override
                                public void done(FaxItem object, ParseException e) {
                                    if (object != null) {
                                        object.setPhaxioStatus("success");
                                        object.saveInBackground();
                                    }
                                }
                            });
                            //
                            setSuccessStatusForRate();
                        } else {
                            faxEntity.setStatus(3);
                            //// TODO: 06.01.2016 Также проверять бесплатные страницы и возвращать их
                            int paidPages = mApp.getPremiumPagesCount();
                            mApp.setPremiumPagesCount(paidPages + faxEntity.getPaidPagesCount());
                            failPages += Tools.getFaxPagesCount(faxEntity);
                            failNumbers.add(faxEntity.getPhoneNumber());
                            query.getFirstInBackground(new GetCallback<FaxItem>() {
                                @Override
                                public void done(FaxItem object, ParseException e) {
                                    if (object != null) {
                                        object.setPhaxioStatus("failure");
                                        object.saveInBackground();
                                    }
                                }
                            });
                        }
                        faxEntity.setUpdateDate(new Date());
                        HelperFactory.getHelper().getFaxEntityDao().update(faxEntity);
                        changes = true;

                    }
                }
                if (changes) {
                    sendNotification(successPages, successNumbers, failPages, failNumbers);
                }
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            NotifyAlarm.completeWakefulIntent(intent);
        }
    }

    private void setSuccessStatusForRate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putBoolean("success", true);
        prefEditor.apply();
    }

    private void sendNotification(int successPages, List<String> successNumbers, int failPages, List<String> failNumbers) {
        if (App.isActivityVisible()) {
            return;
        }
        String content = "";
        String positive = "";
        String negative = "";
        if (successPages > 0) {
            String numbers = "";
            for (int i = 0; i < successNumbers.size(); i++) {
                numbers += successNumbers.get(i);
                if (successNumbers.size() - i > 1) {
                    numbers += ", ";
                }
            }
            positive = String.format(getString(R.string.notity_message_success), successPages, numbers);
        }

        if (failPages > 0) {
            String numbers = "";
            for (int i = 0; i < failNumbers.size(); i++) {
                numbers += failNumbers.get(i);
                if (failNumbers.size() - i > 1) {
                    numbers += ", ";
                }
            }
            negative = String.format(getString(R.string.notity_message_fail), failPages, numbers);
        }
        if (!positive.isEmpty() && !negative.isEmpty()) {
            content = positive + "\n" + negative;
        } else if (!positive.isEmpty()) {
            content = positive;
        } else if (!negative.isEmpty()) {
            content = negative;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_notify_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.notyfy_title_mes))
                .setContentText(getString(R.string.notyfy_mes))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
