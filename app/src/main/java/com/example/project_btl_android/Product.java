package com.example.project_btl_android;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Product implements Serializable {
    private String id;
    private String categoryId;
    private String name;
    private double price;
    private String description;
    private int quantity;
    private String image;
    private boolean ischeck = false;
    private int quantityInCart = 0;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

    public Product(){}

    public String getId() {
        return id;
    }

    public String getcategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImage() {
        return image;
    }

    public boolean isIscheck() {
        return ischeck;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setcategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImage(String Image) {
        this.image = Image;
    }

    public void setIscheck(boolean ischeck) {
        this.ischeck = ischeck;
    }

    public Product(String Id, String categoryId, String name, double price, String description, int quantity, String image) {
        this.id = Id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.image = image;
    }

    public CompletableFuture<Void> updateProductQuantityToCart(Cart cart, TextView txtSoLuong, Activity context){
        int n = Integer.parseInt(txtSoLuong.getText().toString());
        return checkQuantityVisible().thenAccept(quantity -> {
            if(n>quantity){
                Toast.makeText(context, "Không đủ số lượng sản phẩm", Toast.LENGTH_SHORT).show();
                txtSoLuong.setText(this.quantityInCart + "");
                return;
            }
            if(n != 0) {
                this.setQuantityInCart(n);
                //Kieerm tra danh sách sản phẩm trong giỏ hàng
                quantityInCart(cart, this.getId()).thenAccept(quantitiCart -> {
                    //Nếu chưa có -> thêm mới
                    if (quantitiCart == 0) {
                        addDetailProductCartAsync(cart.getId(), this.getId(), n).thenAccept(avoid -> {
                                Toast.makeText(context.getApplication(), "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                        }).exceptionally(e -> {
                            Log.e("Firebase", "Failed to add DetailProductCart", e);
                            return null;
                        });
                    }
                    //Đã có -> update số lượng
                    else {
                        updateDetailProductCartQuantity(cart.getId(), this.getId(), n).thenAccept(aVoid -> {
                            Log.d("Firebase", "Quantity updated successfully");
                        }).exceptionally(e -> {
                            Log.e("Firebase", "Failed to update quantity", e);
                            return null;
                        });
                    }
                }).exceptionally(ex -> {
                    Log.e("Error", "Failed to get quantity", ex);
                    return null;
                });
            }
        }).exceptionally(ex -> {
            Log.e("Error", "Failed to get quantity", ex);
            return null;
        });
    }

    public CompletableFuture<Void> deleteProductFromCart(Cart cart, String productId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.getReference("Detail_ProductCart").orderByChild("cartId").equalTo(cart.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("productId").getValue(String.class).equals(productId)) {
                        // Thực hiện xóa và hoàn thành CompletableFuture với kết quả phù hợp
                        snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    future.complete(null); // Xóa thành công
                                } else {
                                    future.completeExceptionally(task.getException()); // Xảy ra lỗi khi xóa
                                }
                            }
                        });
                        return; // Thoát khỏi vòng lặp sau khi xóa
                    }
                }
                future.completeExceptionally(new Exception("Product not found in cart")); // Không tìm thấy sản phẩm trong giỏ hàng
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException()); // Xảy ra lỗi khi đọc dữ liệu
            }
        });

        return future;
    }

    public CompletableFuture<Integer> quantityInCart(Cart cart, String productId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.getReference("Detail_ProductCart")
                .orderByChild("cartId")
                .equalTo(cart.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int quantity = 0;
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            ProductDetailCart detail = childSnapshot.getValue(ProductDetailCart.class);
                            if (detail != null && detail.getProductId().equals(productId)) {
                                quantity += detail.getQuantity();
                            }
                        }
                        future.complete(quantity);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Integer> getQuantityProduct(String productId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.getReference("Product").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer quantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    if (quantity != null) {
                        future.complete(quantity);
                    } else {
                        future.completeExceptionally(new Exception("Quantity not found for product ID: " + productId));
                    }
                } else {
                    future.completeExceptionally(new Exception("Product not found with ID: " + productId));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException()); // Xảy ra lỗi khi đọc dữ liệu
            }
        });

        return future;
    }

    public double toMoney(int quantity){
        return this.price*quantity;
    }
    public String formatToVND(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount); // Định dạng số tiền
    }
    public void setQuantityInCart(int quantity){
        this.quantityInCart = quantity;
    }

    public int getQuantityInCart() {
        return quantityInCart;
    }

    public CompletableFuture<Integer> checkQuantityVisible() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        db.getReference("Product").child(this.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer quantity = snapshot.child("quantity").getValue(Integer.class);
                            future.complete(quantity != null ? quantity : 0);
                        } else {
                            future.complete(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });

        return future;
    }

    public CompletableFuture<Void> addDetailProductCartAsync(String cartId, String productId, int quantity) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Tạo một key mới cho sản phẩm chi tiết trong giỏ hàng
        String id = db.getReference("Detail_ProductCart").push().getKey();

        if (id == null) {
            future.completeExceptionally(new RuntimeException("Failed to generate a new key for DetailProductCart"));
            return future;
        }

        // Tạo một đối tượng DetailProductCart
        ProductDetailCart detail = new ProductDetailCart(cartId, productId, quantity);

        // Thêm đối tượng DetailProductCart vào Firebase
        db.getReference("Detail_ProductCart").child(id).setValue(detail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            future.complete(null); // Hoàn thành tương lai khi thành công
                        } else {
                            future.completeExceptionally(task.getException());
                        }
                    }
                });

        return future;
    }

    public CompletableFuture<Void> updateDetailProductCartQuantity(String cartId, String productId, int newQuantity) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Tìm tất cả các bản ghi trong Detail_ProductCart có cartId và productId tương ứng
        db.getReference("Detail_ProductCart")
                .orderByChild("cartId")
                .equalTo(cartId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean productFound = false;
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            ProductDetailCart detail = childSnapshot.getValue(ProductDetailCart.class);
                            if (detail != null && detail.getProductId().equals(productId)) {
                                // Cập nhật số lượng mới
                                childSnapshot.getRef().child("quantity").setValue(newQuantity)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                future.complete(null); // Hoàn thành tương lai khi cập nhật thành công
                                            } else {
                                                future.completeExceptionally(task.getException());
                                            }
                                        });
                                productFound = true;
                                break;
                            }
                        }
                        if (!productFound) {
                            future.completeExceptionally(new RuntimeException("Product not found in cart"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });
        return future;
    }

    public CompletableFuture<ArrayList<Product>> getAllProducts() {
        CompletableFuture<ArrayList<Product>> future = new CompletableFuture<>();

        db.getReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Product> productList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                future.complete(productList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Product> getProductById(String productId) {
        CompletableFuture<Product> future = new CompletableFuture<>();

        db.getReference("Product").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {
                    future.complete(product);
                } else {
                    future.completeExceptionally(new Exception("Product not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<String> uploadImage(Uri imageUri) {
        CompletableFuture<String> uploadTaskFuture = new CompletableFuture<>();

        if (imageUri != null) {
            StorageReference fileRef = mStorageRef.child("products/" + UUID.randomUUID().toString());
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

    public CompletableFuture<Void> deleteImage(String imageUrl) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(aVoid -> {
            future.complete(null);
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    public CompletableFuture<Void> addProduct(ProductDb product) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String productId = db.getReference("Product").push().getKey(); // Tạo một khóa duy nhất cho sản phẩm mới
        product.setId(productId);
        db.getReference("Product").child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> updateProduct(String productId, ProductDb product) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.getReference("Product").child(productId).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> deleteProduct(String productId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.getReference("Product").child(productId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> updateProductQuantity(String productId, int newQuantity) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.getReference("Product").child(productId).child("quantity").setValue(newQuantity).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                future.complete(null);
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }
}
