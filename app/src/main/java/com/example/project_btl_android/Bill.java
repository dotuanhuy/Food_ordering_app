package com.example.project_btl_android;

import java.text.NumberFormat;
import java.util.Locale;

public class Bill {
    private String id;
    private String userId;
    private Double total = 0.0;
    private String createDay = null;
    private String status;

    private String address;

    public String getId() {
        return id;
    }

    public void setId(String idBill) {
        this.id = idBill;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Bill() {}

    public Bill(String userId, String createDay, Double total, String status) {
        this.userId = userId;
        this.createDay = createDay;
        this.total = total;
        this.status = status;
    }
    public Bill(String id, String userId, String createDay, Double total, String address, String status) {
        this.id = id;
        this.userId = userId;
        this.createDay = createDay;
        this.total = total;
        this.address = address;
        this.status = status;
    }

    public Double getToTal() {
        return total;
    }

    public void setToTal(Double toTal) {
        this.total = toTal;
    }

    public String getCreateDay() {
        return createDay;
    }

    public void setCreateDay(String createDay) {
        this.createDay = createDay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String formatToVND(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount); // Định dạng số tiền
    }
}
