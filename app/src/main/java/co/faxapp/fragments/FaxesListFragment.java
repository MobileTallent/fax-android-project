package co.faxapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.stmt.PreparedQuery;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.faxapp.FaxDetailsActivity;
import co.faxapp.R;
import co.faxapp.ServiceReceiver;
import co.faxapp.adapters.FaxesAdapter;
import co.faxapp.db.DatabaseHelper;
import co.faxapp.db.FaxEntityDao;
import co.faxapp.db.HelperFactory;
import co.faxapp.dialogs.Dialogs;
import co.faxapp.model.FaxEntity;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class FaxesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemClickListener {
    private static String TAG = FaxesListFragment.class.getSimpleName();
    private int URL_LOADER = 0;
    private ListView mListView;
    private FaxEntityDao mFaxEntityDao;
    private FaxesAdapter mFaxesAdapter;
    private BroadcastReceiver answerReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity().getApplicationContext());
        mFaxesAdapter = new FaxesAdapter(getActivity().getApplicationContext(), null, null);
        try {
            mFaxEntityDao = new FaxEntityDao(dbHelper.getConnectionSource(), FaxEntity.class);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        getActivity().getSupportLoaderManager().initLoader(URL_LOADER, null, this);
        answerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive");
                getLoaderManager().restartLoader(URL_LOADER, null, FaxesListFragment.this);
                Dialogs.showResultDialog(getActivity(), intent, false);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faxes_list, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mFaxesAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int i, long l, boolean b) {
                int selectedCount = mListView.getCheckedItemCount();
                setSubtitle(mode, selectedCount);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.fax_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                List<Long> list = getSelectedFaxes();
                for (Long faxId : list) {
                    try {
                        FaxEntity entity = HelperFactory.getHelper().getFaxEntityDao().queryForId(faxId);
                        List<String> paths = Tools.getListFromString(entity.getFilesPaths());
                        for (String path : paths) {
                            if (path.endsWith(".txt")) {
                                File file = new File(path);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                        HelperFactory.getHelper().getFaxEntityDao().deleteById(faxId);
                    } catch (SQLException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                getLoaderManager().restartLoader(URL_LOADER, null, FaxesListFragment.this);
                actionMode.finish();
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }

            private void setSubtitle(ActionMode mode, int selectedCount) {
                switch (selectedCount) {
                    case 0:
                        mode.setSubtitle(null);
                        break;
                    default:
                        mode.setTitle(getString(R.string.selected) + " " + String.valueOf(selectedCount));
                        break;
                }
            }

            private List<Long> getSelectedFaxes() {
                List<Long> selectedApps = new ArrayList<>();
                SparseBooleanArray sparseBooleanArray = mListView.getCheckedItemPositions();
                for (int i = 0; i < sparseBooleanArray.size(); i++) {
                    if (sparseBooleanArray.valueAt(i)) {
                        long faxId = mFaxesAdapter.getItemId(sparseBooleanArray.keyAt(i));
                        selectedApps.add(faxId);
                    }
                }
                return selectedApps;
            }
        });
        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        try {
            PreparedQuery<FaxEntity> query = this.mFaxEntityDao.getQuery();
            return mFaxEntityDao.getSQLCursorLoader(getActivity().getApplicationContext(), query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mFaxesAdapter.swapCursor(cursor);
        try {
            mFaxesAdapter.setQuery(mFaxEntityDao.getQuery());
            mListView.invalidateViews();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFaxesAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        long faxId = mFaxesAdapter.getItemId(position);
        Intent intent = new Intent(getActivity(), FaxDetailsActivity.class);
        intent.putExtra("faxId", faxId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(URL_LOADER, null, FaxesListFragment.this);
        IntentFilter intentFilter = new IntentFilter(ServiceReceiver.RESULT_SEND);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        getActivity().registerReceiver(answerReceiver, intentFilter);
        checkSuccessAndNeedRate();
    }

    @Override
    public void onPause() {
        super.onPause();
        getLoaderManager().destroyLoader(URL_LOADER);
        getActivity().unregisterReceiver(answerReceiver);
    }

    public void checkSuccessAndNeedRate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean success = sharedPref.getBoolean("success", false);
        boolean rated = sharedPref.getBoolean("rated",false);
        long lastAskDate = sharedPref.getLong("lastAsk",0);
        if (success) {
            if (!rated) {
                if (lastAskDate == 0) {
                    //show dialog
                    Dialogs.showRateDialog(getActivity());
                } else {
                    long now = System.currentTimeMillis();
                    if (Math.abs((now-lastAskDate)/(1000*60*60*24))>=3) {
                        //show dialog
                        Dialogs.showRateDialog(getActivity());
                    }
                }
            }
        }
    }
}
