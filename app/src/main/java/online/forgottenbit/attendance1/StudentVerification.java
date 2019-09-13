package online.forgottenbit.attendance1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

import online.forgottenbit.attendance1.LanConnection.CloseHardwares;
import online.forgottenbit.attendance1.teacher.ApManager;

public class StudentVerification extends AppCompatActivity {


    //final int port = 8185;
    //OpenSocketConnection socketConnectionThread = null;
    //private PrintWriter output;
    //private BufferedReader input;
    //String TEACHER_IP;
    //Socket socket;

    Button verify, reRegister;
    studentDB sDB;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    String teacherID,myName;


    String sendsDetailsString = null;
    ProgressDialog dialog;

    ConnectionsClient connectionsClient;

    CloseHardwares closeHardwares;


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
        myName = ss.getString(0);

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


        connectionsClient = Nearby.getConnectionsClient(StudentVerification.this);

        closeHardwares = new CloseHardwares(getApplicationContext());

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(StudentVerification.this)
                        .setTitle("Notification")
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setMessage("To verify your device please open your location and and check if app have permissions granted.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startVerification();
                                dialog.cancel();
                            }
                        }).show();
            }
        });

    }

    public void startVerification(){

        dialog = new ProgressDialog(StudentVerification.this);
        dialog.setCancelable(false);
        dialog.setMessage("Verification started");
        dialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectionsClient.stopDiscovery();
                connectionsClient.stopAllEndpoints();
                Log.e("StudentVerification","Stopped all service");
                if(closeHardwares.isBluetoothEnabled()){
                    closeHardwares.closeBluetooth();
                }
                if(closeHardwares.isWifiEnabled()){
                    closeHardwares.closeWifi();
                }
                Toast.makeText(StudentVerification.this, "Verification cancelled by you", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        dialog.show();

        startDiscovery();
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

    // Callbacks for connections to other devices
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
                Toast.makeText(StudentVerification.this, "Connection successful", Toast.LENGTH_SHORT).show();

                dialog.setMessage("Connected with teacher device");
                teacherID = endpointId;
                connectionsClient.sendPayload(endpointId,Payload.fromBytes(sendsDetailsString.getBytes()));
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

    /** Callbacks for receiving payloads*/
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            Log.e("ReceivedFrom: ", s);
            String fromTeacher = new String(payload.asBytes());

            Toast.makeText(StudentVerification.this, "ReceivedFrom : "+s, Toast.LENGTH_SHORT).show();
            Log.e("fromTeacher : ", fromTeacher);

            if(fromTeacher.equals("teacherConfirm8185")){
                boolean b = sDB.updateSVerification();
                Toast.makeText(StudentVerification.this, "Verified " +b, Toast.LENGTH_SHORT).show();
                Log.e(StudentVerification.class.getSimpleName(), "  " + b + "  ");
                dialog.cancel();
                connectionsClient.stopAllEndpoints();
                connectionsClient.stopDiscovery();
                if(closeHardwares.isBluetoothEnabled()){
                    closeHardwares.closeBluetooth();
                }
                if(closeHardwares.isWifiEnabled()){
                    closeHardwares.closeWifi();
                }
                startActivity(new Intent(StudentVerification.this, StudentDashBoard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();

            }else {
                Toast.makeText(StudentVerification.this, "Rejected with msg : "+ fromTeacher, Toast.LENGTH_LONG).show();
                dialog.cancel();
                connectionsClient.stopAllEndpoints();
                connectionsClient.stopDiscovery();
            }

        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };


    /**
     * Starts looking for other players using Nearby Connections.
     */
    private void startDiscovery() {

        connectionsClient.startDiscovery(getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e("StartDiscovery", "We are Discovering");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("StartAdvertise", "We are unable to Discover");
            }
        });
    }




}
