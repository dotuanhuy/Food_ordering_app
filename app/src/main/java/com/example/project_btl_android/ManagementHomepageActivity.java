package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/** @noinspection ALL*/
public class ManagementHomepageActivity extends AppCompatActivity {
    CardView cVCategoryManagement, cVProductManagement, cVStatisticManagement, cVOrderManagement;
    long backPressTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_homepage);

        cVCategoryManagement = findViewById(R.id.cVCategoryManagement);
        cVProductManagement = findViewById(R.id.cVProductManagement);
        cVStatisticManagement = findViewById(R.id.cVStatisticManagement);
        cVOrderManagement = findViewById(R.id.cVOrderManagement);

        cVCategoryManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagementHomepageActivity.this, CategoryManagementActivity.class);
                startActivity(intent);
            }
        });
        cVProductManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagementHomepageActivity.this, ProductManagementActivity.class);
                startActivity(intent);
            }
        });
        cVStatisticManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(ManagementHomepageActivity.this, StatisticManagementActivity.class);
                    startActivity(intent);
                }
                catch (Exception e){
                    Toast.makeText(ManagementHomepageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        cVOrderManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagementHomepageActivity.this, OrderManage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 3000 > System.currentTimeMillis()){
            Intent intent = new Intent(ManagementHomepageActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        else{
            Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
        }
        backPressTime = System.currentTimeMillis();
    }
}