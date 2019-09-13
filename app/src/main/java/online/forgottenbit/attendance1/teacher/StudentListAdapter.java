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

public class StudentListAdapter extends BaseAdapter {


    private final Activity context;
    private final ArrayList<AddStudent.StudentData> id;


    public StudentListAdapter(Activity context, ArrayList<AddStudent.StudentData> id) {
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
        View rowView= inflater.inflate(R.layout.student_list_items, null, true);

        TextView textView = rowView.findViewById(R.id.studentName);
        TextView textViewCode = rowView.findViewById(R.id.studentEmail);

        textView.setText(Html.fromHtml("<b>Name : </b>"+id.get(position).getName()));
        textView.append("     "+Html.fromHtml("<b>Roll : </b>"+id.get(position).getRoll()));


        textViewCode.setText(Html.fromHtml("<b>Email : </b>"+id.get(position).getEmail()));
        textViewCode.append("     "+Html.fromHtml("<b>Mob : </b>"+id.get(position).getMob()));
        return rowView;

    }
}