package online.forgottenbit.attendance1.teacher;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import online.forgottenbit.attendance1.R;

public class SubjectAdapter  extends BaseAdapter {


    private final Activity context;
    private final ArrayList<BatchActivity.SubjectData> id;


    public SubjectAdapter(Activity context, ArrayList<BatchActivity.SubjectData> id) {
        this.context = context;
        this.id = id;
    }

    @Override
    public int getCount() {
        return id.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.subject_list_item, null, true);

        TextView textView = rowView.findViewById(R.id.s_name);
        TextView textViewCode = rowView.findViewById(R.id.s_code);

        textView.setText(Html.fromHtml("<b>Subject code : </b>"+id.get(position).getName()));
        textViewCode.setText(Html.fromHtml("<b>Subject code : </b>"+id.get(position).getCode()));
        return rowView;

    }
}