package com.example.mavunohub;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateReportActivity extends AppCompatActivity {

    private Spinner spinnerReportType;
    private Button btnGenerate, btnSavePDF;
    private TextView tvReportPreview;

    private FirebaseFirestore db;
    private String currentUserId;
    private String reportType = "Daily";
    private String reportContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        spinnerReportType = findViewById(R.id.spinnerReportType);
        btnGenerate = findViewById(R.id.btnGenerateReport);
        btnSavePDF = findViewById(R.id.btnSavePDF);
        tvReportPreview = findViewById(R.id.tvReportPreview);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Daily", "Weekly"});
        spinnerReportType.setAdapter(adapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportType = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnGenerate.setOnClickListener(v -> generateReport());
        btnSavePDF.setOnClickListener(v -> savePDFReport());
    }

    private void generateReport() {
        tvReportPreview.setText("Generating report...");
        btnSavePDF.setVisibility(View.GONE);

        AtomicInteger totalUploaded = new AtomicInteger(0);
        AtomicInteger totalSold = new AtomicInteger(0);
        AtomicInteger totalEarnings = new AtomicInteger(0);
        Map<String, ProductStats> productStatsMap = new HashMap<>();

        // Step 1: Get products uploaded
        db.collection("products").whereEqualTo("sellerId", currentUserId).get().addOnSuccessListener(productsSnapshot -> {
            for (QueryDocumentSnapshot doc : productsSnapshot) {
                String productName = doc.getString("name");
                productStatsMap.putIfAbsent(productName, new ProductStats(productName));
                totalUploaded.incrementAndGet();
            }

            // Step 2: Get orders for this farmer
            db.collection("orders").whereEqualTo("farmerId", currentUserId).get().addOnSuccessListener(orderSnapshot -> {
                for (QueryDocumentSnapshot doc : orderSnapshot) {
                    List<Map<String, Object>> products = (List<Map<String, Object>>) doc.get("products");
                    if (products != null) {
                        for (Map<String, Object> item : products) {
                            String status = (String) item.get("status");
                            if (!"Sold".equalsIgnoreCase(status)) continue;

                            String name = (String) item.get("name");
                            long quantity = ((Number) item.get("quantity")).longValue();
                            double totalPrice = ((Number) item.get("totalPrice")).doubleValue();

                            productStatsMap.putIfAbsent(name, new ProductStats(name));
                            ProductStats stats = productStatsMap.get(name);
                            stats.quantitySold += quantity;
                            stats.totalEarned += totalPrice;

                            totalSold.addAndGet((int) quantity);
                            totalEarnings.addAndGet((int) totalPrice);
                        }
                    }
                }

                buildReport(totalUploaded.get(), totalSold.get(), totalEarnings.get(), productStatsMap);
            }).addOnFailureListener(e -> {
                tvReportPreview.setText("Failed to fetch orders.");
                e.printStackTrace();
            });
        }).addOnFailureListener(e -> {
            tvReportPreview.setText("Failed to fetch products.");
            e.printStackTrace();
        });
    }

    private void buildReport(int uploaded, int sold, int earnings, Map<String, ProductStats> breakdown) {
        String date = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(new Date());
        StringBuilder report = new StringBuilder();

        report.append("MavunoHub – Farmer Report (").append(reportType).append(")\n\n");
        report.append("Generated on: ").append(date).append("\n");
        report.append("Farmer ID: ").append(currentUserId).append("\n\n");

        report.append("Summary:\n");
        report.append("-----------------------------\n");
        report.append("Total Products Uploaded: ").append(uploaded).append("\n");
        report.append("Total Products Sold: ").append(sold).append("\n");
        report.append("Total Earnings: KES ").append(earnings).append("\n\n");

        report.append("Breakdown:\n");
        report.append("-----------------------------\n");
        int count = 1;
        for (ProductStats stat : breakdown.values()) {
            report.append(count++).append(". Product: ").append(stat.name).append("\n")
                    .append("   Quantity Sold: ").append(stat.quantitySold).append("\n")
                    .append("   Total Earned: KES ").append((int) stat.totalEarned).append("\n\n");
        }

        report.append("-----------------------------\n");
        report.append("Thank you for using MavunoHub!\n");

        reportContent = report.toString();
        tvReportPreview.setText(reportContent);
        btnSavePDF.setVisibility(View.VISIBLE);
    }

    private void savePDFReport() {
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FarmerReport_" + reportType + ".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);
            doc.add(new Paragraph(reportContent));
            doc.close();
            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private static class ProductStats {
        String name;
        long quantitySold = 0;
        double totalEarned = 0.0;

        ProductStats(String name) {
            this.name = name;
        }
    }
}
