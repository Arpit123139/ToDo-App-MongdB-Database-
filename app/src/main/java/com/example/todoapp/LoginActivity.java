package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button registerBtn;

    private Button loginBtn ;
    private EditText email_ET,password_ET;
    ProgressBar progressBar;
    private String email,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerBtn=findViewById(R.id.registerBtn);
        loginBtn=findViewById(R.id.loginBtn);


        email_ET=findViewById(R.id.email_ET);
        password_ET=findViewById(R.id.password_ET);


        progressBar=findViewById(R.id.progress_bar);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email=email_ET.getText().toString();
                password=password_ET.getText().toString();
                if(validate(view)){
                   LoginUser(view);
                }
            }
        });
    }

    private void LoginUser(View view) {
        progressBar.setVisibility(View.VISIBLE);
        //key Value Structure

        HashMap<String,String> parameter=new HashMap<>();
        parameter.put("email",email);
        parameter.put("password",password);

        String apiKey=" https://todoapparpit.herokuapp.com/api/todo/auth/login";
        //Json Object Request
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(parameter), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //On Success Perform this Functionality
                try {
                    if(response.getBoolean("success")){
                        String token=response.getString("token");
                        Toast.makeText(LoginActivity.this,token,Toast.LENGTH_LONG).show();

                         startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                    progressBar.setVisibility(View.GONE);
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
                        Toast.makeText(LoginActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return parameter;
            }
        };

        //On network issue it will retry
        int socketTime=3000;
        RetryPolicy policy=new DefaultRetryPolicy(socketTime,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        //Adding the request to the RequestQueue
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    public boolean validate(View view){
        boolean isValid=false;
        if(!TextUtils.isEmpty(email)){
            isValid=true;
        }else{
            Toast.makeText(this,"Plz Enter the valid email",Toast.LENGTH_LONG).show();
        }
        if(!TextUtils.isEmpty(password)){
            isValid=true;
        }else{
            Toast.makeText(this,"Plz Enter the valid Password",Toast.LENGTH_LONG).show();
        }
        return isValid;

    }
}