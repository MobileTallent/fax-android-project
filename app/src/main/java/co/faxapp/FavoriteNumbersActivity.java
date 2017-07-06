package co.faxapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.DeleteCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import co.faxapp.adapters.SavedNumbersAdapter;
import co.faxapp.model.SavedNumber;

public class FavoriteNumbersActivity extends AppCompatActivity implements ListView.OnItemClickListener {
    private static String TAG = FavoriteNumbersActivity.class.getSimpleName();
    private EditText captionEt;
    private EditText numberEt;
    private ListView numbersLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_numbers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        captionEt = (EditText) findViewById(R.id.captionNameEt);
        numberEt = (EditText) findViewById(R.id.numberEt);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_fab);
        numbersLv = (ListView) findViewById(R.id.numbersListView);
        ParseQueryAdapter.QueryFactory<SavedNumber> factory = new ParseQueryAdapter.QueryFactory<SavedNumber>() {
            public ParseQuery<SavedNumber> create() {
                ParseQuery<SavedNumber> query = SavedNumber.getQuery();
                query.orderByAscending("name");
                query.whereEqualTo("user", ParseUser.getCurrentUser());
                return query;
            }
        };

        final SavedNumbersAdapter adapter = new SavedNumbersAdapter(this, factory, R.layout.item_favorite);
        numbersLv.setAdapter(adapter);
        numbersLv.setOnItemClickListener(this);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = captionEt.getText().toString();
                String value = numberEt.getText().toString();
                if (!name.isEmpty() && !value.isEmpty()) {
                    SavedNumber favoriteNumber = new SavedNumber();
                    favoriteNumber.setName(name);
                    favoriteNumber.setNumber(value);
                    favoriteNumber.setUser(ParseUser.getCurrentUser());
                    favoriteNumber.setACL(new ParseACL(ParseUser.getCurrentUser()));
                    favoriteNumber.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            adapter.loadObjects();
                        }
                    });
                    adapter.notifyDataSetChanged();
                    captionEt.setText(null);
                    numberEt.setText(null);
                }
            }
        });

        numbersLv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        numbersLv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int i, long l, boolean b) {
                int selectedCount = numbersLv.getCheckedItemCount();
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
                List<SavedNumber> list = getSelectedNumbers();
                for (SavedNumber number : list) {
                    number.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            adapter.loadObjects();
                        }
                    });
                }
                adapter.loadObjects();
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

            private List<SavedNumber> getSelectedNumbers() {
                List<SavedNumber> selecteNumbers = new ArrayList<>();
                SparseBooleanArray sparseBooleanArray = numbersLv.getCheckedItemPositions();
                for (int i = 0; i < sparseBooleanArray.size(); i++) {
                    if (sparseBooleanArray.valueAt(i)) {
                        SavedNumber favoriteNumber = (SavedNumber) numbersLv.getItemAtPosition(sparseBooleanArray.keyAt(i));
                        selecteNumbers.add(favoriteNumber);
                    }
                }
                return selecteNumbers;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        SavedNumber favoriteNumber = (SavedNumber) adapterView.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra("name", favoriteNumber.getName());
        intent.putExtra("value", favoriteNumber.getNumber());
        setResult(RESULT_OK, intent);
        finish();
    }
}
