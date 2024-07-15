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

public class ArrayAdapterInBill extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;

    public ArrayAdapterInBill(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy 1 phần tử
        Product myProduct = myList.get(position);
        //Ánh xạ id
        ImageView img = convertView.findViewById(R.id.imgProductInBill);
        Glide.with(convertView)
                .load(myProduct.getImage())
                .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                .into(img);
        int n = myProduct.getQuantityInCart();
        TextView txtProductWithQuantityInBill = convertView.findViewById(R.id.txtProductWithQuantityInBill);
        txtProductWithQuantityInBill.setText(n + "x " + myProduct.getName());
        TextView txtProductToMoneyInBill = convertView.findViewById(R.id.txtProductToMoneyInBill);
        txtProductToMoneyInBill.setText(myProduct.formatToVND(myProduct.toMoney(n)));
        return convertView;
    }
}
