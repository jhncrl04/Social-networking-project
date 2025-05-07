package com.example.fakebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context){
        super(context, "localUserData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE localUserTable (email TEXT PRIMARY KEY, id TEXT, firstName TEXT, lastName TEXT, phone TEXT, username TEXT, " +
                "password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists localUserTable");
    }

    public Boolean insertUserData(String firstName, String lastName, String email, String phone){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", 1);
        contentValues.put("email", email);
        contentValues.put("firstName", firstName);
        contentValues.put("lastName", lastName);
        contentValues.put("phone", phone);

        long result = DB.insert("localUserTable", null, contentValues);

        if(result == -1){
            return false;
        } else{
            return true;
        }
    }

    public Boolean insertUsername(String username){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);

        Cursor cursor = DB.rawQuery("SELECT * FROM localUserTable where id = ?", new String[]{"1"});

        if(cursor.getCount() > 0){
            long result = DB.update("localUserTable", contentValues, "id = ?", new String[]{"1"});

            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Boolean insertPassword(String password){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", password);

        Cursor cursor = DB.rawQuery("SELECT * FROM localUserTable where id = ?", new String[]{"1"});

        if(cursor.getCount() > 0){
            long result = DB.update("localUserTable", contentValues, "id = ?", new String[]{"1"});

            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Cursor getData(){
            SQLiteDatabase DB = this.getWritableDatabase();
            Cursor cursor = DB.rawQuery("SELECT email, password, username, firstName, lastName, phone FROM localUserTable", null);

            return cursor;
    }

    public Boolean deleteData(){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM localUserTable where id = ?", new String[]{"1"});

        if(cursor.getCount() > 0){
            long result = DB.delete("localUserTable", "id = ? ", new String[]{"1"});

            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }
}
