package co.faxapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import co.faxapp.util.PurchasesHelper;

public class PurchasesActivity extends AppCompatActivity {
    private PurchasesHelper purchasesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        App app = (App) getApplication();
        String base64EncodedPublicKey = app.getBase64EncodedPublicKey();
        purchasesHelper = new PurchasesHelper(this, base64EncodedPublicKey, app);
//        Button buttonFax2Email = (Button) findViewById(R.id.buttonFax2Email);
//        Button buttonSendReceive = (Button) findViewById(R.id.buttonSendReceive);

//        buttonFax2Email.setVisibility(View.GONE);
//        buttonSendReceive.setVisibility(View.GONE);

//        String country = ParseUser.getCurrentUser().getString("country");
//        if (country.equals("Israel")) {
//            buttonFax2Email.setVisibility(View.VISIBLE);
//            buttonSendReceive.setVisibility(View.VISIBLE);
//        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button3) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_3, false);
        } else if (id == R.id.button25) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_25, false);
        } else if (id == R.id.button50) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_50, false);
        } else if (id == R.id.button100) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_100, false);
        } else if (id == R.id.button500) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_500, false);
        } else if (id == R.id.button1000) {
            purchasesHelper.buy(PurchasesHelper.SKU_PAGES_1000, false);
        }
//        else if (id == R.id.buttonUnlimitedMonth) {
//            purchasesHelper.buy(PurchasesHelper.SKU_MONTHLY, true);
//        } else if (id == R.id.buttonFax2Email) {
//            purchasesHelper.buy(PurchasesHelper.SKU_FAX_2_EMAIL, true);
//        } else if (id == R.id.buttonSendReceive) {
//            purchasesHelper.buy(PurchasesHelper.SKU_SEND_AND_RECEIVE, true);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        purchasesHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }

    @Override
    public void onDestroy() {
        purchasesHelper.onDestroy();
        super.onDestroy();
    }
}
