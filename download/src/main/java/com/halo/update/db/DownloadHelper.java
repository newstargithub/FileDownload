package com.halo.update.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by zhouxin on 2016/5/25.
 * Description:
 */
public class DownloadHelper extends SQLiteOpenHelper{

    /* Inner class that defines the table contents */
    public static abstract class DownloadEntry implements BaseColumns {
        public static final String TABLE_NAME = "download";

        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_TARGET_FOLDER = "target_folder";
        public static final String COLUMN_NAME_TARGET_PATH = "target_path";
        public static final String COLUMN_NAME_FILE_NAME = "file_name";
        public static final String COLUMN_NAME_PROGRESS = "progress";
        public static final String COLUMN_NAME_TOTAL_LENGTH = "total_length";
        public static final String COLUMN_NAME_DOWNLOAD_LENGTH = "download_length";
        public static final String COLUMN_NAME_STATE = "state";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String LONG_TYPE = " LONG";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + DownloadEntry.TABLE_NAME + "(" +
            DownloadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DownloadEntry.COLUMN_NAME_URL + TEXT_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_TARGET_FOLDER + TEXT_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_TARGET_PATH + TEXT_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_FILE_NAME + TEXT_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_PROGRESS + FLOAT_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_TOTAL_LENGTH + LONG_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_DOWNLOAD_LENGTH + LONG_TYPE + " , " +
            DownloadEntry.COLUMN_NAME_STATE + INTEGER_TYPE + " )";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + DownloadEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DownloadInfo.db";

    public DownloadHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_TABLE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_DELETE_TABLE);
            db.execSQL(SQL_CREATE_TABLE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
