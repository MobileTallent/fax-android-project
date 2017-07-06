package co.faxapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import co.faxapp.fragments.PrefsFragment;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
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

//    public void updateNumber() {
//        FragmentManager fragmentManager = getFragmentManager();
//        PrefsFragment fragment = (PrefsFragment)fragmentManager.findFragmentById(android.R.id.content);
//        if (fragment != null) {
//            fragment.updateNumber();
//        }
//    }
}
