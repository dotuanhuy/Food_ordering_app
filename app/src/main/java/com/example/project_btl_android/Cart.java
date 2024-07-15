package com.example.project_btl_android;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Cart implements Serializable {
    private String id;
    private String userId;
    int ProductQuantity;
    private double total = 0.0;
    private double tax = 0.0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProductQuantity() {
        return ProductQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        ProductQuantity = productQuantity;
    }

    public Cart() {}

    public Cart(String id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public void updateTotalInCart(ArrayList<Product> listProductSelect){
        this.total = 0.0;
        for (Product product: listProductSelect) {
            this.total += product.toMoney(product.getQuantityInCart());
        }
    }

    public double getTotal() {
        return total;
    }

    public String formatToVND(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount); // Định dạng số tiền
    }
}
