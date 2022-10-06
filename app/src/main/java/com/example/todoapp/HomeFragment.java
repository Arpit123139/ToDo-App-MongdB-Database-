package com.example.todoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    FloatingActionButton floatingActionButton;
    SharedPreferenceClass sharedPreferenceClass;
    String token;

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_home, container, false);

        floatingActionButton =view.findViewById(R.id.add_task_btn);
        sharedPreferenceClass=new SharedPreferenceClass(getContext());

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
        return view;
    }

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

    //Adding the Task To the Server Same as Login
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

}