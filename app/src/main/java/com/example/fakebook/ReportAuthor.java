package com.example.fakebook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportAuthor extends AppCompatActivity {

    FirebaseFirestore firestoreDB;

    Button buttonSubmitReport;
    EditText etReportTitle, etReportDetail;
    TextView tvHeaderTitle;
    ImageButton ivReturn;

    String reportTitle, reportDetail, postId, uid, authorId, authorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_author);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreDB = FirebaseFirestore.getInstance();

        postId = getIntent().getStringExtra("postId");
        uid = getIntent().getStringExtra("uid");
        authorId = getIntent().getStringExtra("authorId");
        authorName = getIntent().getStringExtra("authorFirstName");

        buttonSubmitReport = findViewById(R.id.submit_report);
        etReportDetail = findViewById(R.id.report_detail);
        etReportTitle = findViewById(R.id.report_title);
        tvHeaderTitle = findViewById(R.id.header_title);

        String newHeader = "Report " + authorName;
        tvHeaderTitle.setText(newHeader);

        buttonSubmitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDetail = etReportDetail.getText().toString();
                reportTitle = etReportTitle.getText().toString();

                if (!reportTitle.isEmpty() && !reportDetail.isEmpty()) {
                    submitReport(reportTitle, reportDetail, postId, uid, authorId);
                }
            }
        });

        ivReturn = findViewById(R.id.back_button);

        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void submitReport(String title, String detail, String postId, String uid, String authorId) {

        Date date = new Date();

        DocumentReference docRef = firestoreDB.collection("REPORTS").document();

        String docId = docRef.getId();

        Map<String, Object> newDoc = new HashMap<>();
        newDoc.put("reportType", "User Report");
        newDoc.put("reportId", docId); // Add the doc ID
        newDoc.put("reportTitle", title);
//        newDoc.put("postID", postId);
        newDoc.put("reportStatus", "UNRESOLVED");
        newDoc.put("reportDetail", detail);
        newDoc.put("reportedUserID", authorId);
        newDoc.put("reporterID", uid);
        newDoc.put("reportDate", date);

        docRef.set(newDoc)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ReportAuthor.this, "Report submitted successfully", Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        finish();
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    Log.d("REPORT ERROR", "error " + e);
                });
    }
}