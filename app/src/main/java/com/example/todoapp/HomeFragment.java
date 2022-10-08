package com.example.todoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.todoapp.Adapters.TodoListAdapter;
import com.example.todoapp.interfaces.RecyclerViewClickListener;
import com.example.todoapp.models.TodoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment implements RecyclerViewClickListener {
    FloatingActionButton floatingActionButton;
    SharedPreferenceClass sharedPreferenceClass;
    String token;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView empty_tv;
    ArrayList<TodoModel> arr;
    TodoListAdapter todoListAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_home, container, false);

        progressBar=view.findViewById(R.id.progress_bar);
        empty_tv=view.findViewById(R.id.empty_tv);
        recyclerView=view.findViewById(R.id.recycler_view);

        floatingActionButton =view.findViewById(R.id.add_task_btn);
        sharedPreferenceClass=new SharedPreferenceClass(getContext());

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);             // Size is fixed it is not affected by the adapter contents
        getTasks();
        return view;
    }

    /***********************************************Fetching The Data******************************************/
    private void getTasks() {
        arr=new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url="https://todoapparpit.herokuapp.com/api/todo";

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //On Success Perform this Functionality
                try {
                    if(response.getBoolean("success")) {
                        //getting the arrays
                        JSONArray jsonArray = response.getJSONArray("todos");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            //Iterating over every object and storing it in the array
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                TodoModel todoModel = new TodoModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description")
                                );
                                arr.add(todoModel);
                            }

                            todoListAdapter = new TodoListAdapter(getActivity(), arr,HomeFragment.this);
                            recyclerView.setAdapter(todoListAdapter);
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error==null || error.networkResponse==null){
                    return;
                }
                String body;
                final String statusCode=String.valueOf(error.networkResponse.statusCode);
                try {
                    body=new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject=new JSONObject(body);
                    /************************************************HANDLIND IF THE TOKEN EXPIRES**********************/
                    if(errorObject.getString("msg").equals("Token Not Valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));        // It should redirect to login page
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();

                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization",sharedPreferenceClass.getValue_string("token"));
                return headers;
            }
        };

        //On network issue it will retry
        int socketTime=3000;
        RetryPolicy policy=new DefaultRetryPolicy(socketTime,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        //Adding the request to the RequestQueue
        RequestQueue requestQueue= Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);

    }

    /*************************************************SHOW THE DIALOG BOX**************************************/
    private void showAlertDialog() {
        //Inflating the alert Dialogue Box    Try In Kotlin ....................................ALREADY THERE IN THE COURSE
        LayoutInflater inflater=getLayoutInflater();
        View alertLayout=inflater.inflate(R.layout.custom_dialog_layout,null);

        // this is a custom layout to the Alert Dialogue Box
        final EditText title_field=alertLayout.findViewById(R.id.title);
        final EditText description_field=alertLayout.findViewById(R.id.Description);

        final AlertDialog dialog=new AlertDialog.Builder(getActivity())
                .setView(alertLayout).setTitle("Add Task")
                .setPositiveButton("Add",null)
                .setNegativeButton("Cancel",null)
                .create();

        //Listener for Add and Cancel Button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton=((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title=title_field.getText().toString();
                        String description=description_field.getText().toString();

                        if(!TextUtils.isEmpty(title)){
                            addTask(title,description);
                            //Close the dialog box once the data is added
                            dialog.dismiss();


                        }else{
                            Toast.makeText(getActivity(),"Plz Enter the Title of The Task",Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
        dialog.show();

    }

    private void showUpdateDialog(String id, String title, String description) {
        LayoutInflater inflater=getLayoutInflater();
        View alertLayout=inflater.inflate(R.layout.custom_dialog_layout,null);

        // this is a custom layout to the Alert Dialogue Box
        final EditText title_field=alertLayout.findViewById(R.id.title);
        final EditText description_field=alertLayout.findViewById(R.id.Description);
        title_field.setText(title);
        description_field.setText(description);

        final AlertDialog dialog=new AlertDialog.Builder(getActivity())
                .setView(alertLayout).setTitle("Update Task")
                .setPositiveButton("Update Task",null)
                .setNegativeButton("Cancel",null)
                .create();

        //Listener for Add and Cancel Button
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton=((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title=title_field.getText().toString();
                        String description=description_field.getText().toString();

                        if(!TextUtils.isEmpty(title)){
                            UpdateTask(id,title,description);
                            //Close the dialog box once the data is added
                            dialog.dismiss();


                        }else{
                            Toast.makeText(getActivity(),"Plz Enter the Title of The Task",Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
        dialog.show();
    }

    private void showDeleteDialog(String id, int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Are you want to delete the task ?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No",null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTodo(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    private void showFinishDialog(String id, int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Are you want to finish task ?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No",null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateToFinishTodo(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }


    /*****************************************Adding the NEW Task To the Server Same as Login***********************/
    private void addTask(String title, String description) {

        HashMap<String,String> parameter=new HashMap<>();
        parameter.put("title",title);
        parameter.put("description",description);

        String apiKey=" https://todoapparpit.herokuapp.com/api/todo";
        //Json Object Request
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(parameter), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //On Success Perform this Functionality
                try {
                    if(response.getBoolean("success")){
//                        String title=response.getString("title");
//                        String description=response.getString("description");
                        //Store the token in Shared Preference
                        //sharedPreferenceClass.setValue_string("token",token);
                        Toast.makeText(getActivity(),"Added Successfully ",Toast.LENGTH_LONG).show();
                        getTasks();

                        //startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }
                    //progressBar.setVisibility(View.GONE);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                    //progressBar.setVisibility(View.GONE);
                }

            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response != null) {
                    try {
                        //Converting the jsonResponse
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,  "utf-8"));
                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(getActivity(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                        //progressBar.setVisibility(View.GONE);
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                        //progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization",sharedPreferenceClass.getValue_string("token"));
                return headers;
            }
        };

        //On network issue it will retry
        int socketTime=3000;
        RetryPolicy policy=new DefaultRetryPolicy(socketTime,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        //Adding the request to the RequestQueue
        RequestQueue requestQueue= Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    /*********************************************Updating the Task*************************************************/
    private void UpdateTask(String id,String title, String description) {
        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        String url=" https://todoapparpit.herokuapp.com/api/todo/"+id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                getTasks();
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", token);
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);


        Toast.makeText(getActivity(),"Update Clickesd",Toast.LENGTH_LONG).show();
    }
/*************************************************DELETING THE TASK*************************************************/
    private void deleteTodo(String id, int position) {

        String url="  https://todoapparpit.herokuapp.com/api/todo/"+id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        arr.remove(position);
                        todoListAdapter.notifyItemRemoved(position);         //or call getTask() again
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    /*********************************************Marking The Task As DONE******************************************/
    private void updateToFinishTodo(String id,final int position) {
        String url = " https://todoapparpit.herokuapp.com/api/todo/" + id;
        HashMap<String, String> body = new HashMap<>();
        body.put("finished", "true");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                arr.remove(position);              // Deleting from the ArrayList but it remains in the Database
                                //getTasks();
                                todoListAdapter.notifyItemRemoved(position);
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {
        showUpdateDialog(arr.get(position).getId(),arr.get(position).getTitle(),arr.get(position).getDescription());

    }

    @Override
    public void onEditButtonClick(int position) {

        showUpdateDialog(arr.get(position).getId(),arr.get(position).getTitle(),arr.get(position).getDescription());

    }

    @Override
    public void onDeleteButtonClick(int position) {
        showDeleteDialog(arr.get(position).getId(),position);
    }



    @Override
    public void onDoneButtonClick(int position) {
       showFinishDialog(arr.get(position).getId(),position);

    }
}