package com.rlb.bloodlink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="ClientDB";
    private static final int DATABASE_VERSION= 1;
    private static final String TABLE_CLIENT="client";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ROLE="role";
    private static final String COLUMN_NAME="name";
    private static final String COLUMN_EMAIL="email";
    private static final String COLUMN_TELEPHONE="telephone";
    private static final String COLUMN_RHESUS="rhesus";
    private static final String COLUMN_GROUPE="groupe";

    private static final String COLUMN_SEXE="sexe";
    private static final String CREATE_CLIENT_ROLE = "CREATE TABLE " + TABLE_CLIENT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_SEXE + " TEXT, "
            + COLUMN_TELEPHONE + " TEXT, "
            + COLUMN_ROLE + " TEXT, "
            + COLUMN_GROUPE + " TEXT, "
            + COLUMN_RHESUS + " TEXT)";

    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_CLIENT_ROLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldversion, int newversion){
            db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CLIENT);
            onCreate(db);

    }
    public long insertClientRole(String role){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROLE,role);
        long id=db.insert(TABLE_CLIENT,null,values);
        db.close();
        return id;
    }
    public int updatePI(long id,String nom,String email,String telephone,String sexe){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,nom);
        values.put(COLUMN_EMAIL,email);
        values.put(COLUMN_SEXE,sexe);
        values.put(COLUMN_TELEPHONE,telephone);

        int rows = db.update(TABLE_CLIENT,values,"id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
    public int updateGroupeRhesus(long id,String groupe,String rhesus){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUPE,groupe);
        values.put(COLUMN_RHESUS,rhesus);
        int rows = db.update(TABLE_CLIENT,values,"id = ?",new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
    public Cursor getLastProgress(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT "+COLUMN_NAME+", "+COLUMN_EMAIL+", "+COLUMN_TELEPHONE+", "+COLUMN_ROLE+", "+COLUMN_SEXE+", "+COLUMN_GROUPE+", "+COLUMN_RHESUS+" FROM "+TABLE_CLIENT+" ORDER BY " + COLUMN_ID+" DESC LIMIT 1",null);

    }
}
