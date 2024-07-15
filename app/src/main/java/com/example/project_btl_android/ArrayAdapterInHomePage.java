package com.example.project_btl_android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ArrayAdapterInHomePage extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;

    public ArrayAdapterInHomePage(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
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
        Product myProduct = myList.get(position);
        //Ánh xạ id
        ImageView img = convertView.findViewById(R.id.imgProductInHomePage);
        Glide.with(context)
                .load(myProduct.getImage())
                .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                .into(img);
        TextView txtProductNameInHomePage = convertView.findViewById(R.id.txtProductNameInHomePage);
        txtProductNameInHomePage.setText(myProduct.getName());
        TextView txtProductPriceInHomePage = convertView.findViewById(R.id.txtProductPriceInHomePage);
        txtProductPriceInHomePage.setText(myProduct.formatToVND(myProduct.getPrice()));
        return convertView;
    }
}
