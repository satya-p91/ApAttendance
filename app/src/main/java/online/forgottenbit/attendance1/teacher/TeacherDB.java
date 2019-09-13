package online.forgottenbit.attendance1.teacher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TeacherDB extends SQLiteOpenHelper {


    public static final String DATABASE = "t_details";
    public static final String table_teacher_registration = "s_registration";
    public static final String table_batch_details = "batch_details";
    public static final String table_subject_details = "subject_details";
    public static final String table_student_details = "student_details";
    public static final String table_attendance_details = "attendance_details";


    public TeacherDB(Context context) {
        super(context, DATABASE, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + table_teacher_registration + "("
                + "s_name" + " TEXT,"
                + "s_email" + " TEXT,"
                + "s_mob" + " TEXT,"
                + "s_imei" + " TEXT"
                + ")");

        db.execSQL("Create table " + table_batch_details + "("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name" + " TEXT"
                + ")");

        db.execSQL("Create table " + table_subject_details + "("
                + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "batch_id" + " INTEGER,"
                + "name" + " TEXT,"
                + "code" + " TEXT"
                + ")");

        db.execSQL("Create table " + table_attendance_details + "("
                + "batch_id" + " INTEGER,"
                + "subject_id" + " INTEGER,"
                + "student_roll" + " TEXT,"
                + "time_stamp" + " TEXT,"
                + "AorP" + " INTEGER"
                + ")");

        db.execSQL("Create table " + table_student_details + "("
                + "s_name" + " TEXT,"
                + "s_roll" + " TEXT,"
                + "s_sap" + " TEXT,"
                + "s_email" + " TEXT,"
                + "s_course" + " TEXT,"
                + "s_batch_sec" + " TEXT,"
                + "s_mob" + " TEXT,"
                + "s_sem" + " TEXT,"
                + "s_imei" + " TEXT,"
                + "batch_id" + " INTEGER"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_teacher_registration);
        db.execSQL("DROP TABLE IF EXISTS " + table_batch_details);
        db.execSQL("DROP TABLE IF EXISTS " + table_student_details);
        db.execSQL("DROP TABLE IF EXISTS " + table_subject_details);
        db.execSQL("DROP TABLE IF EXISTS " + table_attendance_details);
        onCreate(db);
    }

    public Cursor getAttendance(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ table_student_details,null);
        return res;
    }

    public long insertAttendance(int batch_id, int subject_id,String roll, String timeStamp, int AorP){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("batch_id", batch_id);
        contentValues.put("subject_id", subject_id);
        contentValues.put("student_roll", roll);
        contentValues.put("time_stamp", timeStamp);
        contentValues.put("AorP", AorP);

        long id = sqLiteDatabase.insert(table_student_details,null,contentValues);
        Log.e("Inserted into data base","   "+id+"  ");
        return id;
    }


    public Cursor getStudentByBatch(int batch_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ table_student_details + " where batch_id = "+ batch_id,null);
        return res;
    }

    public Cursor getAllStudent(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ table_subject_details,null);
        return res;
    }

    public Cursor getSubjectByBatch(int batch_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ table_subject_details + " where batch_id = "+ batch_id,null);
        return res;
    }

    public long insertStudentDetails(String name,String roll, String sap, String email, String course, String batchSec, String mob, String sem, String imei, int batch_id){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("s_name", name);
        contentValues.put("s_roll", roll);
        contentValues.put("s_sap", sap);
        contentValues.put("s_email", email);
        contentValues.put("s_course", course);
        contentValues.put("s_batch_sec", batchSec);
        contentValues.put("s_mob", mob);
        contentValues.put("s_sem", sem);
        contentValues.put("s_imei", imei);
        contentValues.put("batch_id", batch_id);

        long id = sqLiteDatabase.insert(table_student_details,null,contentValues);
        Log.e("Inserted into data base","   "+id+"  ");
        return id;
    }


    public long insertSubject(int batch_id,String name, String code){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("code", code);
        contentValues.put("batch_id", batch_id);

        long id = sqLiteDatabase.insert(table_subject_details,null,contentValues);
        return id;
    }

    public long insertTRegistrationDetails(String name, String email, String mob, String imei) {


        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("s_name", name);
        contentValues.put("s_email", email);
        contentValues.put("s_mob", mob);
        contentValues.put("s_imei", imei);

        Cursor res = sqLiteDatabase.rawQuery("select * from " + table_teacher_registration, null);

        if (res.getCount() > 0) {
            sqLiteDatabase.delete(table_teacher_registration, null, null);
        }

        long id = sqLiteDatabase.insert(table_teacher_registration, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }

    public Cursor getTRegDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + table_teacher_registration, null);
        return res;
    }


    public void clearTable() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(table_teacher_registration, null, null);
        sqLiteDatabase.delete(table_batch_details, null, null);
        sqLiteDatabase.close();
    }

    public long insertBatch(String name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        long id = db.insert(table_batch_details, null, contentValues);
        db.close();
        return id;
    }



    public Cursor getAllBatch() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor ss = db.rawQuery("select * from " + table_batch_details, null);
        return ss;
    }

    public Cursor getBatchByID(int id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor ss = db.rawQuery("select * from " + table_batch_details + " where id=" + id, null);
        return ss;
    }

}
