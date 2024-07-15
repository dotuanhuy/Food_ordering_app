package com.example.project_btl_android;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ArrayAdapterInCart extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;
    Cart cart;
    double total;
    CartActivity mActivity;
    FirebaseDatabase db = new FirebaseDataBaseHelper().getFirebaseDatabase();
    ArrayList<Boolean> checkboxStates;
    private boolean isPositiveButtonClicked = false;
    private double tax = 0.0;

    private int quantityP;

    //Khi tạo 1 adapter -> tạo 1 mảng chứa trạng thái của các checkbox (sản phẩm)
    public ArrayAdapterInCart(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (CartActivity) context;
        checkboxStates = new ArrayList<>();
        for (int i = 0; i < myList.size(); i++) {
            checkboxStates.add(false); // Khởi tạo trạng thái mặc định là false cho mỗi checkbox
        }
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public FirebaseDatabase getDatabase() {
        return db;
    }

    public void setDatabase(FirebaseDatabase database) {
        this.db = database;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy phần tử tại ví trí position
        Product myProduct = myList.get(position);
        //Ánh xạ id
        TextView txtProductToMoneyInCart = convertView.findViewById(R.id.txtProductToMoneyInCart);
        CheckBox ckProductInCart = convertView.findViewById(R.id.ckProductInCart);
        //Đưa thoong tin sản phẩm lên convertView
        ImageView img = convertView.findViewById(R.id.imgProductInCart);
        Glide.with(convertView)
                .load(myProduct.getImage())
                .placeholder(R.drawable.placeholder) // ảnh hiển thị trong khi tải ảnh
                .error(R.drawable.placeholder) // ảnh hiển thị khi có lỗi
                .into(img);
        TextView txtProductNameInCart = convertView.findViewById(R.id.txtProductNameInCart);
        txtProductNameInCart.setText(myProduct.getName());
        TextView txtProductQuantityInCart = convertView.findViewById(R.id.txtPoductQuantityInCart);
        txtProductQuantityInCart.setText(myProduct.getQuantityInCart()+"");
        int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
        TextView txtProductPriceInCart = convertView.findViewById(R.id.txtProductToMoneyInCart);
        txtProductPriceInCart.setText(myProduct.formatToVND(myProduct.toMoney(n)));
        ImageView imgVAddProductToCart = convertView.findViewById(R.id.imgVAddProductToCart);
        ImageView imgVDeleteProductFromCart = convertView.findViewById(R.id.imgVDeleteProductFromCart);

        View finalConvertView = convertView;
        getQuantity(db, myProduct.getId()).thenAccept(quantity -> {
            // Sử dụng quantity nhận được
            if (Integer.parseInt(txtProductQuantityInCart.getText().toString()) > quantity) {
                txtProductNameInCart.setEnabled(false);
                txtProductQuantityInCart.setEnabled(false);
                txtProductPriceInCart.setEnabled(false);
                ckProductInCart.setEnabled(false);
                imgVAddProductToCart.setEnabled(false);
                imgVDeleteProductFromCart.setEnabled(false);
                TextView txtTB = new TextView(context);
                txtTB.setPadding(30, 0, 0, 0);
                txtTB.setTextColor(Color.parseColor("#DB5860"));
                txtTB.setText("Không đủ số lượng " + myProduct.getName());
                ((ViewGroup) finalConvertView).addView(txtTB);
            }
        }).exceptionally(ex -> {
            Log.e("test", "Failed to get quantity", ex);
            return null;
        });

        //Khi chọn 1 sản phẩm trong giỏ hàng
        //Gán giá trị checked cho checkbox theo giá trị trong mảng check ở vị trí position
        ckProductInCart.setChecked(checkboxStates.get(position));
        ckProductInCart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tax = 0.0;
                checkEnable(myProduct, position);
                //Khi click vào checkbox, đảo ngược giá trị checked của checkbox product
                myProduct.setIscheck(!myProduct.isIscheck());
                // Thay đổi giá trị checkbox trong mảng theo isCheck của sự kiện
                checkboxStates.set(position, isChecked);
                //Nếu được chọn -> thêm sản phẩm vào selectList và ngược lại
                if(checkboxStates.get(position) == true){
                    mActivity.addSelectProduct(myProduct);
                }
                else {
                    mActivity.removeSelectProduct(myProduct);
                }
                //Hiển thị tổng tiền của các sản phẩm được chọn trong giỏ hàng
                toStringTotal(cart);
            }
        });

        ImageView imgVTrashFromCart = convertView.findViewById(R.id.imgVTrashFromCart);
        imgVTrashFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirm(myProduct, position);
