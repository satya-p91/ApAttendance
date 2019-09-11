package online.forgottenbit.attendance1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import online.forgottenbit.attendance1.teacher.TeacherDB;
import online.forgottenbit.attendance1.teacher.TeacherDashboard;
import online.forgottenbit.attendance1.teacher.TeacherRegistration;

public class MainActivity extends AppCompatActivity {

    Button student, teacher;

    Dialog dialogTAuth;

    int permission_req_code = 1000;
    View view;
    studentDB sDB;

    TeacherDB teacherDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sDB = new studentDB(MainActivity.this);
        teacherDB = new TeacherDB(MainActivity.this);

        try{
            Cursor s = sDB.getSRegDetails();


            Cursor sT = teacherDB.getTRegDetails();



            if(s!=null && s.getCount()>0){
                Log.e("inside not null","");
                if (s.moveToNext()){
                    Log.e("  ..  ",s.getString(0)+"      "+s.getInt(9));

                    if(s.getString(8).equals(getDeviceIMEI()) && s.getInt(9)==1){
                        startActivity(new Intent(MainActivity.this,StudentDashBoard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }else if(s.getString(8).equals(getDeviceIMEI()) && s.getInt(9)==0){
                        startActivity(new Intent(MainActivity.this,StudentVerification.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }

                }
            }

            if(sT!=null && sT.getCount() > 0){
                Log.e("b"," teacher data");
                if(sT.moveToNext()){
                    if(sT.getString(3).equals(getDeviceIMEI())){
                        startActivity(new Intent(MainActivity.this, TeacherDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                }else{
                    Log.e("b","not founf teacher data");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        student = findViewById(R.id.student);
        teacher = findViewById(R.id.teacher);


        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,StudentRegistration.class));
            }
        });


        dialogTAuth = new Dialog(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.custom_dialog_teacher_auth,null);
        dialogTAuth.setContentView(view);



        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogTAuth.show();
                dialogTAuth.setCancelable(true);

                Button cancel = view.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogTAuth.cancel();
                        recreate();
                    }
                });

                Button confirm  = view.findViewById(R.id.confirm);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        EditText pass = view.findViewById(R.id.passcode);
                        String passStr = pass.getText().toString().trim();

                        if(passStr != null && !passStr.isEmpty() && passStr.equals("123456")){
                            dialogTAuth.cancel();
                            startActivity(new Intent(MainActivity.this, TeacherRegistration.class));
                            pass.setText("");
                        }else{
                            Toast.makeText(MainActivity.this, "Passoword is not correct.", Toast.LENGTH_SHORT).show();
                            dialogTAuth.cancel();
                            pass.setText("");
                            return;
                        }

                    }
                });
            }
        });


    }


    @SuppressLint("MissingPermission")
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if(!defaultPermissionCheck()){
                askForPermission();
            }else{
                deviceUniqueIdentifier = tm.getDeviceId();
            }

        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }


    private void askForPermission() {

        //asking  for storage permission from user at runtime

        ActivityCompat.requestPermissions(this,new String[] {
                Manifest.permission.READ_PHONE_STATE
        },permission_req_code);


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //checking if user granted the permissions or not

        if(requestCode == permission_req_code){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted :)", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "App will not work without permissions, Grant these permissions from settings. :|", Toast.LENGTH_LONG).show();
                askForPermission();
            }
        }
    }

    private boolean defaultPermissionCheck() {
        //checking if permissions is already granted
        int external_storage_write = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        return external_storage_write == PackageManager.PERMISSION_GRANTED;
    }




}
