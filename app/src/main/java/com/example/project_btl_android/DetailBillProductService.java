package com.example.project_btl_android;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DetailBillProductService {
    private DatabaseReference db;

    public DetailBillProductService() {
        this.db = FirebaseDatabase.getInstance().getReference("Detail_ProductBill");
    }

    public CompletableFuture<Integer> getQuantityByBillIdAndProductId(String billId, String productId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int quantity = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (billId.equals(snapshot.child("billId").getValue(String.class)) && productId.equals(snapshot.child("productId").getValue(String.class))) {
                        quantity += snapshot.child("quantity").getValue(Integer.class);
                    }
                }
                future.complete(quantity); // No matching record found
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }

    public CompletableFuture<Void> addDetailProductBill(DetailBillProduct detailBillProduct) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String id = db.push().getKey();
        db.child(id).setValue(detailBillProduct).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<DetailBillProduct>> getDetailBillsByBillId(String billId) {
        CompletableFuture<ArrayList<DetailBillProduct>> future = new CompletableFuture<>();

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<DetailBillProduct> detailBills = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DetailBillProduct detailProductBill = snapshot.getValue(DetailBillProduct.class);
                    if (detailProductBill != null && billId.equals(detailProductBill.getBillId())) {
                        detailBills.add(detailProductBill);
                    }
                }

                future.complete(detailBills);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }
}
