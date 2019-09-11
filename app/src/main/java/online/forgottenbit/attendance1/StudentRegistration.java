package online.forgottenbit.attendance1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import online.forgottenbit.attendance1.teacher.TeacherRegistration;

public class StudentRegistration extends AppCompatActivity {

    public static final String myPreference = "mypref";
    EditText sName, sRoll, sSap, sEmail, sMob, sCourse, sBranchSec,sSem;
    String strName, strRoll, strSap, strEmail, strMob, strCourse, strBranchSec,strSem;
    Button submitStudentDetails;
    studentDB sDB;
    int permission_req_code = 1000;
    //SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        if (!defaultPermissionCheck()) {
            askForPermission();
        }


        //sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE);

        sDB = new studentDB(StudentRegistration.this);

        submitStudentDetails = findViewById(R.id.submitStudentDetails);
        sName = findViewById(R.id.sName);
        sRoll = findViewById(R.id.sRoll);
        sSap = findViewById(R.id.sSap);
        sEmail = findViewById(R.id.sEmail);
        sMob = findViewById(R.id.sMob);
        sCourse = findViewById(R.id.sCourse);
        sBranchSec = findViewById(R.id.sBranchSec);
        sSem = findViewById(R.id.sSem);


        submitStudentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strName = sName.getText().toString().trim();
                strRoll = sRoll.getText().toString().trim();
                strSap = sSap.getText().toString().trim();
                strEmail = sEmail.getText().toString().trim();
                strMob = sMob.getText().toString().trim();
                strCourse = sCourse.getText().toString().trim();
                strBranchSec = sBranchSec.getText().toString().trim();
                strSem = sSem.getText().toString().trim();


                if(strName == null || strName.isEmpty() || strRoll == null || strRoll.isEmpty() || strSap == null || strSap.isEmpty()
                || strEmail == null || strEmail.isEmpty() || strMob==null || strMob.isEmpty() || strCourse==null || strCourse.isEmpty() || strBranchSec == null
                 || strBranchSec.isEmpty() || strSem == null || strSem.isEmpty()){

                    showDialog("Empty Field(s)...", "All fields are mandatory. Please fill all fields to register");

                }else if(strMob.length()<10){

                    showDialog("Invalid mobile number","Please enter valid mobile number");

                } else if(!isValidEmail(strEmail)){

                    showDialog("Invalid email id","Please Enter valid email id");

                }else{

                    new android.app.AlertDialog.Builder(StudentRegistration.this).setTitle("Verify Details").setMessage("Name : " + strName + "\n" + "Roll : " + strRoll + "\n" +
                            "SAP ID : " + strSap + "\n" + "Email : " + strEmail + "\n" + "Mobile : " + strMob + "\n" + "Course : " + strCourse + "\n" + "Branch and Section : " + strBranchSec+"\n"+"Semester : "+strSem)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    long a = sDB.insertSRegistrationDetails(strName,strRoll,strSap,strEmail,strMob,strBranchSec,strCourse,getDeviceIMEI(),strSem);


                                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("name", strName);
                                editor.putString("roll", strRoll);
                                editor.putString("sap", strSap);
                                editor.putString("email", strEmail);
                                editor.putString("mob", strMob);
                                editor.putString("course", strCourse);
                                editor.putString("batchSec", strBranchSec);
                                editor.putString("imei", getDeviceIMEI());
                                editor.commit();*/


                                    dialog.cancel();
                                    Log.e("DB","   "+a+" ");
                                    startActivity(new Intent(StudentRegistration.this, StudentVerification.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                }
                            }).setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
                }

            }
        });

    }

    public void showDialog(String title, String message){
        new AlertDialog.Builder(StudentRegistration.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @SuppressLint("MissingPermission")
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (!defaultPermissionCheck()) {
                askForPermission();
            } else {
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

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_PHONE_STATE
        }, permission_req_code);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //checking if user granted the permissions or not

        if (requestCode == permission_req_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted :)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "App will not work without permissions, Grant these permissions from settings. :|", Toast.LENGTH_LONG).show();
                startActivity(new Intent(StudentRegistration.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        }
    }

    private boolean defaultPermissionCheck() {
        //checking if permissions is already granted
        int external_storage_write = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        return external_storage_write == PackageManager.PERMISSION_GRANTED;
    }


}
