package tk.cheuksblog.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.design.widget.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cheukyin699 on 8/15/17.
 */

public class ToDoOpenHelper extends SQLiteOpenHelper {
    public static final String TODO_TABLE_NAME = "todo";
    public static final String CATEGORY_TABLE_NAME = "categories";
    private static final int TODO_VERSION = 1;
    private static final String TODO_TABLE_CREATE =
            "CREATE TABLE " + TODO_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category_id INTEGER," +
                    "done BOOLEAN," +
                    "description TEXT NOT NULL," +
                    "FOREIGN KEY(category_id) REFERENCES " + CATEGORY_TABLE_NAME + "(id)" +
            ");";
    private static final String CATEGORY_TABLE_CREATE =
            "CREATE TABLE " + CATEGORY_TABLE_NAME + "(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT" +
            ");";

    ToDoOpenHelper(Context c) {
        super(c, TODO_TABLE_NAME, null, TODO_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TODO_TABLE_CREATE);
        db.execSQL(CATEGORY_TABLE_CREATE);

        // Create the default category
        createDefaults(db);
    }

    private void createDefaults(SQLiteDatabase db) {
        ContentValues defaultCategories = new ContentValues();
        defaultCategories.put("name", "Default");
        db.insert(CATEGORY_TABLE_NAME, null, defaultCategories);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // Nothing useful is on it anyways
            db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE_NAME);
            db.execSQL(TODO_TABLE_CREATE);
            db.execSQL(CATEGORY_TABLE_CREATE);

            createDefaults(db);
        }
    }

    public void updateTask(int taskid, boolean done) {
        ContentValues v = new ContentValues();
        v.put("done", done);
        getWritableDatabase().update(TODO_TABLE_NAME, v, "id = " + Integer.toString(taskid), null);
    }

    public HashMap<Integer, String> getCategories() {
        HashMap<Integer, String> categories = new HashMap<Integer, String>();

        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, null, null, null, null, "id");
        while (c.moveToNext()) {
            categories.put(c.getInt(0), c.getString(1));
        }
        c.close();

        return categories;
    }

    public Cursor getTasks(int id) {
        Cursor c = getReadableDatabase().query(
                TODO_TABLE_NAME, null, "category_id = " + Integer.toString(id), null, null, null, "done DESC"
        );

        return c;
    }

    public void deleteTask(int id) {
        getWritableDatabase().delete(TODO_TABLE_NAME, "id = " + Integer.toString(id), null);
    }

    public void insertCategory(String name) {
        if (name.isEmpty()) return;

        ContentValues v = new ContentValues();
        v.put("name", name);
        getWritableDatabase().insert(CATEGORY_TABLE_NAME, null, v);
    }

    public void deleteCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(CATEGORY_TABLE_NAME, "id = " + Integer.toString(id), null);
            db.delete(TODO_TABLE_NAME, "category_id = " + Integer.toString(id), null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public int getNumCategories() {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, null, null, null, null, null);
        int ret = c.getCount();
        c.close();
        return ret;
    }

    public int getNextCategoryId() {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, null, null, null, null, null);
        c.moveToNext();
        int id = c.getInt(0);
        c.close();
        return id;
    }

    public String getCategoryName(int id) {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, "id = " + Integer.toString(id), null, null, null, null);
        c.moveToNext();
        String name = c.getString(1);
        c.close();
        return name;
    }

    public void insertTask(ToDoItem item) {
        if (item.isValid()) {
            ContentValues v = new ContentValues();
            v.put("category_id", item.category);
            v.put("done", item.done);
            v.put("description", item.description);

            getWritableDatabase().insert(TODO_TABLE_NAME, null, v);
        }
    }
}
