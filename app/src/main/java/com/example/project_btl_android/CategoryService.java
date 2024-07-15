package com.example.project_btl_android;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class CategoryService {
    private DatabaseReference db;
    public CategoryService() {
        this.db = FirebaseDatabase.getInstance().getReference("Category");
    }

    public CompletableFuture<ArrayList<Category>> getAllCategories() {
        CompletableFuture<ArrayList<Category>> future = new CompletableFuture<>();

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                future.complete(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<String> getCategoryNameById(String categoryId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null) {
                    future.complete(category.getName());
                } else {
                    future.completeExceptionally(new Exception("Category not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> deleteCategoryById(String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.child(id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> insertCategory(String name) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Tạo một key mới cho category
        String id = db.push().getKey();
        // Tạo category mới với id và name
        Category newCategory = new Category(id, name);
        if (id != null) {
            // Đưa category mới lên Firebase
            db.child(id).setValue(newCategory)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("test", "check");
                        future.complete(null);
                    })
                    .addOnFailureListener(future::completeExceptionally);
            return future;
        }else {
            future.completeExceptionally(new NullPointerException("Failed to generate category ID"));
        }
        return future;
    }

    public CompletableFuture<Void> updateCategory(String categoryId, Category updateCategory) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.child(categoryId).setValue(updateCategory).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("test", "check : " + categoryId);
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }
}
