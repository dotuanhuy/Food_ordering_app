package com.example.project_btl_android;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ViewProductManagementActivity extends AppCompatActivity {
    ImageView imgVProductImageInViewProduct;
    TextView txtCategoryInViewProduct, txtProductNameInViewProduct, txtProductPriceInViewProduct,
            txtProductDescriptionInViewProduct, txtProductQuantityInViewProduct, txtBackToManagementHomepageFromView;
    Product product = new Product();
    String pId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product_management);

        imgVProductImageInViewProduct = findViewById(R.id.imgVProductImageInViewProduct);
        txtCategoryInViewProduct = findViewById(R.id.txtCategoryInViewProduct);
        txtProductNameInViewProduct = findViewById(R.id.txtProductNameInViewProduct);
        txtProductPriceInViewProduct = findViewById(R.id.txtProductPriceInViewProduct);
        txtProductDescriptionInViewProduct = findViewById(R.id.txtProductDescriptionInViewProduct);
        txtProductQuantityInViewProduct = findViewById(R.id.txtProductQuantityInViewProduct);
        txtBackToManagementHomepageFromView = findViewById(R.id.txtBackToManagementHomepageFromView);

        pId = getIntent().getStringExtra("product_id");
        product.getProductById(pId).thenAccept(p -> {
            product = p;
            CategoryService categoryService = new CategoryService();
            categoryService.getCategoryNameById(product.getcategoryId()).thenAccept(categoryName -> {
                Glide.with(this)
                        .load(product.getImage())
                        .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                        .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                        .into(imgVProductImageInViewProduct);
                txtCategoryInViewProduct.setText(categoryName);
                txtProductNameInViewProduct.setText(product.getName());
                txtProductPriceInViewProduct.setText(product.getPrice()+ " VND");
                txtProductDescriptionInViewProduct.setText(product.getDescription());
                txtProductQuantityInViewProduct.setText(product.getQuantity() +"");
            }).exceptionally(ex -> {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            });
        }).exceptionally(ex -> {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });

        txtBackToManagementHomepageFromView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}