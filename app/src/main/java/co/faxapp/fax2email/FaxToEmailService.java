package co.faxapp.fax2email;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import co.faxapp.App;
import co.faxapp.R;
import co.faxapp.model.FaxToEmail;

public class FaxToEmailService {
    private final static String WS_CREATE_URL = "https://www.myfax.co.il/action/myFaxAllocateDistributer.do";
    private final static String WS_DELETE_URL = "https://www.myfax.co.il/action/releaseFaxLine.do";

    private App app;
    private Activity activity;
    private ProgressDialog progressDialog;
    private static final String TAG = FaxToEmailService.class.getSimpleName();

    public FaxToEmailService(Activity activity) {
        this.activity = activity;
        this.app = (App) activity.getApplication();
    }

    public void createUserRequest(String email) {
        CreateTask createTask = new CreateTask(email);
        createTask.execute();
    }

    public void removeUserRequest(String phone, boolean onlyPhoneAndEmail) {
        DeleteTask deleteTask = new DeleteTask(phone, onlyPhoneAndEmail);
        deleteTask.execute();
    }

    private class CreateTask extends AsyncTask<String, Void, String> {
        private String email;
        private String phone;

        public CreateTask(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogShow();
        }

        @Override
        protected String doInBackground(String... params) {
            phone = getPhone();
            if (phone != null) {
                HashMap<String, String> postParams = new HashMap<>();
                postParams.put("distEmail", app.getMyFaxEmail());
                postParams.put("distPassword", app.getMyFaxPassword());
                postParams.put("email", email);
                postParams.put("requestedNumber", phone);
                postParams.put("noMail", "true");
                String resp = performPostCall(WS_CREATE_URL, postParams);
                Log.i(TAG, "resp = " + resp);
                return resp;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            if (aVoid != null) {
                // TODO: 11.01.2016 Разобрать ответ, проверить на ошибки
//                PreferencesActivity preferencesActivity = (PreferencesActivity) activity;
//                preferencesActivity.updateNumber();
//
//                ParseQuery<FaxToEmail> query = FaxToEmail.getQuery();
//                query.whereEqualTo("user", ParseUser.getCurrentUser());
//                query.getFirstInBackground(new GetCallback<FaxToEmail>() {
//                    @Override
//                    public void done(FaxToEmail faxToEmail, ParseException e) {
//                        if (faxToEmail == null) {
//                            faxToEmail = new FaxToEmail();
//                            faxToEmail.setUser(ParseUser.getCurrentUser());
//                        }
//                        faxToEmail.setEmail(email);
//                        faxToEmail.setPhone(phone);
//                        faxToEmail.saveInBackground(new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                Dialogs.congratulationsFaxesDialog(activity, phone);
//                                dialogClose();
//                            }
//                        });
//                    }
//                });


            }
            dialogClose();
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, String> {
        private String phone;
        boolean onlyPhoneAndEmail;

        public DeleteTask(String phone, boolean onlyPhoneAndEmail) {
            this.phone = phone;
            this.onlyPhoneAndEmail = onlyPhoneAndEmail;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogShow();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> postParams = new HashMap<>();
            postParams.put("distEmail", app.getMyFaxEmail());
            postParams.put("distPassword", app.getMyFaxPassword());
            postParams.put("faxLineDID ", phone);
            String resp = performPostCall(WS_DELETE_URL, postParams);
            Log.i(TAG, "resp = " + resp);
            return resp;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            // TODO: 11.01.2016 Разобрать ответ, проверить на ошибки
            if (aVoid != null) {
                try {
                    ParseQuery<FaxToEmail> query = FaxToEmail.getQuery();
                    query.whereEqualTo("phone", phone);
                    FaxToEmail faxToEmail = query.getFirst();
                    if (faxToEmail != null) {
                        if (onlyPhoneAndEmail) {
                            faxToEmail.setPhone("");
                            faxToEmail.setEmail("");
                            faxToEmail.saveInBackground();
                        } else {
                            faxToEmail.deleteInBackground();
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("emailForFax","");
                            editor.putString("phoneForFax","");
                            editor.apply();
                        }
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "DeleteTask " + e.getMessage(), e);
                }
            }
            dialogClose();
        }
    }

    private String getPhone() {
        String number = null;
        try {
            for (int i = 773181005; i <= 773181145; i++) {
                number = "+972" + Integer.toString(i);
                ParseQuery<FaxToEmail> query = FaxToEmail.getQuery();
                query.whereEqualTo("phone", number);
                FaxToEmail faxToEmail = query.getFirst();
            }
        } catch (Exception e) {
            Log.e(TAG, "getPhone " + e.getMessage(), e);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("phoneForFax", number);
            editor.apply();
        }
        return number;
    }

    private String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();
            Log.i(TAG, "respcode=" + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line + "\n";
                }
            } else {
                response = "";

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public void dialogShow() {
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.waiting));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dialogClose() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
