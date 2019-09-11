package online.forgottenbit.attendance1.teacher;

import android.app.Activity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import online.forgottenbit.attendance1.R;

public class BatchListAdapter extends BaseAdapter {


    private final Activity context;
    private final ArrayList<TeacherDashboard.BatchData> id;


    public BatchListAdapter(Activity context, ArrayList<TeacherDashboard.BatchData> id) {
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
        View rowView= inflater.inflate(R.layout.batch_list_item, null, true);

        TextView textView = rowView.findViewById(R.id.b_name);

        textView.setText(id.get(position).getName());
        return rowView;

    }
}
