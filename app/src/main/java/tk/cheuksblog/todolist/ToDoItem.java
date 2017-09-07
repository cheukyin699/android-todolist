package tk.cheuksblog.todolist;

import android.database.Cursor;

class ToDoItem {
    boolean done;
    String description;
    int category;
    public int id;

    ToDoItem(int category, boolean done, String description) {
        this.category = category;
        this.done = done;
        this.description = description;
    }

    ToDoItem(Cursor c) {
        this.id = c.getInt(0);
        this.category = c.getInt(1);
        this.done = c.getInt(2) > 0;
        this.description = c.getString(3);
    }
}
