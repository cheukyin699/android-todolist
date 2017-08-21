package tk.cheuksblog.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cheukyin699 on 8/16/17.
 */

public class ToDoAdapter extends ArrayAdapter<ToDoItem> implements View.OnCreateContextMenuListener {
    public ToDoAdapter(Context ctx, int res, ArrayList<ToDoItem> list) {
        super(ctx, res, list);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    class ToDoView {
        public int taskid;
        private CheckBox todoDone;
        private TextView todoDescription;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ToDoView v = new ToDoView();
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.todo_item, parent, false);
            v.todoDone = (CheckBox) convertView.findViewById(R.id.todo_done);
            v.todoDescription = (TextView) convertView.findViewById(R.id.todo_description);

            convertView.setTag(v);
        } else {
            v = (ToDoView) convertView.getTag();
        }
        convertView.setOnCreateContextMenuListener(this);

        ToDoItem item = getItem(position);

        if (item != null) {
            v.taskid = item.id;
            v.todoDone.setChecked(item.done);
            v.todoDescription.setText(item.description);
        }
        return convertView;
    }
}
