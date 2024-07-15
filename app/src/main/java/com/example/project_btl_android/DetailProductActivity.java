package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class DetailProductActivity extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    TextView txtProductPriceInDetail, txtProductNameInDetail, txtProductDescriptionInDetail,
            txtBackFromDetailToHomepage, txtToMoneyInDetail, txtOrderFromDetail, txtLinkReview;
    EditText txtProductQuantityInCart;
    Product productToDetail;
    String idProduct;
    Cart cart;
    Account user;
    ImageView imgProductInDetail, imgVAddProductToCartInDetail, imgVDeleteProductFromCartInDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);
        //Ánh xạ ID
        txtProductNameInDetail = findViewById(R.id.txtProductNameInDetail);
        txtProductPriceInDetail = findViewById(R.id.txtProductPriceInDetail);
        txtProductDescriptionInDetail = findViewById(R.id.txtDescriptionProductInDetail);
        txtProductQuantityInCart = findViewById(R.id.txtPoductQuantityInCart);
        txtBackFromDetailToHomepage = findViewById(R.id.txtBackFromDetailToHomepage);
        txtOrderFromDetail = findViewById(R.id.txtOrderFromDetail);
        imgProductInDetail = findViewById(R.id.imgProductInDetail);
        txtToMoneyInDetail = findViewById(R.id.txtToMoneyInDetail);
        txtLinkReview = findViewById(R.id.txtLinkReview);
        imgVAddProductToCartInDetail = findViewById(R.id.imgVAddProductToCartInDetail);
        imgVDeleteProductFromCartInDetail = findViewById(R.id.imgVDeleteProductFromCartInDetail);
        cart = (Cart) getIntent().getSerializableExtra("Cart");
        idProduct = getIntent().getStringExtra("product");
        user = (Account) getIntent().getSerializableExtra("user");
        txtLinkReview.setText(Html.fromHtml(getString(R.string.link)));

        showDetailProduct();

        //Thêm số lượng sản phẩm vào giỏ hàng từ detail
        imgVAddProductToCartInDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgVAddProductToCartInDetail.setEnabled(false);
                imgVDeleteProductFromCartInDetail.setEnabled(false);
                int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                txtProductQuantityInCart.setText(String.valueOf(n+1));
                productToDetail.updateProductQuantityToCart(cart, txtProductQuantityInCart, DetailProductActivity.this)
                        .thenAccept(result -> {
                            showDetailProduct().thenAccept(info -> {
                                txtProductQuantityInCart.setText(String.valueOf(n+1));
                                txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.formatToVND(productToDetail.toMoney(n+1)));
                                imgVAddProductToCartInDetail.setEnabled(true);
                                imgVDeleteProductFromCartInDetail.setEnabled(true);
                            }).exceptionally(ex -> {
                                // Xử lý trường hợp ngoại lệ (nếu có)
                                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                return null;
                            });
                        })
                        .exceptionally(ex -> {
                            // Xử lý trường hợp ngoại lệ (nếu có)
                            Log.e("Error", "Failed to update product quantity", ex);
                            return null;
                        });
            }
        });

        //Giarm số lượng sản phẩm trong giỏ hàng từ detail
        imgVDeleteProductFromCartInDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgVAddProductToCartInDetail.setEnabled(false);
                imgVDeleteProductFromCartInDetail.setEnabled(false);
                int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                txtProductQuantityInCart.setText(String.valueOf(n-1));
                if (n == 1) {
                    productToDetail.deleteProductFromCart(cart, productToDetail.getId())
                            .thenAccept(result -> {
                                showDetailProduct().thenAccept(info -> {
                                    txtToMoneyInDetail.setText("Thành tiền: 0.0đ");
                                    imgVAddProductToCartInDetail.setEnabled(true);
                                    imgVDeleteProductFromCartInDetail.setEnabled(true);
                                }).exceptionally(ex -> {
                                    // Xử lý trường hợp ngoại lệ (nếu có)
                                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    return null;
                                });
                            })
                            .exceptionally(ex -> {
                                // Xử lý trường hợp ngoại lệ (nếu có)
                                Log.e("Error", "", ex);
                                return null;
                            });

                } else {
                    txtProductQuantityInCart.setText(String.valueOf(n-1));
                    productToDetail.updateProductQuantityToCart(cart, txtProductQuantityInCart, DetailProductActivity.this)
                            .thenAccept(result -> {
                                txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.formatToVND(productToDetail.toMoney(n-1)));
                                imgVAddProductToCartInDetail.setEnabled(true);
                                imgVDeleteProductFromCartInDetail.setEnabled(true);
                            })
                            .exceptionally(ex -> {
                                // Xử lý trường hợp ngoại lệ (nếu có)
                                Log.e("Error", "Failed to update product quantity", ex);
                                return null;
                            });
                }
            }
        });

        //Thanh toán từ detail
        txtOrderFromDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productToDetail.quantityInCart(cart, productToDetail.getId()).thenAccept(quantityCart -> {
                    if(quantityCart > 0){
                        Intent intent = new Intent(DetailProductActivity.this, BillActivity.class);
//                        intent.putExtra("ProductFromDetail", productToDetail);
                        intent.putExtra("productId", productToDetail.getId());
                        intent.putExtra("Cart", cart);
                        intent.setAction("FromDetail");
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(DetailProductActivity.this, "Vui lòng nhập số lượng sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                }).exceptionally(ex -> {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
            }
        });

        txtLinkReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReview = new Intent(DetailProductActivity.this, ReviewActivity.class);
                intentReview.putExtra("user", user);
                intentReview.putExtra("productId", idProduct);
                startActivity(intentReview);
            }
        });
    }

    public CompletableFuture<Void> showDetailProduct() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.getReference("Product").child(idProduct).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productToDetail = snapshot.getValue(Product.class);
                if (productToDetail == null) {
                    future.completeExceptionally(new Exception("Sản phẩm không tồn tại"));
                    return;
                }

                // Chuyển từ detailActivity đến HomePageActivity
                txtBackFromDetailToHomepage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                // Đưa thông tin sản phẩm lên giao diện
                Glide.with(DetailProductActivity.this)
                        .load(productToDetail.getImage())
                        .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                        .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                        .into(imgProductInDetail);
                txtProductNameInDetail.setText(productToDetail.getName());
                txtProductPriceInDetail.setText(productToDetail.formatToVND(productToDetail.getPrice()));
                txtProductDescriptionInDetail.setText(productToDetail.getDescription());

                // Xử lý bất đồng bộ
                CompletableFuture<Integer> quantityInCartFuture = productToDetail.quantityInCart(cart, productToDetail.getId());
                CompletableFuture<Integer> quantityVisibleFuture = productToDetail.checkQuantityVisible();
                CompletableFuture.allOf(quantityInCartFuture, quantityVisibleFuture).thenRun(() -> {
                    try {
                        int quantityInCart = quantityInCartFuture.get();
                        int quantityVisible = quantityVisibleFuture.get();

                        runOnUiThread(() -> {
                            txtProductQuantityInCart.setText(String.valueOf(quantityInCart));
                            txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.formatToVND(productToDetail.toMoney(quantityInCart)));
                            productToDetail.setQuantityInCart(quantityInCart);
                            checkQuantity(quantityInCart);
                            if (quantityInCart > quantityVisible) {
                                Toast.makeText(DetailProductActivity.this, "Không đủ số lượng sản phẩm", Toast.LENGTH_SHORT).show();
                                txtOrderFromDetail.setEnabled(false);
                                imgVAddProductToCartInDetail.setEnabled(false);
                                imgVDeleteProductFromCartInDetail.setEnabled(false);
                                txtProductQuantityInCart.setEnabled(false);
                            }
                            future.complete(null);
                        });
                    } catch (Exception e) {
                        Log.e("Error", "Failed to get quantities", e);
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        future.completeExceptionally(e);
                    }
                }).exceptionally(ex -> {
                    Log.e("Error", "Failed to complete future", ex);
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    future.completeExceptionally(ex);
                    return null;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new Exception("Sản phẩm không tồn tại"));
            }
        });

        return future;
    }


    //Kiểm tra số lượng trong chi tiết
    //Nếu = 0, ẩn trừ và số lượng
    public void checkQuantity(int q){
        if(q == 0){
            imgVDeleteProductFromCartInDetail.setVisibility(View.INVISIBLE);
            txtProductQuantityInCart.setVisibility(View.INVISIBLE);
        }
        else {
            imgVDeleteProductFromCartInDetail.setVisibility(View.VISIBLE);
            txtProductQuantityInCart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        productToDetail.quantityInCart(cart, productToDetail.getId()).thenAccept(quantity -> {
//            checkQuantity(quantity);
//            txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.toMoney(quantity));
//        }).exceptionally(ex -> {
//            Log.e("Error", "Failed ", ex);
//            return null;
//        });
    }
}