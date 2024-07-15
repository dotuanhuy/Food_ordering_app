package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ReviewActivity extends AppCompatActivity {
    ListView lvReviews;
    TextView txtBackFromReview, txtAddReview;
    Account user;
    String productId;
    ArrayList<Review> listReviews;
    ArrayAdapterReview arrayAdapterReview;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    boolean isOrder = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        lvReviews = findViewById(R.id.lvReviews);
        txtBackFromReview = findViewById(R.id.txtBackFromReview);
        txtAddReview = findViewById(R.id.txtAddReview);
        listReviews = new ArrayList<>();

        user = (Account) getIntent().getSerializableExtra("user");
        productId = getIntent().getStringExtra("productId");

        displayReviews();

        txtAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillService billService = new BillService();
                DetailBillProductService detailBillProductService = new DetailBillProductService();  // Đảm bảo tạo instance của DetailBillProductService một lần

                billService.getBillsByUserId(user.getId()).thenAccept(bills -> {
                    if (bills != null && !bills.isEmpty()) {
                        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
                        for (Bill bill : bills) {
                            CompletableFuture<Void> future = detailBillProductService.getDetailBillsByBillId(bill.getId())
                                    .thenAccept(detailBills -> {
                                        if (detailBills != null && !detailBills.isEmpty()) {
                                            for (DetailBillProduct detail : detailBills) {
                                                if (detail.getProductId().equals(productId)) {
                                                    isOrder = true;
                                                    break;
                                                }
                                            }
//                                            if (detailBills.contains(productId)) {
//                                                isOrder = true;
//                                            }
                                        }
                                    }).exceptionally(ex -> {
                                        // Xử lý khi có lỗi
                                        Toast.makeText(getApplicationContext(), "Failed to retrieve detail bills: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        return null;
                                    });
                            futures.add(future);
                        }

                        // Chờ tất cả các yêu cầu hoàn thành
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                            if (!isOrder) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Bạn cần đặt món ăn trước khi đánh giá", Toast.LENGTH_SHORT).show());
                            } else {
                                Intent intent = new Intent(ReviewActivity.this, AddReview.class);
                                intent.putExtra("user", user);
                                intent.putExtra("productId", productId);
                                startActivity(intent);
                            }
                        }).exceptionally(ex -> {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to complete all tasks: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                            return null;
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Bạn cần đặt món ăn trước khi đánh giá 1", Toast.LENGTH_SHORT).show());
                    }
                }).exceptionally(ex -> {
                    // Xử lý khi có lỗi
                    Toast.makeText(getApplicationContext(), "Failed to retrieve bills: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
            }
        });

        txtBackFromReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayReviews() {
        db.getReference("Review").orderByChild("productId").equalTo(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listReviews.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Review review = snapshot.getValue(Review.class);
                    listReviews.add(review);
                }
                if (listReviews.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Không có đánh giá nào",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    arrayAdapterReview = new ArrayAdapterReview(ReviewActivity.this, R.layout.layout_item_review, listReviews);
                    lvReviews.setAdapter(arrayAdapterReview);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Load data failed "+error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}