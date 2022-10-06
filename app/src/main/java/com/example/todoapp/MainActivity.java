package com.example.todoapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

   // Button logout;
    SharedPreferenceClass sharedPreferenceClass;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceClass=new SharedPreferenceClass(this);

        /************************************************8INITIALIZE THE LAYOUTS ****************************************/
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigationView);
        toolbar= findViewById(R.id.toolbar1);
        //Set the ActionBar as the Toolbar
        setSupportActionBar(toolbar);

        /****************************wHAT HAPPEN WHEN WE CLICK ITEM IN MENU OF NAVIGATION DRAWER************************/
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

               setDrawerClick(item.getItemId());
               item.setChecked(true);                      // The item will be checked when selected
                drawerLayout.closeDrawers();
                return true;

            }
        });

        initDrawer();

    }

    private void initDrawer() {
        // When The Screen Opens after Login we replace the space below toolbar that is the FrameLayout by Hello Fragment
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction ft=manager.beginTransaction();
        ft.replace(R.id.content,new HomeFragment());                  // content is the id of the FrameLayout ..........
        ft.commit();

        //to show the Toggle Button
        drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Setting the color to the ToggleButton
        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(drawerToggle);

    }

    // to sync the toggleButton means that it will rotate whr=en we swipe the navigation Bar and also to show the button in the toolbar
    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setDrawerClick(int itemId) {
        switch (itemId){
            case R.id.action_finished_task:
                getSupportFragmentManager().beginTransaction().replace(R.id.content,new FinishedTaskFragment()).commit();
                break;
            case R.id.action_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.content,new HomeFragment()).commit();
                break;
            case R.id.action_logout:
                sharedPreferenceClass.clear();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                break;

        }
    }

    //For the Toolbar


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                   // Inflate the menu items in the toolbar like refresh and Share
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
}