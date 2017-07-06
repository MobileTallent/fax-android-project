package co.faxapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.faxapp.R;
import co.faxapp.db.HelperFactory;
import co.faxapp.db.OrmliteCursorAdapter;
import co.faxapp.model.FaxEntity;
import co.faxapp.util.Log;
import co.faxapp.util.Tools;

public class FaxesAdapter extends OrmliteCursorAdapter<FaxEntity> {
    private static String TAG = FaxesAdapter.class.getSimpleName();
    private Context mContext;

    public FaxesAdapter(Context context, Cursor c, PreparedQuery<FaxEntity> query) {
        super(context, c, query);
        mContext = context;
    }

    @Override
    public void bindView(View itemView, Context context, FaxEntity item) {
        ViewHolder viewHolder;
        if (null == itemView.getTag()) {
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) itemView.findViewById(R.id.imageStatus);
            viewHolder.contactNameTv = (TextView) itemView.findViewById(R.id.contactNameTv);
            viewHolder.phoneNumberTv = (TextView) itemView.findViewById(R.id.phoneNumberTv);
            viewHolder.itemsCountTv = (TextView) itemView.findViewById(R.id.itemsCountTv);
            viewHolder.sendDateTv = (TextView) itemView.findViewById(R.id.sendDateTv);
            viewHolder.statusLabel = (TextView)itemView.findViewById(R.id.statusLabel);
            itemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) itemView.getTag();
        }
        Date sendDate = item.getSendDate();
        if (item.getStatus()==2) {
            viewHolder.statusLabel.setText(R.string.status_success);
            viewHolder.statusLabel.setTextColor(context.getResources().getColor(R.color.green));
            viewHolder.mImageView.setImageResource(R.drawable.ic_icon_fax_grean);
        } else if (item.getStatus()==3) {
            viewHolder.statusLabel.setText(R.string.status_failure);
            viewHolder.statusLabel.setTextColor(Color.RED);
            viewHolder.mImageView.setImageResource(R.drawable.ic_icon_fax_red);
        } else if (item.getStatus()==0) {
            viewHolder.statusLabel.setTextColor(Color.GRAY);
            viewHolder.statusLabel.setText(R.string.status_created);
            viewHolder.mImageView.setImageResource(R.drawable.ic_icon_fax_gray);
        } else  if (item.getStatus()==1) {
            viewHolder.statusLabel.setTextColor(Color.BLUE);
            viewHolder.statusLabel.setText(R.string.status_inprogress);
            viewHolder.mImageView.setImageResource(R.drawable.ic_icon_fax_blue);
        }
        if (sendDate != null) {
            viewHolder.sendDateTv.setText(SimpleDateFormat.getDateTimeInstance().format(sendDate));
        } else {
            viewHolder.sendDateTv.setText(null);
        }
        String contactName = item.getContactName();
        String phoneNumber = item.getPhoneNumber();
        if (phoneNumber != null && phoneNumber.length() > 0) {
            viewHolder.phoneNumberTv.setText(item.getPhoneNumber());
        } else {
            viewHolder.phoneNumberTv.setText(null);
        }
        if (contactName != null && contactName.length() > 0) {
            viewHolder.contactNameTv.setText(item.getContactName());
        } else {
            viewHolder.contactNameTv.setText(null);
        }
        int value = Tools.getFaxPagesCount(item);
        viewHolder.itemsCountTv.setText(mContext.getString(R.string.pages_count) + value);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_fax, parent, false);
    }

    public void remove(int position) {
        long faxId = getItemId(position);
        try {
            HelperFactory.getHelper().getFaxEntityDao().deleteById(faxId);
            notifyDataSetChanged();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    static class ViewHolder {
        ImageView mImageView;
        TextView contactNameTv;
        TextView phoneNumberTv;
        TextView itemsCountTv;
        TextView sendDateTv;
        TextView statusLabel;
    }
}
