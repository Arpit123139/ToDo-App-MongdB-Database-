package com.example.todoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.todoapp.Adapters.FinishedTaskAdapter;
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


public class FinishedTaskFragment extends Fragment implements RecyclerViewClickListener {
    SharedPreferenceClass sharedPreferenceClass;
    String token;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView empty_tv;
    ArrayList<TodoModel> arr;
    FinishedTaskAdapter finishedListAdapter;

    public FinishedTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_finished_task, container, false);
        progressBar=view.findViewById(R.id.progress_bar);
        empty_tv=view.findViewById(R.id.empty_tv);
        recyclerView=view.findViewById(R.id.recycler_view);


        sharedPreferenceClass=new SharedPreferenceClass(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);             // Size is fixed it is not affected by the adapter contents
        getTasks();
        return view;

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


    /***********************************************Fetching The Data******************************************/
    private void getTasks() {
        arr=new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url="https://todoapparpit.herokuapp.com/api/todo/finished";

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

                            finishedListAdapter = new FinishedTaskAdapter(getActivity(), arr,FinishedTaskFragment.this);
                            recyclerView.setAdapter(finishedListAdapter);
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
                       finishedListAdapter.notifyItemRemoved(position);         //or call getTask() again
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

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onDeleteButtonClick(int position) {

        showDeleteDialog(arr.get(position).getId(),position);

    }

    @Override
    public void onDoneButtonClick(int position) {

    }
}