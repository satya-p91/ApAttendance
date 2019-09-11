package online.forgottenbit.attendance1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class StudentVerification extends AppCompatActivity {


    final int port = 8185;
    Button verify, reRegister;
    studentDB sDB;
    String TEACHER_IP;
    Dialog dialogTAuth;
    View view;
    Socket socket;

    String sendsDetailsString = null;
    ProgressDialog dialog;
    OpenSocketConnection socketConnectionThread = null;
    private PrintWriter output;
    private BufferedReader input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_verification);

        verify = findViewById(R.id.btnVerify);
        reRegister = findViewById(R.id.btnRegister);

        sDB = new studentDB(StudentVerification.this);

        Cursor ss = sDB.getSRegDetails();

        ss.moveToNext();

        TextView name = findViewById(R.id.textName);
        name.setText(Html.fromHtml("<b>Name : </b>" + ss.getString(0)));

        TextView roll = findViewById(R.id.textRoll);
        roll.setText(Html.fromHtml("<b>Roll : </b>" + ss.getString(1)));

        TextView sap = findViewById(R.id.textSap);
        sap.setText(Html.fromHtml("<b>SAP ID : </b>" + ss.getString(2)));

        TextView course = findViewById(R.id.textCourse);
        course.setText(Html.fromHtml("<b>Course : </b>" + ss.getString(4)));

        TextView branch = findViewById(R.id.textBranch);
        branch.setText(Html.fromHtml("<b>Branch & Section : </b>" + ss.getString(5)));

        TextView sem = findViewById(R.id.textSem);
        sem.setText(Html.fromHtml("<b>Semester : </b>" + ss.getString(7)));

        TextView email = findViewById(R.id.textEmail);
        email.setText(Html.fromHtml("<b>Email : </b>" + ss.getString(3)));

        TextView mob = findViewById(R.id.textMob);
        mob.setText(Html.fromHtml("<b>Mobile : </b>" + ss.getString(6)));

        sendsDetailsString = ss.getString(0) + "#" + ss.getString(1) + "#" + ss.getString(2) + "#" + ss.getString(3) + "#" + ss.getString(4)
                + "#" + ss.getString(5) + "#" + ss.getString(6) + "#" + ss.getString(7) + "#" + ss.getString(8) + "#";

        Log.e("StudendDetails", sendsDetailsString);


        reRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentVerification.this, StudentRegistration.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                sDB.clearTable();
            }
        });


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogTAuth = new Dialog(StudentVerification.this);
                LayoutInflater inflater = StudentVerification.this.getLayoutInflater();
                view = inflater.inflate(R.layout.custom_student_side_verification_ip_input, null);
                dialogTAuth.setContentView(view);
                dialogTAuth.show();

                view.findViewById(R.id.ip_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText ee = view.findViewById(R.id.teacher_ip);
                        TEACHER_IP = ee.getText().toString().trim();

                        Toast.makeText(StudentVerification.this, "inside click", Toast.LENGTH_SHORT).show();
                        if (!TEACHER_IP.isEmpty()) {
                            Log.e("inside ", " if");
                            startVerification(sendsDetailsString);
                        } else {
                            Toast.makeText(StudentVerification.this, "Enter IP address", Toast.LENGTH_SHORT).show();
                        }

                        dialogTAuth.cancel();
                    }
                });


            }
        });

    }

    private void startVerification(String message) {
        Log.e("inside ", " method");
        socketConnectionThread = new OpenSocketConnection(message);
        socketConnectionThread.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sDB.close();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socketConnectionThread != null) {
            socketConnectionThread.cancel(true);
        }

    }

    class OpenSocketConnection extends AsyncTask<Void, Void, Void> {


        String message;


        OpenSocketConnection(String message) {
            Log.e("inside ", " thread constructor");
            this.message = message;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(StudentVerification.this);
            dialog.setMessage("Processing");
            dialog.show();
        }


        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Log.e("inside ", " thread do in background");
                socket = new Socket(TEACHER_IP, port);
                socket.setKeepAlive(true);

                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StudentVerification.this, "Connection Established", Toast.LENGTH_LONG).show();
                        Log.e(StudentVerification.class.getSimpleName(), "Connection Established");
                    }
                });

                output.write(message + "\n");
                output.flush();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StudentVerification.this, "data sending to server...", Toast.LENGTH_SHORT).show();
                    }
                });


                new Thread(new ReceiveFromServer()).start();

                return null;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("inside ", " thread post execute");
            super.onPostExecute(aVoid);

        }
    }

    class ReceiveFromServer implements Runnable {
        @Override
        public void run() {
            try {
                final String receivedMessage = input.readLine();

                if (!receivedMessage.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (receivedMessage.equals("teacherConfirm8185")) {

                                boolean b = sDB.updateSVerification();
                                Toast.makeText(StudentVerification.this, "Verified", Toast.LENGTH_SHORT).show();
                                Log.e(StudentVerification.class.getSimpleName(), "  " + b + "  ");

                                if(output!=null){
                                    output.close();
                                }

                                if(input!=null){
                                    try {
                                        input.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (socket != null) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                dialog.cancel();

                                startActivity(new Intent(StudentVerification.this, StudentDashBoard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            } else {
                                Toast.makeText(StudentVerification.this, "Not valid message reveived : " + receivedMessage, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
