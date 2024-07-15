package com.example.project_btl_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/** @noinspection ALL*/
public class AddProductManagementActivity extends AppCompatActivity {
    ArrayList<Category> myListCategory;
    EditText edtProductNameInAddProduct, edtProductPriceInAddProduct, edtProductDescriptionInAddProduct, edtProductQuantityInAddProduct;
    TextView txtBackToProductManagementFromAdd;
    Spinner spnCategoryInAddProduct;
    Button btnChooseImageInAddProduct, btnAdd;
    private String idcat;
    ImageView imgVProductImage;
    Uri selectedImageUri;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    Product product = new Product();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_management);

        edtProductNameInAddProduct = findViewById(R.id.edtProductNameInAddProduct);
        edtProductPriceInAddProduct = findViewById(R.id.edtProductPriceInAddProduct);
        edtProductDescriptionInAddProduct = findViewById(R.id.edtProductDescriptionInAddProduct);
        edtProductQuantityInAddProduct = findViewById(R.id.edtProductQuantityInAddProduct);
        txtBackToProductManagementFromAdd = findViewById(R.id.txtBackToProductManagementFromAdd);
        spnCategoryInAddProduct = findViewById(R.id.spnCategoryInAddProduct);
        btnChooseImageInAddProduct = findViewById(R.id.btnChooseImageInAddProduct);
        imgVProductImage = findViewById(R.id.imgVProductImage);
//        imgVProductImage.setVisibility(View.GONE);
        btnAdd = findViewById(R.id.btnAdd);
        myListCategory = new ArrayList<>();

        txtBackToProductManagementFromAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        CategoryService categoryService = new CategoryService();
        categoryService.getAllCategories().thenAccept(categories -> {
            myListCategory = categories;
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(AddProductManagementActivity.this, android.R.layout.simple_spinner_item, myListCategory) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    // Hiển thị tên của category ở vị trí position
                    textView.setText(myListCategory.get(position).getName());
                    return textView;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                    // Hiển thị tên của category ở vị trí position trong danh sách drop-down
                    textView.setText(myListCategory.get(position).getName());
                    return textView;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCategoryInAddProduct.setAdapter(adapter);
        }).exceptionally(e -> {
            // Xử lý khi xảy ra lỗi
            Toast.makeText(AddProductManagementActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });

        btnChooseImageInAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        spnCategoryInAddProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                idcat = category.getId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtProductNameInAddProduct.getText().toString().trim();
                String price = edtProductPriceInAddProduct.getText().toString().trim();
                double priceNumber;
                String des = edtProductDescriptionInAddProduct.getText().toString().trim();
                String quant = edtProductQuantityInAddProduct.getText().toString().trim();
                int quantity;

                if (name.isEmpty()) {
                    edtProductNameInAddProduct.setError("Vui lòng nhập tên sản phẩm");
                    edtProductNameInAddProduct.requestFocus();
                    return;
                }
                if (price.isEmpty()) {
                    edtProductPriceInAddProduct.setError("Vui lòng nhập giá sản phẩm");
                    edtProductPriceInAddProduct.requestFocus();
                    return;
                } else {
                    priceNumber = Double.parseDouble(price);
                }

                if (quant.isEmpty()) {
                    edtProductQuantityInAddProduct.setError("Vui lòng nhập số lượng sản phẩm");
                    edtProductQuantityInAddProduct.requestFocus();
                    return;
                } else {
                    quantity = Integer.parseInt(quant);
                }

                if(selectedImageUri == null){
                    Toast.makeText(AddProductManagementActivity.this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnAdd.setEnabled(false);
                product.uploadImage(selectedImageUri)
                        .thenAccept(imageUrl -> {
                            // Xử lý khi tải lên thành công và nhận được URL của ảnh
                            ProductDb productDb = new ProductDb("123", idcat, name, priceNumber,  des, quantity, imageUrl);
                            product.addProduct(productDb).thenAccept(aVoid -> {
                                btnAdd.setEnabled(true);
                                Intent intenttoProductManagement = new Intent(AddProductManagementActivity.this, ProductManagementActivity.class);
                                startActivity(intenttoProductManagement);
                                Toast.makeText(AddProductManagementActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                            }).exceptionally(e -> {
                                // Xử lý khi xảy ra lỗi
                                Toast.makeText(AddProductManagementActivity.this, "Thêm sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                                return null;
                            });
                        })
                        .exceptionally(e -> {
                            // Xử lý khi xảy ra lỗi
                            Toast.makeText(AddProductManagementActivity.this, "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show();
                            return null;
                        });
            }
        });

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imgVProductImage);
        }
    }

    // Phương thức để lấy dung lượng của file từ Uri
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddProductManagementActivity.this, ProductManagementActivity.class);
        startActivity(intent);
    }
}