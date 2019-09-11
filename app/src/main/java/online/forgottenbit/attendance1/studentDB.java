package online.forgottenbit.attendance1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class studentDB extends SQLiteOpenHelper {

    public static final String DATABASE = "s_details";
    public static final String table_student_registration = "s_registration";

    public studentDB(Context context){
        super(context,DATABASE,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("Create table " + table_student_registration + "("
                + "s_name" + " TEXT,"
                + "s_roll" + " TEXT,"
                + "s_sap" + " TEXT,"
                + "s_email" + " TEXT,"
                + "s_course" + " TEXT,"
                + "s_batch_sec" + " TEXT,"
                + "s_mob" + " TEXT,"
                + "s_sem" + " TEXT,"
                + "s_imei" + " TEXT,"
                + "is_verified" + " INTEGER DEFAULT 0"
                + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_student_registration);

        onCreate(db);
    }

    public long insertSRegistrationDetails(String name,String roll,String sap,String email, String mob, String batchSec, String course,String imei,String sem){


        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("s_name", name);
        contentValues.put("s_roll", roll);
        contentValues.put("s_sap", sap);
        contentValues.put("s_email", email);
        contentValues.put("s_mob", mob);
        contentValues.put("s_course", course);
        contentValues.put("s_batch_sec", batchSec);
        contentValues.put("s_sem", sem);
        contentValues.put("s_imei", imei);

        Cursor res = sqLiteDatabase.rawQuery("select * from "+ table_student_registration,null);

        if(res.getCount()>0){
            sqLiteDatabase.delete(table_student_registration,null,null);
        }

        long id=sqLiteDatabase.insert(table_student_registration, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }

    public Cursor getSRegDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ table_student_registration,null);
        return res;
    }
    public void clearTable(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(table_student_registration,null,null);
        sqLiteDatabase.close();
    }

     public boolean updateSVerification(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

         ContentValues contentValues = new ContentValues();
         contentValues.put("is_verified", 1);

        int a = sqLiteDatabase.update(table_student_registration,contentValues,null,null);
        return a>0;
     }
}
