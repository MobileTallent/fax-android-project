package co.faxapp.util;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

import co.faxapp.App;
import co.faxapp.R;
import co.faxapp.dialogs.Dialogs;
import co.faxapp.model.FaxToEmail;

public class PurchasesHelper {
    private static final String TAG = PurchasesHelper.class.getName();

    public static final String SKU_PAGES_3 = "faxpage"; //3 pages
    public static final String SKU_PAGES_25 = "25pages";
    public static final String SKU_PAGES_50 = "50pages";
    public static final String SKU_PAGES_100 = "100pages";
    public static final String SKU_PAGES_500 = "500pages";
    public static final String SKU_PAGES_1000 = "1000pages";
//    public static final String SKU_MONTHLY = "monthlyplan39";
//    public static final String SKU_FAX_2_EMAIL = "fax2mail";
//    public static final String SKU_SEND_AND_RECEIVE = "sendandrecive";


    static final int RC_REQUEST = 10001;
    private IabHelper mHelper;
    private App app;
    private SharedPreferences sp;
    private Activity activity;

    public PurchasesHelper(Activity callback, String base64EncodedPublicKey, App app) {
        this.activity = callback;
        this.app = app;
        sp = PreferenceManager.getDefaultSharedPreferences(callback);
        mHelper = new IabHelper(callback, base64EncodedPublicKey);

        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, "Problem setting up In-app Billing: " + result);
                } else {
                    Log.i(TAG, "Hooray, IAB is fully set up!");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "mHelper.handleActivityResult...");
        boolean b = mHelper.handleActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "mHelper.handleActivityResult..." + b);
    }

    public void buy(String scu, boolean subscription) {
        String payload = "";
        if (!subscription) {
            mHelper.launchPurchaseFlow(activity, scu, RC_REQUEST, mPurchaseFinishedListener, payload);
        } else {
            mHelper.launchPurchaseFlow(activity, scu, IabHelper.ITEM_TYPE_SUBS, RC_REQUEST, mPurchaseFinishedListener, payload);
        }
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.i(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                Log.e(TAG, "mPurchaseFinishedListener mHelper==null");
                return;
            }

            if (result.isFailure()) {
                Log.e(TAG, "mPurchaseFinishedListener Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Log.e(TAG, "mPurchaseFinishedListener Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.i(TAG, "mPurchaseFinishedListener Purchase successful.");

            if (purchase.getSku().equals(SKU_PAGES_3) ||
                    purchase.getSku().equals(SKU_PAGES_25) ||
                    purchase.getSku().equals(SKU_PAGES_50) ||
                    purchase.getSku().equals(SKU_PAGES_100) ||
                    purchase.getSku().equals(SKU_PAGES_500) ||
                    purchase.getSku().equals(SKU_PAGES_1000)
                    ) {
                Log.i(TAG, "Purchase is " + purchase.getSku() + " Starting pages consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
//            else if (purchase.getSku().equals(SKU_MONTHLY)) {
//                Log.i(TAG, "Purchase is " + purchase.getSku());
//                app.setUnlimited(true);
//            } else if (purchase.getSku().equals(SKU_FAX_2_EMAIL)) {
//                Log.i(TAG, "Purchase is " + purchase.getSku());
//                app.setIsFaxToEmail(true);
//                registerFax2Email(purchase, null);
//            } else if (purchase.getSku().equals(SKU_SEND_AND_RECEIVE)) {
//                Log.i(TAG, "Purchase is " + purchase.getSku());
//                app.setIsFaxToEmail(true);
//                app.setUnlimited(true);
//                registerFax2Email(purchase, null);
//            }
            Toast.makeText(activity, R.string.thanks, Toast.LENGTH_LONG).show();
        }
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.i(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Log.e(TAG, "Failed to query inventory: " + result);
                return;
            }

            String phone = sp.getString("phoneForFax", null);

            Log.i(TAG, "Query inventory was successful.");

            Purchase purchase = inventory.getPurchase(SKU_PAGES_3);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_3. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_3), mConsumeFinishedListener);
            }

            purchase = inventory.getPurchase(SKU_PAGES_25);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_25. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_25), mConsumeFinishedListener);
            }

            purchase = inventory.getPurchase(SKU_PAGES_50);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_50. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_50), mConsumeFinishedListener);
            }

            purchase = inventory.getPurchase(SKU_PAGES_100);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_100. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_100), mConsumeFinishedListener);
            }

            purchase = inventory.getPurchase(SKU_PAGES_500);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_500. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_500), mConsumeFinishedListener);
            }

            purchase = inventory.getPurchase(SKU_PAGES_1000);
            if (purchase != null && verifyDeveloperPayload(purchase)) {
                Log.i(TAG, "We have SKU_PAGES_1000. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_PAGES_1000), mConsumeFinishedListener);
            }

//            purchase = inventory.getPurchase(SKU_MONTHLY);
//            if (purchase != null && verifyDeveloperPayload(purchase)) {
//                Log.i(TAG, "We have SKU_MONTHLY. Set settings");
//                app.setUnlimited(true);
//            }

//            Purchase purchaseOne = inventory.getPurchase(SKU_FAX_2_EMAIL);
//            Purchase purchaseTwo = inventory.getPurchase(SKU_SEND_AND_RECEIVE);
//            if ((purchaseOne != null && verifyDeveloperPayload(purchaseOne)) || (purchaseTwo != null && verifyDeveloperPayload(purchaseTwo))) {
//                Log.i(TAG, "We have SKU_FAX_2_EMAIL or SKU_SEND_AND_RECEIVE. Set settings");
//                app.setIsFaxToEmail(true);
//                if (purchaseTwo != null) {
//                    app.setUnlimited(true);
//                    registerFax2Email(purchaseTwo, phone);
//                }
//                if (purchaseOne!=null) {
//                    registerFax2Email(purchaseOne, phone);
//                }
//            } else {
//                if (phone != null && !phone.equals("")) {
//                    FaxToEmailService faxToEmailService = new FaxToEmailService(activity);
//                    faxToEmailService.removeUserRequest(phone, false);
//                }
//            }
        }
    };

    public void addPremiumPages(int count) {
        int prem = app.getPremiumPagesCount();
        app.setPremiumPagesCount(prem + count);
    }

    private void registerFax2Email(final Purchase purchase, final String phone) {
        ParseQuery<FaxToEmail> query = FaxToEmail.getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<FaxToEmail>() {
            @Override
            public void done(FaxToEmail faxToEmail, ParseException e) {
                String phoneParse = null;
                String emailParse = null;
                if (faxToEmail == null) {
                    faxToEmail = new FaxToEmail();
                    faxToEmail.setUser(ParseUser.getCurrentUser());
                } else {
                    phoneParse = faxToEmail.getPhone();
                    emailParse = faxToEmail.getEmail();
                }
                faxToEmail.setPaymentDate(new Date(purchase.getPurchaseTime()).toString());
                final String finalPhoneParse = phoneParse;
                final String finalEmailParse = emailParse;
                faxToEmail.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (phone == null && finalPhoneParse != null) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("emailForFax", finalEmailParse);
                            editor.putString("phoneForFax",finalPhoneParse);
                            editor.apply();
                        } else if (phone==null) {
                            Dialogs.setEmailForFaxesDialog(activity);
                        }
                    }
                });
            }
        });
    }


    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.i(TAG, "mConsumeFinishedListener Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) {
                Log.e(TAG, "mConsumeFinishedListener mHelper==null");
                return;
            }

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.i(TAG, "Consumption successful. Provisioning.");
                switch (purchase.getSku()) {
                    case SKU_PAGES_3:
                        addPremiumPages(3);
                        break;
                    case SKU_PAGES_25:
                        addPremiumPages(25);
                        break;
                    case SKU_PAGES_50:
                        addPremiumPages(50);
                        break;
                    case SKU_PAGES_100:
                        addPremiumPages(100);
                        break;
                    case SKU_PAGES_500:
                        addPremiumPages(500);
                        break;
                    case SKU_PAGES_1000:
                        addPremiumPages(1000);
                        break;
                }
            } else {
                Log.e(TAG, "Error while consuming: " + result);
            }

            Log.i(TAG, "End consumption flow.");
        }
    };

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    public void onDestroy() {
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
