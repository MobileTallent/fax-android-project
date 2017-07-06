package co.faxapp.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import co.faxapp.R;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class TextFragment extends Fragment {
    private static final String TAG = TextFragment.class.getName();
    private EditText editText;
    private TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        editText = (EditText) view.findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editText.getText().toString().length();
                int result = 2000-length;
                mTextView.setText(getText(R.string.characters)+" "+result);

            }
        });
        mTextView = (TextView)view.findViewById(R.id.counter);
        return view;
    }

    public String saveText() {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            return null;
        }
        try {
            String fileName = ""+ new Date().getTime();
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/faxapp/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            final File myFile = new File(dir, fileName + ".txt");

            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(myFile);

            fos.write(text.getBytes());
            fos.close();
//            Log.i(TAG,"count in txt="+ Tools.getTxtPages(myFile.getAbsolutePath()));
            return myFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG,e.getMessage(),e);
        }
        return null;
    }
}
