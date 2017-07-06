package co.faxapp;

import android.app.IntentService;
import android.content.Intent;

import com.itextpdf.text.pdf.PdfReader;
import com.phaxio.Fax;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.faxapp.db.HelperFactory;
import co.faxapp.model.FaxEntity;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class NetworkService extends IntentService {
    private static final String TAG = NetworkService.class.getSimpleName();
    public static final String ACTION_SEND = "co.faxapp.action.ACTION_SEND";
    public static final String SEND_RESULT = "co.faxapp.result.SEND_RESULT";
    public static final String RESULT_STRING = "co.faxapp.result.RESULT_STRING";
    public static final String FAX_ID = "co.faxapp.result.FAXID";
    public static final String PHAXIO_ID = "co.faxapp.result.PHAXIO_ID";
    private App mApp;

    public NetworkService() {
        super("NetworkService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            mApp = (App) getApplication();
            if (action.equals(ACTION_SEND)) {
                long faxId = intent.getLongExtra("faxId", -1);
                Log.i(TAG, "faxId=" + faxId);
                sendFax(faxId);
            }
        }
    }

    private void sendFax(long faxId) {
        try {
            Log.i(TAG, "Start send");
            FaxEntity faxEntity = HelperFactory.getHelper().getFaxEntityDao().queryForId(faxId);
            long phaxioId = sendFax(faxEntity);
            Log.i(TAG, "End send");
            sendResult(true, getString(R.string.phaxio_answer_good), faxId, phaxioId);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            sendResult(false, e.getMessage(), faxId, -1);
        }
    }

    public long sendFax(FaxEntity mFaxEntity) throws Exception {
        mApp.setPhaxioKeys();
        Map<String, Object> options = new HashMap<>();
        String header = mApp.getCustomHeader();
        if (header==null) {
            header = getString(R.string.header_fax);
        }
        options.put("header_text", header);
//        options.put("string_data", "Test add page");
        List<String> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(mFaxEntity.getCode() + mFaxEntity.getPhoneNumber().trim());

        List<File> files = new ArrayList<>();
        List<String> paths = Tools.getListFromString(mFaxEntity.getFilesPaths());
        int limitPagesCount = mApp.getAviablePagesCount();
        if (mApp.isUnlimited()) {
            limitPagesCount=200;
        }
        if (limitPagesCount <= 0) {
            throw new Exception(getString(R.string.limit_error));
        }
        int allPages = 0;
        for (String s : paths) {
            if (s.toLowerCase().endsWith(".pdf")) {
                int rc = limitPagesCount - allPages;
                if (rc > 0) {
                    PdfReader reader = new PdfReader(s);
                    int n = reader.getNumberOfPages();
                    if (n > rc) {
                        throw new Exception(getString(R.string.limit_error));
                    }
                    allPages += n;
                }
            } else {
                if (!mApp.isUnlimited()) {
                    if (s.toLowerCase().endsWith(".doc") || s.toLowerCase().endsWith(".docx")) {
                        continue;
                    }
                }
                allPages++;
            }
            files.add(new File(s));
            if (allPages == limitPagesCount) {
                break;
            }
        }

        long phaxioId = Fax.send(phoneNumbers, files, options);
        if (!mApp.isUnlimited()) {
            saveUsedPages(allPages, mFaxEntity);
        }
        return phaxioId;
    }

    private void saveUsedPages(int sendedPages, FaxEntity entity) throws IOException, SQLException {
        int freePages = mApp.getAvailableFreePages();
        int prem = mApp.getPremiumPagesCount();
        if (sendedPages>=freePages) {
            mApp.saveFreePages(0);
            int usedPremPages = sendedPages-freePages;
            mApp.setPremiumPagesCount(prem-usedPremPages);
            entity.setPaidPagesCount(usedPremPages);
            HelperFactory.getHelper().getFaxEntityDao().update(entity);
        } else {
            mApp.saveFreePages(freePages - sendedPages);
        }
    }

    public void sendResult(boolean ok, String result, long faxId, long phaxioId) {
        Intent intentResponse = new Intent();
        intentResponse.setAction(ACTION_SEND);
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        intentResponse.putExtra(SEND_RESULT, ok);
        intentResponse.putExtra(RESULT_STRING, result);
        intentResponse.putExtra(FAX_ID, faxId);
        intentResponse.putExtra(PHAXIO_ID, phaxioId);
        sendBroadcast(intentResponse);
    }

}
