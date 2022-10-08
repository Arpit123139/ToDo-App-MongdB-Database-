package com.example.todoapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

   // Button logout;
    SharedPreferenceClass sharedPreferenceClass;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private TextView username,email;
    private CircleImageView avatar;

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

        /*****************************************HOW TO ACCESS THE NAVIGATION HEADER************************************/
        View headerView=navigationView.getHeaderView(0);
        username=headerView.findViewById(R.id.username);
        email=headerView.findViewById(R.id.user_email);
        avatar=headerView.findViewById(R.id.avatar);


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

        getUserProfile();

    }

    private void getUserProfile() {

        String url=" https://todoapparpit.herokuapp.com/api/todo/auth";
        String token=sharedPreferenceClass.getValue_string("token");
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if(response.getBoolean("success")){
                        JSONObject user=response.getJSONObject("user");
                        username.setText(user.getString("username"));
                        email.setText(user.getString("email"));

                        //Using Picasso to set the image
                        Picasso.with(getApplicationContext()).load(user.getString("avatar")).placeholder(R.drawable.ic_account)
                                .error(R.drawable.ic_account)
                                .into(avatar);

                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Not able to get the user "+error.toString() ,Toast.LENGTH_LONG).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String>map=new HashMap<>();
                map.put("Authorization",token);
                return map;
            }
        };
        int socketTimeOut=30000;
        RetryPolicy policy=new DefaultRetryPolicy(socketTimeOut,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
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

    /**************************************************For THE TOOLBAR****************************************************/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                   // Inflate the menu items in the toolbar like refresh and Share
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.action_share:
                Intent sharingIntent=new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String sharebody="Hey Try This todo App it uses permanent Saving of your Tssk";
                sharingIntent.putExtra(Intent.EXTRA_TEXT,sharebody);
                startActivity(Intent.createChooser(sharingIntent,"Share Via"));

                return true;

            case R.id.refresh_menu:
                FragmentManager manager=getSupportFragmentManager();
                FragmentTransaction ft=manager.beginTransaction();
                ft.replace(R.id.content,new HomeFragment());                  // content is the id of the FrameLayout ..........
                ft.commit();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}