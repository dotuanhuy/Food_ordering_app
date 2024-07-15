package com.example.project_btl_android;

public class DetailBillProduct {
    private String billId;
    private String productId;
    private int quantity;

    public DetailBillProduct() {
    }

    public DetailBillProduct(String billId, String productId, int quantity) {
        this.billId = billId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
