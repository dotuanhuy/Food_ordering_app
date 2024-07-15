package com.example.project_btl_android;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ArrayAdapterReview extends ArrayAdapter<Review> {
    Activity context;
    int idLayout;
    ArrayList<Review> myList;
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    public ArrayAdapterReview(Activity context, int idLayout, ArrayList<Review> myList) {
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

        Review review = myList.get(position);
        TextView txtReviewUserName = convertView.findViewById(R.id.txtReviewUserName);
        TextView txtReviewText = convertView.findViewById(R.id.txtReviewText);
        ImageView imgReview = convertView.findViewById(R.id.imgReview);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingBar);

        AccountService accountService = new AccountService();
        View finalConvertView = convertView;
        accountService.getUserNameById(review.getUserId()).thenAccept(userName -> {
            Log.d("test", "check userName: " + userName);
            txtReviewUserName.setText(userName);
            txtReviewText.setText(review.getContent());
            ratingBar.setRating(review.getRating());
            ratingBar.setIsIndicator(true);
            Glide.with(finalConvertView)
                    .load(review.getImage())
                    .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                    .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                    .into(imgReview);

        }).exceptionally(ex -> {
            // Xử lý khi có lỗi
            Toast.makeText(context, "Failed to retrieve account: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });

        return convertView;
    }
}
