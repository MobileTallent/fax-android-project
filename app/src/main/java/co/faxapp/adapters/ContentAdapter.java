package co.faxapp.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import co.faxapp.R;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class ContentAdapter extends ArrayAdapter<String> {
    private static final String TAG = ContentAdapter.class.getSimpleName();
    private Context mContext;
    private int resView;
    private List<String> items;
    private RemoveListener mRemoveListener;

    public ContentAdapter(Context context, int resource, List<String> objects, RemoveListener removeListener) {
        super(context, resource, objects);
        mContext = context;
        resView = resource;
        items = objects;
        mRemoveListener = removeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final ViewHolder holder;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resView, null);
            holder = new ViewHolder();
            holder.mImageView = (ImageView) v.findViewById(R.id.imageView);
            holder.mImageView.setTag(position);
            holder.deleteButton = (ImageButton) v.findViewById(R.id.deleteButton);
            holder.fileName = (TextView) v.findViewById(R.id.file_name);
            holder.pagesCount = (TextView) v.findViewById(R.id.count_value);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.position = position;
        String path = items.get(position);
        File file = new File(path);
        String ext = Tools.fileExt(path).substring(1);
        if (ext.toLowerCase().equals("jpeg")) {
            ext = "jpg";
        } else if (ext.toLowerCase().equals("docx")) {
            ext="doc";
        }
        if (ext.equals("txt")) {
            try {
                FileInputStream in = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine().trim();
                if (line.length()>16) {
                    line=line.substring(0,15)+"...";
                }
                holder.fileName.setText(line);
            } catch (IOException e) {
                holder.fileName.setText(file.getName());
                Log.e(TAG,e.getMessage(),e);
            }
        } else {
            holder.fileName.setText(file.getName());
        }
        int resID = mContext.getResources().getIdentifier(ext.toLowerCase(), "drawable", mContext.getPackageName());
        if (resID > 0) {
            holder.mImageView.setImageResource(resID);
        } else {
            holder.mImageView.setImageResource(R.drawable._blank);
        }
        if (ext.toLowerCase().equals("pdf")) {
            holder.pagesCount.setText(Integer.toString(Tools.getPdfPagesCount(path)));
        } else if (ext.toLowerCase().equals("doc")){
            holder.pagesCount.setText(R.string.undefind);
        } else {
            holder.pagesCount.setText("1");
        }
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = items.get(holder.position);
                openFile(path);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoveListener.removeItem(holder.position);
            }
        });
        return v;
    }

    public void addPath(String s) {
        items.add(s);
        notifyDataSetChanged();
    }

    public void removePath(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        ImageView mImageView;
        ImageButton deleteButton;
        TextView fileName;
        TextView pagesCount;
        int position;
    }

    public void openFile(String path) {
        try {
            File fileForShow = new File(path);
            String ext = Tools.fileExt(fileForShow.getName()).substring(1);
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            String mimeType = myMime.getMimeTypeFromExtension(ext);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(fileForShow), mimeType);
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            Toast.makeText(mContext, R.string.not_open_file, Toast.LENGTH_LONG).show();
        }
    }

    public interface RemoveListener {
        void removeItem(int position);
    }
}
