package online.forgottenbit.attendance1.LanConnection;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;

import online.forgottenbit.attendance1.teacher.TeacherDB;

public class Server {

    private static final Strategy STRATEGY = Strategy.P2P_STAR;


    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    ArrayList<String> studentsID = new ArrayList<>();

    TeacherDB tDB;

    Context context;

    public Server(Context context, ConnectionsClient connectionsClient){
        this.context = context;
        this.connectionsClient = connectionsClient;

        tDB = new TeacherDB(context);
        studentsID.clear();
        Log.e("ServerClass","Cleared Studetents ID list");
    }


    /** send message to given student id, Can only used by teacher side*/
    private void sendMessage(String message, String studentID){
        Payload payload = Payload.fromBytes(message.getBytes());
        connectionsClient.sendPayload(studentID,payload);
    }

    /** Disconnects from the opponent and reset the UI. */
    public void disconnect() {
        Log.e("ConnectionClientServer","Stopping all functionality");
        connectionsClient.stopAllEndpoints();
        connectionsClient.stopAdvertising();
    }


    /** Callback for connection to students devices */
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            Log.e("TeacherServer", "onConnectionInitiated: accepting connection");

            //TODO : add verification if student already connected then should not connect again
            connectionsClient.acceptConnection(endpointId,payloadCallback);
            Log.e("ConnectionSenderName: ",connectionInfo.getEndpointName());

        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {

            if(connectionResolution.getStatus().isSuccess()){
                Log.e("ConnectionLifeCycle", "onConnectionResult: connection successful with : "+endpointId);
                studentsID.add(endpointId);
            }else {
                Log.i("ConnectionLifeCycle", "onConnectionResult: connection failed");
            }


        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.i("ConnectionLifeCycle", "onDisconnected: disconnected from the student");
        }
    };


    /** Callbacks for receiving payloads */
    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            Log.e("ReceivedFrom: ",endpointId);
            /*studentDetails = new String(payload.asBytes());
            sendMessage("You sent : "+studentDetails);
            startAdvertising.setEnabled(true);
            textStatus.setText("Message received : "+studentDetails);
            Log.e("StudentDetails : ",studentDetails);*/
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private String[] parseReceivedData(String receivedData) {
        String[] val = receivedData.split("#");
        return val;
    }















}
