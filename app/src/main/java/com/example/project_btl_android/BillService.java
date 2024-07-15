package com.example.project_btl_android;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class BillService {
    private DatabaseReference db;

    public BillService() {
        this.db = FirebaseDatabase.getInstance().getReference("Bill");
    }

    public CompletableFuture<ArrayList<Bill>> getBillsBetweenDates(String startDate, String endDate) {
        CompletableFuture<ArrayList<Bill>> future = new CompletableFuture<>();

        db.orderByChild("createDay").startAt(startDate).endAt(endDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Bill> billList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bill bill = snapshot.getValue(Bill.class);
                    if (bill != null) {
                        billList.add(bill);
                    }
                }
                future.complete(billList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Double> getTotalFromBillsBetweenDates(String startDate, String endDate) {
        CompletableFuture<Double> future = new CompletableFuture<>();
        db.orderByChild("createDay").startAt(startDate).endAt(endDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalSum = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bill bill = snapshot.getValue(Bill.class);
                    if (bill != null) {
                        totalSum += bill.getToTal();
                    }
                }
                future.complete(totalSum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<ArrayList<Bill>> getBillsByUserId(String userId) {
        CompletableFuture<ArrayList<Bill>> future = new CompletableFuture<>();

        db.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Bill> bills = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bill bill = snapshot.getValue(Bill.class);
                    if (bill != null) {
                        if (bill.getStatus().equals("finish")) {
                            bills.add(bill);
                        }
                    }
                }
                future.complete(bills);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

//    public CompletableFuture<ArrayList<Bill>> getBillsByStatus(String status) {
//        CompletableFuture<ArrayList<Bill>> future = new CompletableFuture<>();
//        db.orderByChild("status").equalTo(status).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                ArrayList<Bill> bills = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Bill bill = snapshot.getValue(Bill.class);
//                    bills.add(bill);
//                }
//                future.complete(bills);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                future.completeExceptionally(databaseError.toException());
//            }
//        });
//
//        return future;
//    }

    public CompletableFuture<Void> updateBillStatus(String billId, String newStatus) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.child(billId).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }
}
