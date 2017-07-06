package co.faxapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQueryAdapter;

import co.faxapp.R;
import co.faxapp.model.SavedNumber;

public class SavedNumbersAdapter extends ParseQueryAdapter<SavedNumber> {

    private Context mContext;
    private int mResource;

    public SavedNumbersAdapter(Context context, QueryFactory<SavedNumber> queryFactory, int itemViewResource) {
        super(context, queryFactory, itemViewResource);
        this.mContext = context;
        this.mResource = itemViewResource;
    }


    static class ViewHolder {
        TextView name;
        TextView value;
    }

    @Override
    public View getItemView(SavedNumber item, View v, ViewGroup parent) {
        final ViewHolder holder;
        if (v == null) {
            v = View.inflate(mContext, mResource, null);
            holder = new ViewHolder();
            holder.name = (TextView) v.findViewById(R.id.nameTv);
            holder.value = (TextView) v.findViewById(R.id.numberTv);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.value.setText(item.getNumber());
        holder.name.setText(item.getName());
        return v;
    }
}
