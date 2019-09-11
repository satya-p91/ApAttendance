package online.forgottenbit.attendance1.teacher;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Enumeration;

import online.forgottenbit.attendance1.R;

public class ReceiveStudentData extends AppCompatActivity {

    public static final int SERVER_PORT = 8185;
    public static String SERVER_IP = "";
    ServerSocket serverSocket;
    Socket socket;
    TextView tvIP, tvPort;

    String []sDetails;

    private PrintWriter output;
    private BufferedReader input;

    TeacherDB tDB;

    int BATCH_ID;

    StartConnectionWithStudent socketConnectionThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_student_data);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tDB = new TeacherDB(ReceiveStudentData.this);

        BATCH_ID = getIntent().getIntExtra("batch_id",0);

        socketConnectionThread = new StartConnectionWithStudent();
        socketConnectionThread.execute();


    }

    private String getLocalIpAddress() throws UnknownHostException {

        if (ApManager.isApOn(this)) {

            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                     en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (intf.getName().contains("wlan")) {

                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                                Log.e("error", inetAddress.getHostAddress());
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                Log.e("error", ex.toString());
            }

        } else {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipInt = wifiInfo.getIpAddress();
                Log.e("IP address : ", InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress());
                return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
            }
        }

        return null;

    }


    class StartConnectionWithStudent extends AsyncTask<Void, Void, Void>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ReceiveStudentData.this, "Ready for connection", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            try {

                if(serverSocket!= null && serverSocket.isBound()){
                    serverSocket.close();
                }
                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvIP.setText("IP: " + SERVER_IP);
                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                try {
                    socket = serverSocket.accept();
                    socket.setKeepAlive(true);
                    output = new PrintWriter(socket.getOutputStream());

                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    new Thread(new Thread2(input)).start();
                } catch (IOException e) {
                    Log.e("In theard 1", e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tDB.close();

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (output != null){
            output.close();
        }
        if(input!=null){
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socketConnectionThread != null) {
            socketConnectionThread.cancel(true);
        }

    }

    private String[] parseReceivedData(String receivedData){
        String []val = receivedData.split("#");
        return val;
    }

    class Thread2 implements Runnable {

        private BufferedReader ipObj;

        Thread2(BufferedReader ipObj) {
            this.ipObj = ipObj;
        }

        @Override
        public void run() {
            while (true) {
                try {

                    if (ipObj == null) {
                        Log.e("Err", "null obj is passed");
                    }
                    final String message = ipObj.readLine();

                    Log.e("msgServerReceive",":  "+message);

                    if (message != null) {
                        sDetails = parseReceivedData(message);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(ReceiveStudentData.this)
                                        .setCancelable(false)
                                        .setTitle("Verify student")
                                        .setMessage(Html.fromHtml("<b>Name : </b>"+sDetails[0]+"<br><b>Roll : </b>"+sDetails[1]))
                                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new Thread(new Thread3("Reject")).start();


                                                dialog.cancel();

                                            }
                                        })
                                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                new Thread(new Thread3("teacherConfirm8185")).start();

                                                tDB.insertStudentDetails(sDetails[0],sDetails[1],sDetails[2],sDetails[3],sDetails[4],sDetails[5],sDetails[6],sDetails[7],sDetails[8],BATCH_ID);
                                                //TODO : have to add condition if student data is already in teacher device and student is re-registering himself
                                                dialog.cancel();

                                                socketConnectionThread = new StartConnectionWithStudent();
                                                socketConnectionThread.execute();
                                            }
                                        }).show();
                            }
                        });
                    } else {

                        Log.e("msgServerReceive","Error hai thread 2 server");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                socketConnectionThread = new StartConnectionWithStudent();
                                socketConnectionThread.execute();
                            }
                        });
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Thread3 implements Runnable {
        private String message;

        Thread3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.write(message+"\n");
            output.flush();
            Log.e("msgServerSend",message);

            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
