package com.example.project_btl_android;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class AccountService {
    DatabaseReference db;
    public AccountService() {
        this.db = FirebaseDatabase.getInstance().getReference("Account");
    }

    public static String encryptPassword(String password) {
        String encryptedPassword = PasswordUtils.encryptPassword(password);
        return encryptedPassword;
    }

    public CompletableFuture<String> getUserNameById(String id) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("userName").getValue(String.class);
                    future.complete(userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    CompletableFuture<Boolean> checkUserNameExists(String userName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(true); // Username tồn tại
                } else {
                    future.complete(false); // Username không tồn tại
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    CompletableFuture<Boolean> checkEmailExists(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(true); // Username tồn tại
                } else {
                    future.complete(false); // Username không tồn tại
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    CompletableFuture<Boolean> checkPhoneNumberExists(String phoneNumber) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(true); // Username tồn tại
                } else {
                    future.complete(false); // Username không tồn tại
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }
}
