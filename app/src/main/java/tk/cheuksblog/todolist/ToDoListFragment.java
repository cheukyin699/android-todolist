package tk.cheuksblog.todolist;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by cheukyin699 on 8/20/17.
 */

public class ToDoListFragment extends ListFragment {
    private FloatingActionButton addButton;
    private ListView list;
    private View.OnClickListener addListener;

    public void setAddListener(View.OnClickListener l) {
        addListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.todolist_fragment, container, false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.delete_contextmenu, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addButton = (FloatingActionButton) view.findViewById(R.id.fab);
        addButton.setOnClickListener(addListener);

        list = getListView();
        registerForContextMenu(list);
    }
}
