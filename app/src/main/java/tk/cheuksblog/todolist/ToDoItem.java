package tk.cheuksblog.todolist;

import android.database.Cursor;

/**
 * Created by cheukyin699 on 8/16/17.
 */

public class ToDoItem {
    public boolean done;
    public String description;
    public int category;
    public int id;

    public ToDoItem(int category, boolean done, String description) {
        this.category = category;
        this.done = done;
        this.description = description;
    }

    public ToDoItem(Cursor c) {
        this.id = c.getInt(0);
        this.category = c.getInt(1);
        this.done = c.getInt(2) > 0;
        this.description = c.getString(3);
    }

    public boolean isValid() {
        return !description.isEmpty();
    }
}
