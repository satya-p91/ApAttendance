package online.forgottenbit.attendance1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class StudentDashBoard extends AppCompatActivity {

    studentDB sDB;
    String sDetails;

    Button markAttendance;

    Timer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dash_board);

        sDB = new studentDB(StudentDashBoard.this);

        checkCredentials();

        myTimer = new Timer();

        markAttendance = findViewById(R.id.btnMarkAttendance);

        markAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(StudentDashBoard.this);
                dialog.setMessage("Connecting with teacher's device around you");
                dialog.setCancelable(false);
                dialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        myTimer.cancel();

                    }
                });

                dialog.show();

                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.cancel();
                        TimerMethod();
                    }
                },2500);




            }
        });
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.

            //Do something to the UI thread here

            markAttendceF();

        }
    };

    public void markAttendceF(){
        new AlertDialog.Builder(StudentDashBoard.this)
                .setCancelable(false)
                .setTitle("Mark Attendance")
                .setMessage(Html.fromHtml("Teacher <b>XYZ</b> is marking attendance for <b>DA7211</b>."))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dash_board_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.logout:

                new AlertDialog.Builder(StudentDashBoard.this)
                        .setCancelable(false)
                        .setTitle("Are you sure you want to logout?")
                        .setMessage("Once you log out you have to re-verify your device by teacher to mark attendance.")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sDB.clearTable();
                                startActivity(new Intent(StudentDashBoard.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

            case R.id.sInfo:
                Cursor c = sDB.getSRegDetails();
                if(c!=null && c.getCount()>0){
                    c.moveToNext();
                    sDetails = "<b>Name : </b>" + c.getString(0) + "<br>" + "<b>Roll : </b>" + c.getString(1) + "<br>" +
                            "<b>SAP ID : </b>" + c.getString(2) + "<br>" + "<b>Email : </b>" + c.getString(3) + "<br>" + "<b>Mobile : </b>" + c.getString(4) +
                            "<br>" + "<b>Course : </b>" + c.getString(5) + "<br>" + "<b>Branch and Section : </b>" + c.getString(6)+"<br>"+"<b>Semester : <b>"+c.getString(7);
                }else{
                    Toast.makeText(this, "Something went wrong. Please logout and login again.", Toast.LENGTH_SHORT).show();
                }

                new AlertDialog.Builder(StudentDashBoard.this)
                        .setTitle("Your details are...")
                        .setMessage(Html.fromHtml(sDetails))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    public  void checkCredentials(){
        Cursor c = sDB.getSRegDetails();

        if(c==null || c.getCount()<=0){

            new AlertDialog.Builder(StudentDashBoard.this)
                    .setCancelable(false)
                    .setTitle("FATAL ERROR :(")
                    .setMessage("You have somehow erased this application data and lost credentials. You have to login again...")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sDB.clearTable();
                            startActivity(new Intent(StudentDashBoard.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    }).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sDB.close();
    }
}

