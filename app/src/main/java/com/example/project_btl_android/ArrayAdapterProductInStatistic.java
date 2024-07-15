package com.example.project_btl_android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArrayAdapterProductInStatistic extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;
    StatisticManagementActivity mActivity;
    double revenue = 0.0;
    int quantity = 0;
    double price = 0.0;

    public ArrayAdapterProductInStatistic(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (StatisticManagementActivity) context;
    }
    //gọi hàm getView

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        if (convertView != null) return convertView;
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy 1 phần tử
        Product product = myList.get(position);
        //Ánh xạ id
        TextView txtProductNameInStatistic = convertView.findViewById(R.id.txtProductNameInStatistic);
        TextView txtProductRevenueInStatistic = convertView.findViewById(R.id.txtProductRevenueInStatistic);
        txtProductNameInStatistic.setText(product.getName());
        revenue = 0.0;
        String startDate = mActivity.edtTimeFrom.getText().toString();
        String endDate = mActivity.edtTimeTo.getText().toString();
        BillService billService = new BillService();

        billService.getBillsBetweenDates(startDate, endDate)
                .thenCompose(listBills -> {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (Bill bill : listBills) {
                        DetailBillProductService detailBillProductService = new DetailBillProductService();
                        CompletableFuture<Void> future = detailBillProductService.getQuantityByBillIdAndProductId(bill.getId(), product.getId())
                                .thenAccept(resQuantity -> {
                                    if (resQuantity != null) {
                                        synchronized (this) {
                                            quantity += resQuantity;
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to get quantity for billId " + bill.getId(), Toast.LENGTH_SHORT).show();
                                    }
                                }).exceptionally(ex -> {
                                    Toast.makeText(context, "Failed: " + ex.toString(), Toast.LENGTH_SHORT).show();
                                    return null;
                                });
                        futures.add(future);
                    }

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                })
                .thenRun(() -> {
                    revenue = product.getPrice() * quantity;
                    txtProductRevenueInStatistic.setText(revenue + " VND");
                    quantity = 0;
                })
                .exceptionally(ex -> {
                    Toast.makeText(context, "Failed: " + ex.toString(), Toast.LENGTH_SHORT).show();
                    return null;
                });
        return convertView;
    }

}
