package co.faxapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.sql.SQLException;
import java.util.Date;

import co.faxapp.db.HelperFactory;
import co.faxapp.model.FaxEntity;
import co.faxapp.model.FaxItem;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class ServiceReceiver extends BroadcastReceiver {
    private static final String TAG = ServiceReceiver.class.getSimpleName();
    public static final String RESULT_SEND = "co.faxapp.action.RESULT_SEND";

    public ServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long faxId = intent.getLongExtra(NetworkService.FAX_ID, -1);
        Log.i(TAG, "onReceive");
        try {
            boolean ok = intent.getBooleanExtra(NetworkService.SEND_RESULT, false);
            FaxEntity entity = HelperFactory.getHelper().getFaxEntityDao().queryForId(faxId);
            Date date = new Date();
            entity.setSendDate(date);
            entity.setUpdateDate(date);
            if (ok) {
                entity.setStatus(1);
            } else {
                entity.setStatus(3);
            }
            long phaxioId = intent.getLongExtra(NetworkService.PHAXIO_ID,-1);
            if (phaxioId!=-1) {
                entity.setPhaxioId(phaxioId);
                FaxItem faxItem = Tools.createFaxItem(entity);
                faxItem.saveInBackground();
            }
            HelperFactory.getHelper().getFaxEntityDao().update(entity);
            sendResult(context,ok,intent.getStringExtra(NetworkService.RESULT_STRING),intent.getLongExtra(NetworkService.FAX_ID,-1));
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void sendResult(Context context, boolean ok, String result, long faxId) {
        Intent intentResponse = new Intent();
        intentResponse.setAction(RESULT_SEND);
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        intentResponse.putExtra(NetworkService.SEND_RESULT, ok);
        intentResponse.putExtra(NetworkService.RESULT_STRING, result);
        intentResponse.putExtra(NetworkService.FAX_ID, faxId);
        context.sendBroadcast(intentResponse);
    }
}
