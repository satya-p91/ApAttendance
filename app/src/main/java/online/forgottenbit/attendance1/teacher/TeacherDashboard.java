package online.forgottenbit.attendance1.teacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import online.forgottenbit.attendance1.MainActivity;
import online.forgottenbit.attendance1.R;
import online.forgottenbit.attendance1.StudentDashBoard;

public class TeacherDashboard extends AppCompatActivity {

    TeacherDB tDB;

    ListView batchList;
    BatchListAdapter adapter;
    ArrayList<BatchData> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        tDB = new TeacherDB(TeacherDashboard.this);

        batchList = findViewById(R.id.batch_list);

        list = new ArrayList<>();

        Cursor ss = tDB.getAllBatch();
        if(ss!=null && ss.getCount()>0){

            while (ss.moveToNext()){
                list.add(new BatchData(ss.getInt(0),ss.getString(1)));
            }
        }else{
            Toast.makeText(this, "No batch found. Add batch.", Toast.LENGTH_SHORT).show();
        }

        adapter = new BatchListAdapter(TeacherDashboard.this, list);

        batchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(TeacherDashboard.this, BatchActivity.class);
                i.putExtra("name",list.get(position).getName());
                i.putExtra("id",list.get(position).getId());
                startActivity(i);
            }
        });
        batchList.setAdapter(adapter);




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.teacher_dashboard_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.teacher_logout:

                new AlertDialog.Builder(TeacherDashboard.this)
                        .setCancelable(false)
                        .setTitle("Are you sure you want to logout?")
                        .setMessage("Once you log out you will lose all app data...")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tDB.clearTable();
                                startActivity(new Intent(TeacherDashboard.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;

            case R.id.add_batch:
                addBatch();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }


    class BatchData{
        private int id;
        private  String name;

        public BatchData(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public void addBatch(){

        LayoutInflater inflater = TeacherDashboard.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.batch_add_custom_dialog,null);

        final Dialog dialogAddBatch = new Dialog(TeacherDashboard.this);
        dialogAddBatch.setContentView(view);

        final EditText name = view.findViewById(R.id.batch_name);
        Button cancel = view.findViewById(R.id.cancel);
        Button add = view.findViewById(R.id.add);

        dialogAddBatch.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddBatch.cancel();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().trim()!=null && !name.getText().toString().trim().isEmpty()){
                    tDB.insertBatch(name.getText().toString().trim());

                    //notify data set changes in adapter

                    Cursor ss = tDB.getAllBatch();
                    if(ss!=null && ss.getCount()>0){
                        if(list!=null && list.size()>0){
                            list.clear();
                        }
                        while (ss.moveToNext()){
                            list.add(new BatchData(ss.getInt(0),ss.getString(1)));
                        }
                    }else{
                        Toast.makeText(TeacherDashboard.this, "No batch found. Add batch.", Toast.LENGTH_SHORT).show();
                    }

                    adapter = new BatchListAdapter(TeacherDashboard.this, list);
                    adapter.notifyDataSetChanged();

                    dialogAddBatch.cancel();
                }
            }
        });
    }
}
