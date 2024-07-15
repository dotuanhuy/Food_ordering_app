package com.example.project_btl_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class AddReview extends AppCompatActivity {
    TextView txtBackFromAddReview;
    RatingBar ratingBarAdd;
    ImageButton imgBtnUpload;
    ImageView imgReviewAdd;
    EditText edtContent;
    Button btnAddReview;
    Uri selectedImageUri;
    Account user;
    String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        txtBackFromAddReview = findViewById(R.id.txtBackFromAddReview);
        ratingBarAdd = findViewById(R.id.ratingBarAdd);
        imgBtnUpload = findViewById(R.id.imgBtnUpload);
        imgReviewAdd = findViewById(R.id.imgReviewAdd);
        edtContent = findViewById(R.id.edtContent);
        btnAddReview = findViewById(R.id.btnAddReview);

        user = (Account) getIntent().getSerializableExtra("user");
        productId = getIntent().getStringExtra("productId");

        imgBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddReview.setEnabled(false);
                ReviewService reviewService = new ReviewService();
                reviewService.uploadImage(selectedImageUri)
                        .thenAccept(imageUrl -> {
                            float rating = ratingBarAdd.getRating();
                            String content = edtContent.getText().toString();
                            Review review = new Review(user.getId(), productId, content, imageUrl, rating);
                            reviewService.addReview(review).thenAccept(r -> {
                                Toast.makeText(getApplicationContext(), "Thêm đánh giá thành công", Toast.LENGTH_SHORT).show();
                                btnAddReview.setEnabled(true);
                                finish();
                            }).exceptionally(e -> {
                                // Xử lý khi xảy ra lỗi
                                Toast.makeText(getApplicationContext(), "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show();
                                return null;
                            });
                        })
                        .exceptionally(e -> {
                            // Xử lý khi xảy ra lỗi
                            Toast.makeText(getApplicationContext(), "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show();
                            return null;
                        });
            }
        });

        txtBackFromAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    .into(imgReviewAdd);
        }
    }
}