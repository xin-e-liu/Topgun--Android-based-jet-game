/*
Copyright 2011 codeoedoc

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.box.game.planeandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ID = "userid";
    public static final String KEY_ISBN = "isbn";
    public static final String KEY_LEVEL = "level";
    public static final String KEY_RECORD = "record";
    public static final String KEY_TIMESTAMP = "time";
    private static final String TAG = "DBAdapter";

    private static final String DATABASE_NAME = "TopGun";
    private static final String DATABASE_TABLE = "record";
    private static final int DATABASE_VERSION = 3;

    private static final String DATABASE_CREATE = "create table record (_id integer primary key autoincrement, userid text, "
            + "isbn text, level integer, " + "record double, time text);";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS record");
            onCreate(db);
        }
    }

    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertRecord(String ID, String isbn, int level, double record,
            String time) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, ID);
        initialValues.put(KEY_ISBN, isbn);
        initialValues.put(KEY_LEVEL, level);
        initialValues.put(KEY_RECORD, record);
        initialValues.put(KEY_TIMESTAMP, time);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteRecord(String ID, int level) {
        return db.delete(DATABASE_TABLE, KEY_ID + "=" + ID + " AND "
                + KEY_LEVEL + "=" + level, null) > 0;
    }

    public Cursor getAllRecords() {
        return db.query(
                DATABASE_TABLE,
                new String[] { KEY_ROWID, KEY_ID, KEY_ISBN, KEY_LEVEL,
                        KEY_RECORD, KEY_TIMESTAMP },
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getRecord(String ID) throws SQLException {
        Cursor mCursor = db.query(
                DATABASE_TABLE,
                new String[] { KEY_ROWID, KEY_ID, KEY_ISBN, KEY_LEVEL,
                        KEY_RECORD, KEY_TIMESTAMP },
                KEY_ID + "='" + ID + "'",
                null,
                null,
                null,
                null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getRecordIDLevel(String ID, int level) throws SQLException {
        Cursor mCursor = db.query(
                DATABASE_TABLE,
                new String[] { KEY_ROWID, KEY_ID, KEY_ISBN, KEY_LEVEL,
                        KEY_RECORD, KEY_TIMESTAMP },
                KEY_ID + "='" + ID + "' AND " + KEY_LEVEL + "=" + level,
                null,
                null,
                null,
                null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateRecordShort(String ID, int level, int record) {
        ContentValues args = new ContentValues();
        args.put(KEY_RECORD, record);
        return db.update(DATABASE_TABLE, args, KEY_ID + "='" + ID + "' AND "
                + KEY_LEVEL + "=" + level, null) > 0;
    }

    public boolean updateRecord(String ID, String isbn, int level,
            double record, String time) {
        ContentValues args = new ContentValues();
        args.put(KEY_ISBN, isbn);
        args.put(KEY_RECORD, record);
        args.put(KEY_TIMESTAMP, time);
        return db.update(DATABASE_TABLE, args, KEY_ID + "='" + ID + "' AND "
                + KEY_LEVEL + "=" + level, null) > 0;
    }
}