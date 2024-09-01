package com.example.queuesystem;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText txtName;
    private EditText txtNumber;
    private Spinner spinnerDepartment;
    private Button btnOkay;
    private static final String API_URL = "http://192.168.1.5:8080/api/queues";

    private Dialog dialog;
    private Button btnPrint, btnEdit;
    private TextView txtNameConfirmation, txtStudentNumberConfirmation, txtDepartmentConfirmation, txtQueueNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Main activity UI elements
        txtName = findViewById(R.id.txtName);
        txtNumber = findViewById(R.id.txtNumber);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        btnOkay = findViewById(R.id.btnOkay);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.departments, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        spinnerDepartment.setAdapter(adapter);


        // Set up the Okay button click listener
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txtName.getText().toString();
                String studentNum = txtNumber.getText().toString();
                String department = spinnerDepartment.getSelectedItem().toString();

                if (!name.isEmpty() && !studentNum.isEmpty()) {
                    // Fetch the queue number from the API
                    getQueueNumber(MainActivity.this, name, studentNum, department);
                } else {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to show the confirmation dialog
    private void showConfirmationDialog(String name, String studentNum, String department, String queueNum) {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.activity_confirmation_box);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.activity_confirmation_bg));
        dialog.setCancelable(false);

        // Find dialog UI elements
        btnPrint = dialog.findViewById(R.id.btnPrint);
        btnEdit = dialog.findViewById(R.id.btnEdit);
        txtNameConfirmation = dialog.findViewById(R.id.txtConfirmationName);
        txtStudentNumberConfirmation = dialog.findViewById(R.id.txtConfirmationStudentNumber);
        txtDepartmentConfirmation = dialog.findViewById(R.id.txtConfirmationDepartment);
        txtQueueNum = dialog.findViewById(R.id.txtQueueNum);

        // Set the text of the confirmation dialog
        txtNameConfirmation.setText(name);
        txtStudentNumberConfirmation.setText(studentNum);
        txtDepartmentConfirmation.setText(department);
        txtQueueNum.setText(queueNum);

        dialog.show();

        // Set the Print button click listener
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                sendPrintRequest(name, studentNum, department, queueNum);
                txtName.setText("");
                txtNumber.setText("");
            }
        });

        // Set the Edit button click listener
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void sendPrintRequest(String name, String studentNum, String department, String queueNum) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);


        // Create JSON object with the data
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("student_number", studentNum);
            jsonBody.put("department", department);
            jsonBody.put("queue_number", queueNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                API_URL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Handle the response from the server
                            String result = response.getString("message");
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(MainActivity.this, "Failed to send print request: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // Method to get the queue number from the API
    public static void getQueueNumber(Context context, String name, String studentNum, String department) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Construct the API URL with the department parameter
        String apiUrl = API_URL + "/generate-queue-number?department=" + Uri.encode(department);

        // Create a GET request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Assuming the queue number is in the JSON response under a key "queue_number"
                            String queueNumber = response.getString("queue_number");

                            // Show confirmation dialog with the queue number
                            ((MainActivity) context).showConfirmationDialog(name, studentNum, department, queueNumber);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Failed to parse response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(context, "Failed to get queue number: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


}
