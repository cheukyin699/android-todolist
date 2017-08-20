package tk.cheuksblog.todolist;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ToDoOpenHelper tdHelper;
    private ToDoAdapter taskList;
    private ArrayList<ToDoItem> tasks;
    private NavigationView navigationView;
    private ListView todoList;
    private int selectedCategoryId;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tdHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edit = new EditText(MainActivity.this);
                AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Add Task")
                        .setView(edit)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface di, int which) {
                                ToDoItem item = new ToDoItem(selectedCategoryId, false, String.valueOf(edit.getText()));

                                tdHelper.insertTask(item);

                                updateBodyUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dlg.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        tdHelper = new ToDoOpenHelper(this.getApplicationContext());
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateMenuUI();

        // Default to the first category
        selectedCategoryId = tdHelper.getNextCategoryId();
        setTitle(tdHelper.getCategoryName(selectedCategoryId));

        todoList = (ListView) findViewById(R.id.todoList);
        registerForContextMenu(todoList);

        // Get the tasks
        updateBodyUI();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()) {
            case R.id.todoList:
                MenuInflater inf = getMenuInflater();
                inf.inflate(R.menu.delete_contextmenu, menu);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.delete_item:
                View v = todoList.getChildAt(info.position);
                ToDoAdapter.ToDoView tItem = (ToDoAdapter.ToDoView) v.getTag();
                tdHelper.deleteTask(tItem.taskid);

                updateBodyUI();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateMenuUI() {
        // Append categories to navigation
        Menu menu = navigationView.getMenu();
        HashMap<Integer, String> categories = tdHelper.getCategories();

        menu.removeGroup(R.id.category_group);

        for (Integer id : categories.keySet()) {
            MenuItem item = menu.add(R.id.category_group, id, Menu.NONE, categories.get(id));
        }
    }

    private void updateBodyUI() {
        Cursor c = tdHelper.getTasks(selectedCategoryId);
        tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new ToDoItem(c));
        }
        c.close();

        if (taskList == null) {
            taskList = new ToDoAdapter(this, R.layout.todo_item, tasks);
        } else {
            taskList.clear();
            taskList.addAll(tasks);
            taskList.notifyDataSetChanged();
        }

        // Set ListView adapter
        todoList.setAdapter(taskList);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            getFragmentManager().popBackStack();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                getFragmentManager().beginTransaction()
                        .add(new SettingsFragment(), "settings")
                        .commit();
                return true;
            case R.id.delete_category:
                // If there is only 1 existing category, don't delete it
                if (tdHelper.getNumCategories() <= 1) break;
                // Delete category
                tdHelper.deleteCategory(selectedCategoryId);
                // Reset selected
                selectedCategoryId = tdHelper.getNextCategoryId();
                // Update displays
                updateMenuUI();
                updateBodyUI();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateToDo(View v) {
        CheckBox cb = (CheckBox) v;
        View parent = (View) cb.getParent();
        ToDoAdapter.ToDoView tdv = (ToDoAdapter.ToDoView) parent.getTag();
        boolean checked = cb.isChecked();
        int id = tdv.taskid;

        tdHelper.updateTask(id, checked);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_category) {
            final EditText edit = new EditText(MainActivity.this);
            AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Add Category")
                    .setView(edit)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int which) {
                            tdHelper.insertCategory(String.valueOf(edit.getText()));

                            updateMenuUI();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dlg.show();

            return true;
        }

        selectedCategoryId = id;
        setTitle(tdHelper.getCategoryName(selectedCategoryId));
        updateBodyUI();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
