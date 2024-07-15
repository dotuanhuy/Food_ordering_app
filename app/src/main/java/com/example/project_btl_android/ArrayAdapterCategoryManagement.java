package com.example.project_btl_android;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ArrayAdapterCategoryManagement extends ArrayAdapter<Category> {
    Activity context;
    int idLayout;
    ArrayList<Category> myList;
    CategoryManagementActivity mActivity;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    CategoryService categoryService = new CategoryService();

    public ArrayAdapterCategoryManagement(Activity context, int idLayout, ArrayList<Category> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (CategoryManagementActivity) context;
    }

    //gọi hàm getView

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy 1 phần tử
        Category category = myList.get(position);
        //Ánh xạ id
        EditText txtCategoryName = convertView.findViewById(R.id.txtCategoryName);
        txtCategoryName.setText(category.getName());
        ImageView imgVDeleteCategory = convertView.findViewById(R.id.imgVDeleteCategory);
        ImageView imgVEditCategory = convertView.findViewById(R.id.imgVEditCategory);
        ImageView imgVSaveCatagory = convertView.findViewById(R.id.imgVSaveCatagory);
        txtCategoryName.setEnabled(false);
        imgVSaveCatagory.setVisibility(View.VISIBLE);

        imgVEditCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtCategoryName.setEnabled(true);
            }
        });

        imgVSaveCatagory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtCategoryName.getText().toString().isEmpty()) {
                    if (!txtCategoryName.getText().toString().equals(category.getName())) {
                        CategoryService categoryService = new CategoryService();
                        Category categoryNew = new Category(category.getId(), txtCategoryName.getText().toString());
                        categoryService.updateCategory(category.getId(), categoryNew).thenAccept(r -> {
                            Toast.makeText(context, "Sửa thể loại thành công", Toast.LENGTH_SHORT).show();
                        }).exceptionally(e -> {
                            Toast.makeText(context, "Sửa thể loại thất bại", Toast.LENGTH_SHORT).show();
                            return null;
                        });;
                    }

                }
                else{
                    Toast.makeText(context, "Tên thể loại không được để trống", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Sửa trực tiếp trên Edittext
        txtCategoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Khi xóa 1 item -> xác nhận
        imgVDeleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirm(category);
            }
        });
        return convertView;
    }


    //Xác nhận trước khi xóa
    public void DeleteConfirm(Category category){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle("XÁC NHẬN XÓA");
        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // Xử lý kết quả, ví dụ: hiển thị số lượng sản phẩm
                countProductsByCategoryId(category.getId()).thenAccept(count -> {
                    if (count == 0) {
                        categoryService.deleteCategoryById(category.getId()).thenAccept(r -> {

                        }).exceptionally(ex -> {
                            // Xử lý lỗi
                            Toast.makeText(context.getApplicationContext(), "Failed to count products: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            return null;
                        });
                    }
                    else {
                        Toast.makeText(mActivity, "Có sản phẩm thuộc thể loại này, không thể xóa!", Toast.LENGTH_SHORT).show();
                    }
                }).exceptionally(ex -> {
                    // Xử lý lỗi
                    Toast.makeText(context.getApplicationContext(), "Failed to count products: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
            }
        });
        dialog.create().show();
    }

    public CompletableFuture<Integer> countProductsByCategoryId(String categoryId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        db.getReference("Product").orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = (int) snapshot.getChildrenCount();
                        future.complete(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });
        return future;
    }

}
