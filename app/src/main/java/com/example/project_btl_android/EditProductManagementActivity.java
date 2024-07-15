package com.example.project_btl_android;

import android.content.ContentValues;
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

import java.util.ArrayList;

/** @noinspection ALL*/
public class EditProductManagementActivity extends AppCompatActivity {
    ImageView imgVProductImageInEditProduct;
    EditText edtProductNameInEditProduct, edtProductPriceInEditProduct, edtProductDescriptionInEditProduct, edtProductQuantityInEditProduct;
    TextView txtBackToManagementHomepageFromEdit;
    Spinner spnCategoryInEditProduct;
    Button btnEditProduct;
    Product product = new Product();
    ArrayList<Category> myListCategory;
    String idcat;
    String pId;
    Uri selectedImageUri;
    boolean checkUploadImg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product_management);

        myListCategory = new ArrayList<>();
        imgVProductImageInEditProduct = findViewById(R.id.imgVProductImageInEditProduct);
        edtProductNameInEditProduct = findViewById(R.id.edtProductNameInEditProduct);
        edtProductPriceInEditProduct = findViewById(R.id.edtProductPriceInEditProduct);
        edtProductDescriptionInEditProduct = findViewById(R.id.edtProductDescriptionInEditProduct);
        edtProductQuantityInEditProduct = findViewById(R.id.edtProductQuantityInEditProduct);
        txtBackToManagementHomepageFromEdit = findViewById(R.id.txtBackToManagementHomepageFromEdit);
        spnCategoryInEditProduct = findViewById(R.id.spnCategoryInEditProduct);
        btnEditProduct = findViewById(R.id.btnEditProduct);

        CategoryService category = new CategoryService();
        category.getAllCategories().thenAccept(categories -> {
            myListCategory = categories;
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, myListCategory) {
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
            spnCategoryInEditProduct.setAdapter(adapter);
        }).exceptionally(e -> {
            // Xử lý khi xảy ra lỗi
            Toast.makeText(EditProductManagementActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });

        //Đưa thông tin sản phẩm lên trước khi chỉnh sửa
        pId = getIntent().getStringExtra("product_id");
        product.getProductById(pId).thenAccept(p -> {
            product = p;
            String CategoryName = null;
            for (Category c : myListCategory) {
                if (c.getId().equals(product.getcategoryId())) {
                    CategoryName = c.getName();
                    break;
                }
            }
            int position = -1;
            for (int i = 0; i < spnCategoryInEditProduct.getCount(); i++) {
                Category c = (Category) spnCategoryInEditProduct.getItemAtPosition(i);
                if (c.getName().equals(CategoryName)) {
                    position = i;
                    break;
                }
            }

            // Nếu tìm thấy vị trí, thiết lập vị trí đó cho Spinner
            if (position != -1) {
                spnCategoryInEditProduct.setSelection(position, true);
            }
            Glide.with(this)
                    .load(product.getImage())
                    .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                    .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                    .into(imgVProductImageInEditProduct);
            edtProductNameInEditProduct.setText(product.getName());
            edtProductPriceInEditProduct.setText(product.getPrice() + "");
            edtProductDescriptionInEditProduct.setText(product.getDescription());
            edtProductQuantityInEditProduct.setText(product.getQuantity() + "");
        }).exceptionally(e -> {
            // Xử lý khi xảy ra lỗi
            Toast.makeText(EditProductManagementActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });

        txtBackToManagementHomepageFromEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgVProductImageInEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        spnCategoryInEditProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                idcat = category.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtProductNameInEditProduct.getText().toString().trim();
                String price = edtProductPriceInEditProduct.getText().toString().trim();
                String des = edtProductDescriptionInEditProduct.getText().toString().trim();
                String quant = edtProductQuantityInEditProduct.getText().toString().trim();
                Double priceNumber;
                int quantity;

                ContentValues myvalue = new ContentValues();
                myvalue.put("CategoryID", idcat);

                if (name.isEmpty()) {
                    edtProductNameInEditProduct.setError("Vui lòng nhập tên sản phẩm");
                    edtProductNameInEditProduct.requestFocus();
                    return;
                }
                if (price.isEmpty()) {
                    edtProductPriceInEditProduct.setError("Vui lòng nhập giá sản phẩm");
                    edtProductPriceInEditProduct.requestFocus();
                    return;
                } else {
                    priceNumber = Double.parseDouble(price);
                }

                if (quant.isEmpty()) {
                    edtProductQuantityInEditProduct.setError("Vui lòng nhập số lượng sản phẩm");
                    edtProductQuantityInEditProduct.requestFocus();
                    return;
                } else {
                    quantity = Integer.parseInt(quant);
                }

                // Kiem tra upload image
                if (checkUploadImg) {
                    btnEditProduct.setEnabled(false);
                    product.uploadImage(selectedImageUri).thenAccept(imageUrl -> {
                        ProductDb productDb = new ProductDb(pId, idcat, name, priceNumber,  des, quantity, imageUrl);
                        product.updateProduct(pId, productDb).thenAccept(r -> {
                            product.deleteImage(product.getImage()).thenAccept(r1 -> {
                                btnEditProduct.setEnabled(true);
                                Toast.makeText(EditProductManagementActivity.this, "Sửa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                Intent intenttoProductManagement = new Intent(EditProductManagementActivity.this, ProductManagementActivity.class);
                                startActivity(intenttoProductManagement);
                            }).exceptionally(e -> {
                                Toast.makeText(getApplicationContext(), "Xóa ảnh sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                                return null;
                            });
                        }).exceptionally(e -> {
                            // Xử lý khi xảy ra lỗi
                            Toast.makeText(EditProductManagementActivity.this, "Sửa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                            return null;
                        });
                    }).exceptionally(e -> {
                        // Xử lý khi xảy ra lỗi
                        Toast.makeText(EditProductManagementActivity.this, "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show();
                        return null;
                    });
                }
                else {
                    btnEditProduct.setEnabled(false);
                    ProductDb productDb = new ProductDb(pId, idcat, name, priceNumber,  des, quantity, product.getImage());
                    product.updateProduct(pId, productDb).thenAccept(r -> {
                        btnEditProduct.setEnabled(true);
                        Toast.makeText(EditProductManagementActivity.this, "Sửa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        Intent intenttoProductManagement = new Intent(EditProductManagementActivity.this, ProductManagementActivity.class);
                        startActivity(intenttoProductManagement);
                    }).exceptionally(e -> {
                        // Xử lý khi xảy ra lỗi
                        Toast.makeText(EditProductManagementActivity.this, "Sửa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                        return null;
                    });
                }
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
            checkUploadImg = true;
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imgVProductImageInEditProduct);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditProductManagementActivity.this, ProductManagementActivity.class);
        startActivity(intent);
    }
}
