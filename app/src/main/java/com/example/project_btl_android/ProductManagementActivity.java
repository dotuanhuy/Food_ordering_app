package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/** @noinspection ALL*/
public class ProductManagementActivity extends AppCompatActivity {
    ListView lvProduct;
    TextView txtBackToManagementHomepageFromProduct, txtVAddProduct;
    ArrayAdapterProductManagement myAdapterProductManagement;
    ArrayList<Product> myListProduct;
    ArrayList<String> myListProductName;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        txtVAddProduct = findViewById(R.id.txtVAddProduct);
        txtBackToManagementHomepageFromProduct = findViewById(R.id.txtBackToManagementHomepageFromProduct);
        lvProduct = findViewById(R.id.lvProduct);
        myListProduct = new ArrayList<>();
        myListProductName = new ArrayList<>();

        db.getReference("Product").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myListProduct.clear();
                myListProductName.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        myListProductName.add(product.getName());
                        myListProduct.add(product);
                    }
                    myAdapterProductManagement = new ArrayAdapterProductManagement(ProductManagementActivity.this, R.layout.layout_product, myListProduct);
                    lvProduct.setAdapter(myAdapterProductManagement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        txtBackToManagementHomepageFromProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductManagementActivity.this, ManagementHomepageActivity.class);
                startActivity(intent);
            }
        });

        txtVAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductManagementActivity.this, AddProductManagementActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProductManagementActivity.this, ManagementHomepageActivity.class);
        startActivity(intent);
    }
}