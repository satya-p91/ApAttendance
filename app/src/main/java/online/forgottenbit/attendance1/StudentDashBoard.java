package online.forgottenbit.attendance1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

import online.forgottenbit.attendance1.LanConnection.CloseHardwares;

public class StudentDashBoard extends AppCompatActivity {

    studentDB sDB;
    String sDetails;

    Button markAttendance;

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    String teacherID,myName;


    String sendsDetailsString = null;
    ProgressDialog dialog;

    ConnectionsClient connectionsClient;

    CloseHardwares closeHardwares;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dash_board);

        sDB = new studentDB(StudentDashBoard.this);

        checkCredentials();

        markAttendance = findViewById(R.id.btnMarkAttendance);

        connectionsClient = Nearby.getConnectionsClient(StudentDashBoard.this);

        closeHardwares = new CloseHardwares(getApplicationContext());

        markAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(StudentDashBoard.this)
                        .setTitle("Notification")
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setMessage("To mark your attendance please open your location and and check if app have permissions granted.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startAttendance();
                                dialog.cancel();
                            }
                        }).show();
            }
        });
    }

    private void startAttendance() {

        dialog = new ProgressDialog(StudentDashBoard.this);
        dialog.setCancelable(false);
        dialog.setMessage("Attendance started");
        dialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectionsClient.stopDiscovery();
                connectionsClient.stopAllEndpoints();
                if(closeHardwares.isBluetoothEnabled()){
                    closeHardwares.closeBluetooth();
                }
                if(closeHardwares.isWifiEnabled()){
                    closeHardwares.closeWifi();
                }
                Toast.makeText(StudentDashBoard.this, "Attendance cancelled by you", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        dialog.show();

        startDiscovery();

    }

    /**
     * Starts looking for other players using Nearby Connections.
     */
    private void startDiscovery() {

        connectionsClient.startDiscovery(getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e("StartDiscovery", "We are Discovering");
                dialog.setMessage("Started Searching teacher device");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentDashBoard.this, "Failed", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                connectionsClient.stopDiscovery();
                Log.e("StartAdvertise", "We are unable to Discover");
            }
        });
    }

    private EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.e("EndPointFound", "End point found " + s);

            if(discoveredEndpointInfo.getEndpointName().equals("teacher")){
                connectionsClient.requestConnection(myName, s, connectionLifecycleCallback);
                dialog.setMessage("Teacher device found");
                connectionsClient.stopDiscovery();
            }
        }

        @Override
        public void onEndpointLost(@NonNull String s) {

        }
    };

    /** Callbacks for connections to other devices*/
    private ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {

            Log.e("Teacher", "onConnectionInitiated: accepting connection");
            connectionsClient.acceptConnection(endpointId, payloadCallback);
            Log.e("ConnectionSenderName: ", connectionInfo.getEndpointName());
            dialog.setMessage("Connecting with teacher device...");

        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
            if (connectionResolution.getStatus().isSuccess()) {

                Log.e("ConnectionLifeCycle", "onConnectionResult: connection successful");
                Toast.makeText(StudentDashBoard.this, "Connection successful", Toast.LENGTH_SHORT).show();

                dialog.setMessage("Connected with teacher device");
                teacherID = endpointId;
                connectionsClient.sendPayload(teacherID, Payload.fromBytes(sendsDetailsString.getBytes()));
                connectionsClient.stopDiscovery();

            } else {
                Log.e("ConnectionLifeCycle", "onConnectionResult: connection failed");
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.i("ConnectionLifeCycle", "onDisconnected: disconnected from the opponent");
        }
    };

    /** Callbacks for receiving payloads*/
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            Log.e("ReceivedFrom: ", s);
            String fromTeacher = new String(payload.asBytes());

            Toast.makeText(StudentDashBoard.this, "ReceivedFrom : "+s, Toast.LENGTH_SHORT).show();
            Log.e("fromTeacher : ", fromTeacher);

            if(fromTeacher.equals("teacherConfirm8185")){
                Toast.makeText(StudentDashBoard.this, "Marked ", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                connectionsClient.stopAllEndpoints();
                connectionsClient.stopDiscovery();
                if(closeHardwares.isBluetoothEnabled()){
                    closeHardwares.closeBluetooth();
                }
                if(closeHardwares.isWifiEnabled()){
                    closeHardwares.closeWifi();
                }
                dialog.cancel();

            }else {
                dialog.cancel();
                if(closeHardwares.isBluetoothEnabled()){
                    closeHardwares.closeBluetooth();
                }
                if(closeHardwares.isWifiEnabled()){
                    closeHardwares.closeWifi();
                }
                connectionsClient.stopAllEndpoints();
                connectionsClient.stopDiscovery();
            }

        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };



    @Override
    protected void onStop() {
        super.onStop();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();

        if(closeHardwares.isWifiEnabled()){
            closeHardwares.closeWifi();
        }
        if(closeHardwares.isBluetoothEnabled()){
            closeHardwares.closeBluetooth();
        }

        dialog.cancel();
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