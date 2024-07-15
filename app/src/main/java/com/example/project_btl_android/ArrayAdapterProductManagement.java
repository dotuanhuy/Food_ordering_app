package com.example.project_btl_android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class ArrayAdapterProductManagement extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;
    SQLiteDatabase database = null;
    ProductManagementActivity mActivity;
    Boolean check = true;

    public ArrayAdapterProductManagement(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (ProductManagementActivity) context;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
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
        Product product = myList.get(position);
        //Ánh xạ id
        TextView txtProductName = convertView.findViewById(R.id.txtProductName);
        txtProductName.setText(product.getName());
        ImageView imgVViewProduct = convertView.findViewById(R.id.imgVViewProduct);
        ImageView imgVEditProduct = convertView.findViewById(R.id.imgVEditProduct);
        ImageView imgVDeleteProduct = convertView.findViewById(R.id.imgVDeleteProduct);
        imgVViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ViewProductManagementActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            }
        });

        imgVEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, EditProductManagementActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            }
        });

        imgVDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirm(product);
            }
        });
        return convertView;
    }

    public void DeleteConfirm(Product product){
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
                product.deleteImage(product.getImage()).thenAccept(r -> {
                    product.deleteProduct(product.getId()).thenAccept(r2 -> {
                        Toast.makeText(mActivity, "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    }).exceptionally(e -> {
                        Toast.makeText(mActivity, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                        return null;
                    });
                }).exceptionally(e -> {
                    Toast.makeText(mActivity, "Xóa ảnh sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                    return null;
                });
            }
        });
        dialog.create().show();
    }
}
