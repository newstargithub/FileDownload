package com.halo.update.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.halo.update.DownloadInfo;

import java.util.List;

/**
 * Created by zhouxin on 2016/5/25.
 * Description: Data Access Object 数据访问对象
 */
public class DownloadDao extends DataBaseDao<DownloadInfo>{

    public DownloadDao(Context context) {
        super(new DownloadHelper(context));
    }

    /** 根据URL获取 */
    public DownloadInfo get(String url) {
        String selection = DownloadHelper.DownloadEntry.COLUMN_NAME_URL + "=?";
        String[] selectionArgs = new String[]{url};
        List<DownloadInfo> list = get(selection, selectionArgs);
        return list.size() > 0 ? list.get(0) : null;
    }

    /** 移除一个 */
    public boolean remove(String url) {
        String whereClause = DownloadHelper.DownloadEntry.COLUMN_NAME_URL + "=?";
        String[] whereArgs = new String[]{url};
        int delete = delete(whereClause, whereArgs);
        return delete > 0;
    }

    @Override
    public DownloadInfo parseCursorToBean(Cursor cursor) {
        DownloadInfo bean = new DownloadInfo();
        bean.setId(cursor.getInt(cursor.getColumnIndex(DownloadHelper.DownloadEntry._ID)));
        bean.setUrl(cursor.getString(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_URL)));
        bean.setTargetFolder(cursor.getString(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_TARGET_FOLDER)));
        bean.setTargetPath(cursor.getString(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_TARGET_PATH)));
        bean.setFileName(cursor.getString(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_FILE_NAME)));
        bean.setProgress(cursor.getFloat(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_PROGRESS)));
        bean.setTotalLength(cursor.getLong(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_TOTAL_LENGTH)));
        bean.setDownloadLength(cursor.getLong(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_DOWNLOAD_LENGTH)));
        bean.setState(cursor.getInt(cursor.getColumnIndex(DownloadHelper.DownloadEntry.COLUMN_NAME_STATE)));
        return bean;
    }

    @NonNull
    private ContentValues getContentValues(DownloadInfo bean) {
        ContentValues values = new ContentValues();
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_URL, bean.getUrl());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_TARGET_FOLDER, bean.getTargetFolder());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_TARGET_PATH, bean.getTargetPath());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_FILE_NAME, bean.getFileName());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_PROGRESS, bean.getProgress());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_TOTAL_LENGTH, bean.getTotalLength());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_DOWNLOAD_LENGTH, bean.getDownloadLength());
        values.put(DownloadHelper.DownloadEntry.COLUMN_NAME_STATE, bean.getState());
        return values;
    }

    @Override
    protected String getTableName() {
        return DownloadHelper.DownloadEntry.TABLE_NAME;
    }

    @Override
    public long replace(DownloadInfo bean) {
        SQLiteDatabase database = openWriter();
        ContentValues values = getContentValues(bean);
        long id = database.replace(getTableName(), null, values);
        closeDatabase(database, null);
        return id;
    }

    public long insert(DownloadInfo bean) {
        SQLiteDatabase database = openWriter();
        ContentValues values = getContentValues(bean);
        long id = database.insert(getTableName(), null, values);
        closeDatabase(database, null);
        return id;
    }

    public void update(DownloadInfo bean) {
        SQLiteDatabase database = openWriter();
        ContentValues values = getContentValues(bean);
        String whereClause = DownloadHelper.DownloadEntry.COLUMN_NAME_URL + "=?";
        String[] whereArgs = new String[]{bean.getUrl()};
        database.update(getTableName(), values, whereClause, whereArgs);
        closeDatabase(database, null);
    }
}