//                //Cập nhật giao diện
                notifyDataSetChanged();
            }
        });

        //Tăng sản phẩm từ giỏ hàng
        imgVAddProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgVAddProductToCart.setEnabled(false);
                //Thêm sản phẩm
                int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                txtProductQuantityInCart.setText(String.valueOf(n+1));
                myProduct.updateProductQuantityToCart(cart, txtProductQuantityInCart, mActivity)
                        .thenAccept(result -> {
                            toStringTotal(cart);
                            txtProductToMoneyInCart.setText(myProduct.toMoney(myProduct.getQuantityInCart()) + "");
                            imgVAddProductToCart.setEnabled(true);
                        })
                        .exceptionally(ex -> {
                            imgVAddProductToCart.setEnabled(true);
                            // Xử lý trường hợp ngoại lệ (nếu có)
                            Log.e("Error", "Failed to update product quantity", ex);
                            return null;
                        });
            }
        });

        //Giảm
        imgVDeleteProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Nếu số lượng chỉ còn 1 và tiếp tục giảm -> xóa
                if (myProduct.getQuantityInCart() == 1) {
                    DeleteConfirm(myProduct, position);
                } else {
                    int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                    txtProductQuantityInCart.setText(String.valueOf(n-1));
                    myProduct.updateProductQuantityToCart(cart, txtProductQuantityInCart, mActivity)
                            .thenAccept(result -> {
                                toStringTotal(cart);
                                txtProductToMoneyInCart.setText(myProduct.toMoney(myProduct.getQuantityInCart()) + "");
                            })
                            .exceptionally(ex -> {
                                // Xử lý trường hợp ngoại lệ (nếu có)
                                Log.e("Error", "Failed to update product quantity", ex);
                                return null;
                            });
                }
            }
        });

        txtProductQuantityInCart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                myProduct.updateProductQuantityToCart(cart, txtProductQuantityInCart, database, mActivity);
            }
        });

        //Khi click vào tên của sản phẩm trong giỏ hàng, chuyển đến trang chi tiết của sản phẩm đó
        txtProductNameInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, DetailProductActivity.class);
                intent.putExtra("product", myProduct.getId());
                intent.putExtra("Cart", mActivity.cart);
                mActivity.startActivity(intent);
            }
        });
        return convertView;
    }

    public CompletableFuture<Integer> getQuantity(FirebaseDatabase db, String productId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        db.getReference("Product").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int quantity = 0;
                if (snapshot.exists()) {
                    Integer quantityValue = snapshot.child("quantity").getValue(Integer.class);
                    if (quantityValue != null) {
                        quantity = quantityValue;
                    }
                }
                future.complete(quantity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new Exception("Load data failed"));
            }
        });
        return future;
    }

    public interface QuantityCallback {
        void onQuantityReceived(int quantity);
    }

    //cập nhật tổng tiền các sản phẩm được chọn trong giỏ hàng
    public void toStringTotal(Cart cart){
        cart.updateTotalInCart(mActivity.mySelectProductList);
        mActivity.txtTotalMoneyProductSelect.setText(cart.formatToVND(cart.getTotal()));
    }

    //Xác nhận xóa
    public void DeleteConfirm(Product myProduct, int position){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle("XÁC NHẬN XÓA");
        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //Xóa sản phẩm -> xóa trạng thái checkbox của sản phẩm
                checkboxStates.remove(position);
                myProduct.setIscheck(false);
                if(mActivity.mySelectProductList.contains(myProduct)){
                    mActivity.removeSelectProduct(myProduct);
                }
                mActivity.myListProductInCart.remove(myProduct);
                myProduct.deleteProductFromCart(cart, myProduct.getId())
                        .thenAccept(result -> {
                            toStringTotal(cart);
                            notifyDataSetChanged();
                            Log.d("Firebase", "Detail_ProductCart item deleted successfully");
                        })
                        .exceptionally(e -> {
                            // Xử lý nếu có lỗi xảy ra khi xóa
                            Log.e("Firebase", "Failed to delete Detail_ProductCart item", e);
                            return null;
                        });
            }
        });
        dialog.create().show();
    }

    public void checkEnable(Product myProduct, int position){
        CompletableFuture<Integer> quantityInCartFuture = myProduct.quantityInCart(cart, myProduct.getId());
        CompletableFuture<Integer> quantityVisibleFuture = myProduct.checkQuantityVisible();
        CompletableFuture.allOf(quantityInCartFuture, quantityVisibleFuture).thenRun(() -> {
            try {
                int quantityInCart = quantityInCartFuture.get();
                int quantityVisible = quantityVisibleFuture.get();

                if (quantityInCart > quantityVisible) {
                    checkboxStates.set(position, false);
                    mActivity.mySelectProductList.remove(myProduct);
                }

                // Update UI or perform any other necessary operations here
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
