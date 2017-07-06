package co.faxapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.net.InetAddress;

import co.faxapp.util.Log;

public class DispatchActivity extends AppCompatActivity {
    private static final int LOGIN_REQUEST = 0;
    private static final String TAG = DispatchActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        //// TODO: 17.11.2015 No internet - no faxing!
//        if (!isInternetAvailable()) {
//            AlertDialog.Builder bld = new AlertDialog.Builder(this);
//            bld.setMessage(R.string.no_internet);
//            bld.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            });
//            bld.create().show();
//        }
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        runDispatch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult " + requestCode + " " + resultCode + " " + data);
        setResult(resultCode);
        runDispatch();
    }

    private void runDispatch() {
        if (ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            startActivityForResult(getParseLoginIntent(), LOGIN_REQUEST);
        }
    }

    /**
     * Override this to generate a customized intent for starting ParseLoginActivity.
     * However, the preferred method for configuring Parse Login UI components is by
     * specifying activity options in AndroidManifest.xml, not by overriding this.
     *
     * @return Intent that can be used to start ParseLoginActivity
     */
    protected Intent getParseLoginIntent() {
        ParseLoginBuilder builder = new ParseLoginBuilder(this);
        return builder.build();
    }

}
