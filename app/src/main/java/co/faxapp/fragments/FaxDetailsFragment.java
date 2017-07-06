package co.faxapp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ui.Countries;
import com.parse.ui.Country;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.faxapp.App;
import co.faxapp.FavoriteNumbersActivity;
import co.faxapp.FaxDetailsActivity;
import co.faxapp.NetworkService;
import co.faxapp.NotifyAlarm;
import co.faxapp.PreferencesActivity;
import co.faxapp.PurchasesActivity;
import co.faxapp.R;
import co.faxapp.TextActivity;
import co.faxapp.adapters.ContentAdapter;
import co.faxapp.adapters.CountryAdapter;
import co.faxapp.db.HelperFactory;
import co.faxapp.dialogs.Dialogs;
import co.faxapp.dialogs.FileChooser;
import co.faxapp.model.FaxEntity;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class FaxDetailsFragment extends Fragment implements FileChooser.FileSelectedListener{
    private static final String TAG = FaxDetailsFragment.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 11;
    private static final int REQUEST_PICK_IMAGE = 12;
    private static final int REQUEST_PICK_CONTACT = 13;
    private static final int REQUEST_TXT = 14;
    private static final int REQUEST_PICK_FAVORITE = 15;
    static final String FAX_ID = "faxId";
    private EditText phoneNumberInput;
    private FaxEntity mFaxEntity;
    private String mCurrentPhotoPath;
    private TextView contactNameLabel;
    private TextView pagesCountValue;
    private ContentAdapter contentAdapter;
    private ProgressDialog progressDialog;
    private BroadcastReceiver answerReceiver;
    private Button saveButton;
    private Button sendButton;
    private App app;
    private Spinner spinner;

    public static FaxDetailsFragment newInstance(long faxId) {
        Bundle args = new Bundle();
        args.putLong(FAX_ID, faxId);
        FaxDetailsFragment fragment = new FaxDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (getArguments() != null) {
            long faxId = getArguments().getLong(FAX_ID);
            try {
                mFaxEntity = HelperFactory.getHelper().getFaxEntityDao().queryForId(faxId);

                if (mFaxEntity == null) {
                    mFaxEntity = new FaxEntity();
                } else {
                    Tools.checkAndFixFaxAttachments(mFaxEntity);
                }
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage(), e);
                mFaxEntity = new FaxEntity();
            }

        } else {
            mFaxEntity = new FaxEntity();
        }
        answerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dialogClose();
                sendButton.setEnabled(true);
                saveButton.setEnabled(true);
                Dialogs.showResultDialog(getActivity(), intent, true);
            }
        };
        app = (App) getActivity().getApplication();
    }


    private void handleExternalData(Intent intent) {
        Log.i(TAG, "intent = " + intent);
        ArrayList<Uri> files = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        for (Uri uri : files) {
            uriResolver(uri);
        }
    }

    private void uriResolver(Uri uri) {
        if (uri.getScheme().equals("content")) {
            addPath(uri);
        } else if (uri.getScheme().equals("file")) {
            addPath(uri.getPath());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fax_details, container, false);

        spinner = (Spinner) view.findViewById(R.id.countrySpinner);
        List<Country> countries = Countries.get().getCountries();
        CountryAdapter countryAdapter = new CountryAdapter(getActivity(), R.layout.item_country_code, countries);
        countryAdapter.setDropDownViewResource(R.layout.item_country_code);
        spinner.setAdapter(countryAdapter);
        if (mFaxEntity.getCode() == null) {
            int code = app.getCountry();
            if (code != 0) {
                spinner.setSelection(code);
            }
        } else {
            int position = Countries.get().getCountryPositionByCode(mFaxEntity.getCode());
            spinner.setSelection(position);
        }
        phoneNumberInput = (EditText) view.findViewById(R.id.phone_number_et);
        phoneNumberInput.setText(mFaxEntity.getPhoneNumber());
        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (phoneNumberInput.getText() != null && mFaxEntity.getPhoneNumber() != null) {
                    if (!mFaxEntity.getPhoneNumber().equals(phoneNumberInput.getText().toString())) {
                        contactNameLabel.setText(null);
                        mFaxEntity.setContactName(null);
                    }
                }
            }
        });
        contactNameLabel = (TextView) view.findViewById(R.id.contact_name);
        contactNameLabel.setText(mFaxEntity.getContactName());
        FloatingActionButton contactFab = (FloatingActionButton) view.findViewById(R.id.contactButton);
        contactFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, REQUEST_PICK_CONTACT);
            }
        });
        FloatingActionButton addButton = (FloatingActionButton) view.findViewById(R.id.add_fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
        ListView contentListView = (ListView) view.findViewById(R.id.contentListView);
        List<String> paths = Tools.getListFromString(mFaxEntity.getFilesPaths());
        contentAdapter = new ContentAdapter(getActivity(), R.layout.item_content, paths, new ContentAdapter.RemoveListener() {
            @Override
            public void removeItem(int position) {
                removePath(position);
            }
        });
        contentListView.setAdapter(contentAdapter);
        pagesCountValue = (TextView) view.findViewById(R.id.pages_count_value);
        app.setDefColors(pagesCountValue.getTextColors());
        updatePagesCountValue();

        saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNumberInput.getText().length() > 0) {
                    if (saveFax() != -1) {
                        Toast.makeText(getActivity(), R.string.save_message, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                } else {
                    FaxDetailsActivity activity = (FaxDetailsActivity) getActivity();
                    Snackbar.make(activity.getCoordinatorLayout(), R.string.need_data, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                ParseAnalytics.trackEventInBackground("saveClick");
            }
        });
        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contentAdapter.getCount() > 0 && phoneNumberInput.getText().length() > 0) {
                    final long faxId = saveFax();
                    if (faxId != -1) {
                        int pagesCount = Tools.getFaxPagesCount(mFaxEntity);
                        boolean sizeInLimit = Tools.inLimitFilesSize(mFaxEntity);
                        int limitPage = app.getAviablePagesCount();
                        if (app.isUnlimited()) {
                            limitPage = 200;
                        }
                        if (pagesCount > limitPage || Tools.getListFromString(mFaxEntity.getFilesPaths()).size() > 10 || !sizeInLimit) {
                            Dialogs.overLimitMessage(getActivity(), R.string.phaxio_limit);
                        } else {
                            app.getUserProfile().fetchInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {

                                    if (app.getUserProfile().isLocked()) {
                                        Dialogs.lockDialog(getActivity());
                                    } else {
                                        startFaxService(faxId);
                                    }
                                }
                            });

                        }
                    }
                } else {
                    FaxDetailsActivity activity = (FaxDetailsActivity) getActivity();
                    Snackbar.make(activity.getCoordinatorLayout(), R.string.need_data, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                ParseAnalytics.trackEventInBackground("sendClick");
            }
        });
        ImageButton orgButtons = (ImageButton) view.findViewById(R.id.orgsButton);
        orgButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FavoriteNumbersActivity.class);
                startActivityForResult(intent, REQUEST_PICK_FAVORITE);

            }
        });
        return view;
    }

    public void updatePagesCountValue() {
        int pagesCount = Tools.getFaxPagesCount(mFaxEntity);
        if (app.isUnlimited()) {
            pagesCountValue.setText(R.string.pages_count_unlim);
            getActivity().invalidateOptionsMenu();
        } else {
            if (app.getAviablePagesCount() > 0) {
                pagesCountValue.setText(String.format(getString(R.string.counter_label), pagesCount, app.getAviablePagesCount()));
            } else {
                pagesCountValue.setText(String.format(getString(R.string.counter_label), pagesCount, app.getAviablePagesCount()) + " " + getString(R.string.more_pages));
            }
            if (pagesCount > app.getAviablePagesCount()) {
                pagesCountValue.setTextColor(Color.RED);
                Dialogs.overLimitMessage(getActivity(), R.string.limit_message);
            } else {
                pagesCountValue.setTextColor(app.getDefColors());
            }
        }
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        if (app.isUnlimited()) {
            popupMenu.inflate(R.menu.popupmenu_unlimited);
        } else {
            popupMenu.inflate(R.menu.popupmenu);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.gallery) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, REQUEST_PICK_IMAGE);
                    return true;
                } else if (item.getItemId() == R.id.camera) {
                    getCameraPhoto();
                    return true;
                } else if (item.getItemId() == R.id.pdf) {
                    List<String> exts = new ArrayList<String>();
                    if (app.isUnlimited()) {
                        exts.add("doc");
                        exts.add("docx");
                        exts.add("pdf");
                    } else {
                        exts.add("pdf");
                    }
                    FileChooser fileChooser = new FileChooser(getActivity());
                    fileChooser.setFileListener(FaxDetailsFragment.this);
                    fileChooser.setExtensions(exts);
                    fileChooser.showDialog();
                    return true;
                } else if (item.getItemId() == R.id.text) {
                    Intent intent = new Intent(getActivity(), TextActivity.class);
                    startActivityForResult(intent, REQUEST_TXT);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void getCameraPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void startFaxService(long faxId) {
        NotifyAlarm mNotifyAlarm = new NotifyAlarm();
        mNotifyAlarm.cancelAlarm(getActivity());
        mNotifyAlarm.setAlarm(getActivity());

        dialogShow(R.string.fax_sending);
        sendButton.setEnabled(false);
        saveButton.setEnabled(false);
        Intent serviceIntent = new Intent(getActivity(), NetworkService.class);
        serviceIntent.putExtra(FAX_ID, faxId);
        serviceIntent.setAction(NetworkService.ACTION_SEND);
        getActivity().startService(serviceIntent);
    }

    public long saveFax() {
        Country country = (Country) spinner.getSelectedItem();
        mFaxEntity.setCode(country.getCode());
        mFaxEntity.setPhoneNumber(phoneNumberInput.getText().toString());
        Date date = new Date();
        if (mFaxEntity.getCreateDate() == null) {
            mFaxEntity.setCreateDate(date);
        }
        mFaxEntity.setUpdateDate(date);
        mFaxEntity.setStatus(0);
        try {
            HelperFactory.getHelper().getFaxEntityDao().createOrUpdate(mFaxEntity);
            return mFaxEntity.getId();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getActivity(), R.string.error_saving_fax, Toast.LENGTH_LONG).show();
            return -1;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivity fragment handle: " + requestCode + " " + resultCode + " " + data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                addPath(mCurrentPhotoPath);
            }
        } else if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                addPath(selectedImage);
            }
        } else if (requestCode == REQUEST_PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactUri = data.getData();
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = getActivity().getContentResolver().query(contactUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String phoneNumber = cursor.getString(numberIndex);
                    String contactName = cursor.getString(nameIndex);
                    String selectedNumber = phoneNumber.replace("-", "").replace(" ", "");
                    phoneNumberInput.setText(selectedNumber);
                    mFaxEntity.setContactName(contactName);
                    mFaxEntity.setPhoneNumber(selectedNumber);
                    contactNameLabel.setText(contactName);
                    cursor.close();
                }
            }
        } else if (requestCode == REQUEST_TXT) {
            if (resultCode == Activity.RESULT_OK) {
                String path = data.getStringExtra("path");
                if (path != null) {
                    addPath(path);
                }
            }
        } else if (requestCode == REQUEST_PICK_FAVORITE) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra("name");
                String value = data.getStringExtra("value");
                phoneNumberInput.setText(value);
                mFaxEntity.setContactName(name);
                mFaxEntity.setPhoneNumber(value);
                contactNameLabel.setText(name);
            }
        }
    }

    public void addPath(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        addPath(filePath);
        cursor.close();
    }

    public void addPath(String filePath) {
        List<String> paths = Tools.getListFromString(mFaxEntity.getFilesPaths());
        paths.add(filePath);
        mFaxEntity.setFilesPaths(Tools.getStringFromList(paths));
        contentAdapter.addPath(filePath);
        updatePagesCountValue();
    }

    public void removePath(int position) {
        List<String> paths = Tools.getListFromString(mFaxEntity.getFilesPaths());
        if (paths.size() > 0) {
            paths.remove(position);
            mFaxEntity.setFilesPaths(Tools.getStringFromList(paths));
            contentAdapter.removePath(position);
            updatePagesCountValue();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void fileSelected(File file) {
        addPath(file.getAbsolutePath());
    }

    public void dialogShow(int message) {
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(message));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dialogClose() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(NetworkService.ACTION_SEND);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(answerReceiver, intentFilter);


        // Get intent, action and MIME type
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.getAction() != null && intent.getType() != null) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                handleExternalData(intent);
            } else if (Intent.ACTION_SEND.equals(action)) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                uriResolver(imageUri);
            }
            getActivity().setIntent(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(answerReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), PreferencesActivity.class);
            startActivity(intent);
        } else if (id==R.id.purchases){
            Intent intent = new Intent(getActivity(), PurchasesActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
