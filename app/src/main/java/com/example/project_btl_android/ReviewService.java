package com.example.project_btl_android;

import android.net.Uri;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReviewService {
    DatabaseReference db;
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    public ReviewService() {
        this.db = FirebaseDatabase.getInstance().getReference("Review");
    }

    public CompletableFuture<String> uploadImage(Uri imageUri) {
        CompletableFuture<String> uploadTaskFuture = new CompletableFuture<>();

        if (imageUri != null) {
            StorageReference fileRef = mStorageRef.child("reviews/" + UUID.randomUUID().toString());
            UploadTask uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    uploadTaskFuture.complete(task.getResult().toString());
                } else {
                    uploadTaskFuture.completeExceptionally(task.getException());
                }
            });
        } else {
            uploadTaskFuture.completeExceptionally(new IllegalArgumentException("Image URI is null"));
        }

        return uploadTaskFuture;
    }

    public CompletableFuture<Void> addReview(Review review) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String reviewId = db.push().getKey();
        review.setId(reviewId);
        db.child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}
