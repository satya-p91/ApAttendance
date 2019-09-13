package online.forgottenbit.attendance1.teacher;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import online.forgottenbit.attendance1.LanConnection.CloseHardwares;
import online.forgottenbit.attendance1.R;

public class ReceiveStudentData extends AppCompatActivity {


    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    ConnectionsClient connectionsClient;

    String[] sDetails;
    TeacherDB tDB;
    int BATCH_ID;
    AlertDialog dd;
    String studentID, studentDetails, sName;
    Button start, stop;
    TextView textStatus;

    private CloseHardwares closeHardwares;
    /**
     * Callbacks for receiving payloads
     */
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            Log.e("ReceivedFrom: ", endpointId);
            studentDetails = new String(payload.asBytes());
            textStatus.setText("Receiving data from " + sName);
            Log.e("StudentDetails : ", studentDetails);
            try {
                sDetails = parseReceivedData(studentDetails);
                showPopDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
    /**
     * Callback for connection to students devices
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            Log.e("TeacherServer", "onConnectionInitiated: accepting connection");
            textStatus.setText("Connection Initiated with " + connectionInfo.getEndpointName());
            sName = connectionInfo.getEndpointName();
            connectionsClient.acceptConnection(endpointId, payloadCallback);
            Log.e("ConnectionSenderName: ", connectionInfo.getEndpointName());
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {

            if (connectionResolution.getStatus().isSuccess()) {
                Log.e("ConnectionLifeCycle", "onConnectionResult: connection successful with : " + sName);

                textStatus.setText("connection successful with : " + endpointId);
                studentID = endpointId;
            } else {
                Log.i("ConnectionLifeCycle", "onConnectionResult: connection failed");

                textStatus.setText("connection failed with : " + sName);
            }

        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.i("ConnectionLifeCycle", "onDisconnected: disconnected from the student");

            textStatus.setText("connection successful with : " + sName);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_student_data);
/*
        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);*/

/* socketConnectionThread = new StartConnectionWithStudent();
        socketConnectionThread.execute();*/

        tDB = new TeacherDB(ReceiveStudentData.this);

        BATCH_ID = getIntent().getIntExtra("batch_id", 0);

        connectionsClient = Nearby.getConnectionsClient(this);

        closeHardwares = new CloseHardwares(getApplicationContext());

        stop = findViewById(R.id.stopAdvertising);
        start = findViewById(R.id.startAdvertising);
        textStatus = findViewById(R.id.textStatus);


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(true);
                stop.setEnabled(false);
                disconnect();

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                stop.setEnabled(true);
                startAdvertising();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    /**
     * Broadcasts our presence using Nearby Connections so other players can find us.
     */
    private void startAdvertising() {

        connectionsClient.startAdvertising("teacher", getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.e("StartAdvertise", "We are Advertising");
                        textStatus.setText("Started Advertising");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("StartAdvertise", "We are unable to Advertise");
                textStatus.setText("unable to Advertise, Reopen the app");
            }
        });
    }

    private void showPopDialog() {
        new AlertDialog.Builder(ReceiveStudentData.this)
                .setCancelable(false)
                .setTitle("Verify student")
                .setMessage(Html.fromHtml("<b>Name : </b>" + sDetails[0] + "<br><b>Roll : </b>" + sDetails[1] + "<br><b>Sem : </b>" + sDetails[7]))
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("Rejected", studentID);
                        connectionsClient.disconnectFromEndpoint(studentID);
                        dialog.cancel();

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long a = tDB.insertStudentDetails(sDetails[0], sDetails[1], sDetails[2], sDetails[3], sDetails[4], sDetails[5], sDetails[6], sDetails[7], sDetails[8], BATCH_ID);

                        Log.e("isInsertedInDB", a + " ");

                        sendMessage("teacherConfirm8185", studentID);
                        connectionsClient.disconnectFromEndpoint(studentID);
                        sDetails = null;

                        //TODO : have to add condition if student data is already in teacher device and student is re-registering himself
                        dialog.cancel();

                    }
                }).show();
    }


    /**
     * send message to given student id, Can only used by teacher side
     */
    private void sendMessage(String message, String studentID) {
        Payload payload = Payload.fromBytes(message.getBytes());
        connectionsClient.sendPayload(studentID, payload);
    }

    /**
     * Disconnects from the opponent and reset the UI.
     */
    public void disconnect() {
        Log.e("ConnectionClientServer", "Stopping all functionality");
        if (textStatus != null) {
            textStatus.setText("Stop registering students");
        }
        connectionsClient.stopAllEndpoints();
        connectionsClient.stopAdvertising();

        if(closeHardwares.isBluetoothEnabled()){
            closeHardwares.closeBluetooth();
        }

        if(closeHardwares.isWifiEnabled()){
            closeHardwares.closeWifi();
        }
    }


    private String[] parseReceivedData(String receivedData) {
        String[] val = receivedData.split("#");
        return val;
    }

}
