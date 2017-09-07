package tk.cheuksblog.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ToDoOpenHelper extends SQLiteOpenHelper {
    private static final String TODO_TABLE_NAME = "todo";
    private static final String CATEGORY_TABLE_NAME = "categories";
    private static final int TODO_VERSION = 1;

    private static final String TODO_FIELD_ID = "id";
    private static final String TODO_FIELD_CID = "category_id";
    private static final String TODO_FIELD_DONE = "done";
    private static final String TODO_FIELD_DESC = "description";
    private static final String CAT_FIELD_ID = "id";
    private static final String CAT_FIELD_NAME = "name";

    private static final String TODO_TABLE_CREATE =
            "CREATE TABLE " + TODO_TABLE_NAME + " (" +
                    TODO_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TODO_FIELD_CID + " INTEGER," +
                    TODO_FIELD_DONE + " BOOLEAN," +
                    TODO_FIELD_DESC + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + TODO_FIELD_CID + ") REFERENCES " + CATEGORY_TABLE_NAME + "(" + TODO_FIELD_ID + ")" +
            ");";
    private static final String CATEGORY_TABLE_CREATE =
            "CREATE TABLE " + CATEGORY_TABLE_NAME + "(" +
                    CAT_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CAT_FIELD_NAME + " TEXT" +
            ");";

    private int numCategories;

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
        defaultCategories.put(CAT_FIELD_NAME, "Default");
        db.insert(CATEGORY_TABLE_NAME, null, defaultCategories);
        numCategories = 1;
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

    void updateTask(int taskid, boolean done) {
        ContentValues v = new ContentValues();
        v.put(TODO_FIELD_DONE, done);
        getWritableDatabase().update(TODO_TABLE_NAME, v, TODO_FIELD_ID + " = " + Integer.toString(taskid), null);
    }

    Cursor getCategories() {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, null, null, null, null, CAT_FIELD_ID);
        numCategories = c.getCount();
        return c;
    }

    Cursor getTasks(int id) {
        return getReadableDatabase().query(
                TODO_TABLE_NAME, null, CAT_FIELD_ID + " = " + Integer.toString(id), null, null, null, TODO_FIELD_DONE + " DESC"
        );
    }

    void deleteTask(int id) {
        getWritableDatabase().delete(TODO_TABLE_NAME, TODO_FIELD_ID + " = " + Integer.toString(id), null);
    }

    void insertCategory(String name) {
        if (name.isEmpty()) return;

        ContentValues v = new ContentValues();
        v.put(CAT_FIELD_NAME, name);
        getWritableDatabase().insert(CATEGORY_TABLE_NAME, null, v);

        numCategories++;
    }

    void deleteCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(CATEGORY_TABLE_NAME, CAT_FIELD_ID + " = " + Integer.toString(id), null);
            db.delete(TODO_TABLE_NAME, TODO_FIELD_CID + " = " + Integer.toString(id), null);
            db.setTransactionSuccessful();

            numCategories--;
        } finally {
            db.endTransaction();
        }
    }

    int getNumCategories() {
        return numCategories;
    }

    int getNextCategoryId() {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, null, null, null, null, null);
        c.moveToNext();
        int id = c.getInt(0);
        c.close();
        return id;
    }

    String getCategoryName(int id) {
        Cursor c = getReadableDatabase().query(CATEGORY_TABLE_NAME, null, CAT_FIELD_ID + " = " + Integer.toString(id), null, null, null, null);
        c.moveToNext();
        String name = c.getString(1);
        c.close();
        return name;
    }

    void insertTask(ToDoItem item) {
        if (!item.description.isEmpty()) {
            ContentValues v = new ContentValues();
            v.put(TODO_FIELD_CID, item.category);
            v.put(TODO_FIELD_DONE, item.done);
            v.put(TODO_FIELD_DESC, item.description);

            getWritableDatabase().insert(TODO_TABLE_NAME, null, v);
        }
    }
}
