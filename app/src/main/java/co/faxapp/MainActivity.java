package co.faxapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import co.faxapp.dialogs.Dialogs;
import co.faxapp.fragments.FaxesListFragment;
import co.faxapp.model.UserProfile;
import co.faxapp.util.Log;
import co.faxapp.util.PurchasesHelper;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;
    private PurchasesHelper purchasesHelper;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAccess()) {
                    Intent intent = new Intent(MainActivity.this, FaxDetailsActivity.class);
                    startActivity(intent);
                }
            }
        });
        app = (App) getApplication();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        final ParseUser currentUser = ParseUser.getCurrentUser();
        final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        ParseQuery<UserProfile> query = UserProfile.getQuery();
        query.whereEqualTo("user", currentUser);
        query.getFirstInBackground(new GetCallback<UserProfile>() {
            @Override
            public void done(final UserProfile object, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Query UserProfile is error: " + e.getMessage() + ", create new profile!");
                    final UserProfile userProfile = new UserProfile();
                    userProfile.setUser(currentUser);
                    userProfile.setPaidPages(0);
                    userProfile.setFreePages(app.getDefaultFreePages());
                    userProfile.setLocked(false);
                    userProfile.setACL(new ParseACL(currentUser));
                    userProfile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            parseInstallation.put("profile", userProfile);
                            parseInstallation.saveInBackground();
                        }
                    });
                    app.setUserProfile(userProfile);
                } else {
                    app.setUserProfile(object);
                    parseInstallation.put("profile", object);
                    parseInstallation.saveInBackground();
                    if (object.isLocked()) {
                        Dialogs.lockDialog(MainActivity.this);
                    }
                }

            }
        });
        String base64EncodedPublicKey = app.getBase64EncodedPublicKey();
        purchasesHelper = new PurchasesHelper(this, base64EncodedPublicKey, app);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

    }

    private boolean checkAccess() {
        ParseUser current = ParseUser.getCurrentUser();
        try {
            current.fetch();
        } catch (ParseException e) {
            Log.e(TAG, "Fetch current user error", e);
        }
        String email = current.getEmail();
        if (email != null) {
            Object obj = current.get("emailVerified"); //// TODO: 17.12.2015 проапдейтить таблицу юзеров и убрать этот колхоз
            Boolean verify = current.getBoolean("emailVerified");
            if (!verify && obj != null) {
                Dialogs.confirmEmailDialog(MainActivity.this);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionsForApp();
        } else {
            startFaxApp();
        }

    }

    private void startFaxApp() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment =new FaxesListFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
        String country = ParseUser.getCurrentUser().getString("country");
        if (country == null || country.isEmpty()) {
            if (App.getUserCountry() == null) {
                Dialogs.setUserCountry(this, -1, null);
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getPermissionsForApp() {
        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.GET_ACCOUNTS);
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.GET_ACCOUNTS)) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI
            }
        }
        if (permissions.size() > 0) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), PERMISSIONS_REQUEST);
        } else {
            startFaxApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0) {
                boolean ok = true;
                for (int i : grantResults) {
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    startFaxApp();
                    return;
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);

        } else if (id == R.id.purchases) {
            Intent intent = new Intent(this, PurchasesActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        purchasesHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        purchasesHelper.onDestroy();
        super.onDestroy();
    }

}
