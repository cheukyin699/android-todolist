package tk.cheuksblog.todolist;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ToDoOpenHelper tdHelper;
    private ToDoAdapter taskList;
    private NavigationView navigationView;
    private ToDoListFragment todoList;
    private SettingsFragment settingsFragment;
    private int selectedCategoryId;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tdHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore the preferences
        restorePreferences();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settingsFragment = new SettingsFragment();
        todoList = new ToDoListFragment();
        tdHelper = new ToDoOpenHelper(this.getApplicationContext());
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        todoList.setAddListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edit = new EditText(MainActivity.this);
                showAlertDialog("Add Task", edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int which) {
                            ToDoItem item = new ToDoItem(selectedCategoryId, false, String.valueOf(edit.getText()));

                            tdHelper.insertTask(item);

                            updateBodyUI();
                        }
                });
            }
        });
        getFragmentManager().beginTransaction()
                .add(R.id.body, todoList)
                .commit();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        updateMenuUI();

        // Default to the first category
        selectedCategoryId = tdHelper.getNextCategoryId();
        setTitle(tdHelper.getCategoryName(selectedCategoryId));

        // Get the tasks
        updateBodyUI();
    }

    public void restorePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String theme = preferences.getString("theme",
                getResources().getString(R.string.theme_default));

        if (theme.equals("light")) {
            setTheme(R.style.AppTheme_Light_NoActionBar);
            getApplication().setTheme(R.style.AppTheme_Light);
        } else if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            getApplication().setTheme(R.style.AppTheme_Dark);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.delete_item:
                View v = todoList.getListView().getChildAt(info.position);
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
        Cursor c = tdHelper.getCategories();

        menu.removeGroup(R.id.category_group);

        while (c.moveToNext()) {
            menu.add(R.id.category_group, c.getInt(0), Menu.NONE, c.getString(1));
        }

        c.close();
    }

    private void updateBodyUI() {
        Cursor c = tdHelper.getTasks(selectedCategoryId);
        ArrayList<ToDoItem> tasks = new ArrayList<>();
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
        todoList.setListAdapter(taskList);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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

        switch (id) {
            case R.id.action_settings:
                if (getFragmentManager().findFragmentByTag(SettingsFragment.TAG) == null) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.body, settingsFragment, SettingsFragment.TAG)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            case R.id.delete_category:
                // If there is only 1 existing category, don't delete it
                if (tdHelper.getNumCategories() <= 1) break;
                // Delete category
                tdHelper.deleteCategory(selectedCategoryId);
                // Reset selected
                selectedCategoryId = tdHelper.getNextCategoryId();
                setTitle(tdHelper.getCategoryName(selectedCategoryId));
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

    private void showAlertDialog(String title, EditText edit, DialogInterface.OnClickListener listener) {
        AlertDialog dlg = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setView(edit)
                .setPositiveButton("Add", listener)
                .setNegativeButton("Cancel", null)
                .create();
        dlg.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_category) {
            // Adding a category
            final EditText edit = new EditText(this);
            showAlertDialog("Add Category", edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int which) {
                    tdHelper.insertCategory(String.valueOf(edit.getText()));

                    updateMenuUI();
                        }
            });

            return true;
        } else {
            // Selecting on a category
            selectedCategoryId = id;
            setTitle(tdHelper.getCategoryName(selectedCategoryId));
            updateBodyUI();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }
}
