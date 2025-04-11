package com.example.mavunohub;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GenerateReportActivity extends AppCompatActivity {

    private Spinner reportSpinner;
    private Button btnGenerateReport;
    private String selectedReportType = "Daily"; // Default selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        // Initialize views
        reportSpinner = findViewById(R.id.spinnerReportType);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        // Set up the Spinner
        String[] reportOptions = {"Daily", "Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reportOptions);
        reportSpinner.setAdapter(adapter);

        // Handle Spinner selection
        reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReportType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Handle Generate Report button click
        btnGenerateReport.setOnClickListener(v -> {
            Toast.makeText(GenerateReportActivity.this, "Generating " + selectedReportType + " Report...", Toast.LENGTH_SHORT).show();
            // TODO: Implement actual report generation logic here
        });
    }
}
