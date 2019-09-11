package online.forgottenbit.attendance1.teacher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import online.forgottenbit.attendance1.R;
import online.forgottenbit.attendance1.StudentVerification;

public class AddStudent extends AppCompatActivity {

    int batch_id;



    ListView studentList;
    ArrayList<StudentData> list;


    FloatingActionButton addNewStudent;
    SwipeRefreshLayout swipeRefreshLayout;

    TeacherDB tDb;

    StudentListAdapter studentListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        batch_id = getIntent().getIntExtra("batch_id",0);

        studentList = findViewById(R.id.studentListAdd);

        addNewStudent = findViewById(R.id.addNewStudent);
        addNewStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddStudent.this, ReceiveStudentData.class).putExtra("batch_id",batch_id));
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        tDb = new TeacherDB(AddStudent.this);

        list = new ArrayList<>();

        Cursor ss = tDb.getSubjectByBatch(batch_id);
        if(ss!=null && ss.getCount()>0){
            if(list.size()>0){
                list.clear();
            }
            while (ss.moveToNext()){
                list.add(new StudentData(ss.getString(0),ss.getString(1),ss.getString(2),ss.getString(3)
                        ,ss.getString(4),ss.getString(5),ss.getString(6),ss.getString(7),ss.getString(8)
                        ,ss.getString(9)));
            }
        }else{
            Toast.makeText(this, "No Student found. Add student.", Toast.LENGTH_SHORT).show();
        }


        studentListAdapter = new StudentListAdapter(AddStudent.this, list);
        studentList.setAdapter(studentListAdapter);


    }


    private void refreshLayout(){
        list = new ArrayList<>();

        Cursor ss = tDb.getSubjectByBatch(batch_id);
        if(ss!=null && ss.getCount()>0){
            if(list.size()>0){
                list.clear();
            }
            while (ss.moveToNext()){
                list.add(new StudentData(ss.getString(0),ss.getString(1),ss.getString(2),ss.getString(3)
                        ,ss.getString(4),ss.getString(5),ss.getString(6),ss.getString(7),ss.getString(8)
                        ,ss.getString(9)));
            }
        }else{
            Toast.makeText(this, "No Student found. Add student.", Toast.LENGTH_SHORT).show();
        }

        studentListAdapter = new StudentListAdapter(AddStudent.this, list);
        studentListAdapter.notifyDataSetChanged();
    }


    class StudentData{
        String name,roll,sap,email,course,batchSec,mob,sem,imei,batch_id;

        public StudentData(String name, String roll, String sap, String email, String course, String batchSec, String mob, String sem, String imei, String batch_id) {
            this.name = name;
            this.roll = roll;
            this.sap = sap;
            this.email = email;
            this.course = course;
            this.batchSec = batchSec;
            this.mob = mob;
            this.sem = sem;
            this.imei = imei;
            this.batch_id = batch_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoll() {
            return roll;
        }

        public void setRoll(String roll) {
            this.roll = roll;
        }

        public String getSap() {
            return sap;
        }

        public void setSap(String sap) {
            this.sap = sap;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public String getBatchSec() {
            return batchSec;
        }

        public void setBatchSec(String batchSec) {
            this.batchSec = batchSec;
        }

        public String getMob() {
            return mob;
        }

        public void setMob(String mob) {
            this.mob = mob;
        }

        public String getSem() {
            return sem;
        }

        public void setSem(String sem) {
            this.sem = sem;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getBatch_id() {
            return batch_id;
        }

        public void setBatch_id(String batch_id) {
            this.batch_id = batch_id;
        }
    }



}
