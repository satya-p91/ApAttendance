package online.forgottenbit.attendance1.teacher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import online.forgottenbit.attendance1.MainActivity;
import online.forgottenbit.attendance1.R;
import online.forgottenbit.attendance1.StudentRegistration;

public class TeacherRegistration extends AppCompatActivity {

    TextView singIn, signUp;

    RelativeLayout signInLayout, signUpLayout;

    EditText name,email,mobile;
    String nameStr,emailStr,mobStr;
    Button submit;

    TeacherDB teacherDB;

    int permission_req_code = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_registration);

        if (!defaultPermissionCheck()) {
            askForPermission();
        }

        teacherDB = new TeacherDB(TeacherRegistration.this);

        singIn = findViewById(R.id.singin);
        signUp = findViewById(R.id.singup);

        signInLayout = findViewById(R.id.signinLayout);
        signUpLayout = findViewById(R.id.signupLayout);

        name = findViewById(R.id.editTeacherName);
        email = findViewById(R.id.editTeacherEmail);
        mobile = findViewById(R.id.editTeacherMob);
        submit = findViewById(R.id.submitTeacherDetails);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpLayout.setVisibility(View.VISIBLE);
                signInLayout.setVisibility(View.GONE);
                signUp.setTextColor(Color.parseColor("#57CAD5"));
                singIn.setTextColor(Color.parseColor("#999999"));

            }
        });


        singIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpLayout.setVisibility(View.GONE);
                signInLayout.setVisibility(View.VISIBLE);
                signUp.setTextColor(Color.parseColor("#999999"));
                singIn.setTextColor(Color.parseColor("#57CAD5"));

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameStr = name.getText().toString().trim();
                emailStr = email.getText().toString().trim();
                mobStr = mobile.getText().toString().trim();

                if(nameStr == null || nameStr.isEmpty() || emailStr == null || emailStr.isEmpty() || mobStr == null || mobStr.isEmpty()){
                    showDialog("Empty Field(s)","All fields are mandatory. Fill all fields to reigster");
                    return;
                }

                if(!isValidEmail(emailStr)){
                    showDialog("Invalid Email ID", "Please enter valid eamil ID to register.");
                }

                long a=teacherDB.insertTRegistrationDetails(nameStr,emailStr,mobStr,getDeviceIMEI());

                Log.e("insert",".. "+a+"  ");
                startActivity(new Intent(TeacherRegistration.this, TeacherDashboard.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

    }

    public void showDialog(String title, String message){
        new AlertDialog.Builder(TeacherRegistration.this)
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
                startActivity(new Intent(TeacherRegistration.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
