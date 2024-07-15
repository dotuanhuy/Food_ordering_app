package com.example.project_btl_android;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CompletableFuture;

public class CartService {
    private DatabaseReference db;

    public CartService() {
        this.db = FirebaseDatabase.getInstance().getReference("Cart");
    }

    public CompletableFuture<CartDb> addCart(String userId) {
        CompletableFuture<CartDb> future = new CompletableFuture<>();
        String id = db.push().getKey();
        if (id == null) {
            future.completeExceptionally(new RuntimeException("Failed to generate a new key for DetailProductCart"));
            return future;
        }
        CartDb cartDb = new CartDb(id, userId);
        db.child(id).setValue(cartDb)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            future.complete(cartDb);
                        } else {
                            future.completeExceptionally(task.getException());
                        }
                    }
                });

        return future;
    }
}
