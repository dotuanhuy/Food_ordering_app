package com.example.project_btl_android;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ArrayAdapterHistory extends ArrayAdapter<Bill> {
    Activity context;
    int idLayout;
    ArrayList<Bill> myList;
    public ArrayAdapterHistory(Activity context, int idLayout, ArrayList<Bill> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
    }
    //gọi hàm getView

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);

        //Ánh xạ id
        TextView txtBillID = convertView.findViewById(R.id.txtBillID);
        TextView txtBillDay = convertView.findViewById(R.id.txtBillDay);
        TextView txtTotal = convertView.findViewById(R.id.txtTotal);
        TextView txtConfirm = convertView.findViewById(R.id.txtConfirmHis);

        //Lấy 1 phần tử
        Bill bill = myList.get(position);
        String status = "";
        if (bill.getStatus().equals("wait")) {
            txtConfirm.setVisibility(View.GONE);
        }
        else if (bill.getStatus().equals("shipping")) {
            status = "finish";
        }
        else {
            txtConfirm.setVisibility(View.GONE);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("XÁC NHẬN ĐƠN HÀNG");
        dialog.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        String finalStatus = status;
        dialog.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BillService billService = new BillService();
                billService.updateBillStatus(bill.getId(), finalStatus).thenAccept(aVoid -> {
                    Toast.makeText(context, "Xác nhận đơn hàng thành công", Toast.LENGTH_SHORT).show();
                }).exceptionally(throwable -> {
                    Toast.makeText(context, "Xác nhận đơn hàng thất bại" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
            }
        });

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.create().show();
            }
        });

        txtBillID.setText("#HD" + position);
        txtBillDay.setText(bill.getCreateDay());
        txtTotal.setText("Tổng tiền: " + bill.formatToVND(bill.getToTal()));
        ArrayList<Product> myListProductInBill = new ArrayList<>();
        ArrayList<Integer> listProductQuantityInHistory = new ArrayList<>();
        DetailBillProductService detailBillProductService = new DetailBillProductService();
        CompletableFuture<ArrayList<DetailBillProduct>> futureDetails = detailBillProductService.getDetailBillsByBillId(bill.getId());
        View finalConvertView = convertView;
        futureDetails.thenAccept(detailBillProducts -> {
            ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();

            for (DetailBillProduct detailBill : detailBillProducts) {
                Product product = new Product();
                CompletableFuture<Void> futureProduct = product.getProductById(detailBill.getProductId())
                        .thenAccept(p -> {
                            myListProductInBill.add(p);
                            listProductQuantityInHistory.add(detailBill.getQuantity());
                        }).exceptionally(throwable -> {
                            Toast.makeText(context, "Failed to retrieve product: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            return null;
                        });
                futures.add(futureProduct);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                // Lấy danh sách tên sản phẩm trong 1 bill
                ArrayList<String> listProductName = new ArrayList<>();
                for (Product product : myListProductInBill) {
                    listProductName.add(product.getName());
                }

                // Tạo layout mới chứa tên các sản phẩm trong bill
                LinearLayout productinBill = finalConvertView.findViewById(R.id.productinBill);
                int i = 0;
                for (String name : listProductName) {
                    TextView txt = new TextView(context);
                    txt.setText(listProductQuantityInHistory.get(i) + "x " + name);
                    txt.setPadding(5, 0, 0, 0);
                    txt.setTypeface(null, Typeface.BOLD);
                    txt.setTextSize(16);
                    productinBill.addView(txt);
                    i++;
                }
            }).exceptionally(throwable -> {
                Toast.makeText(context, "Failed to complete all tasks: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            });
        }).exceptionally(throwable -> {
            Toast.makeText(context, "Failed to retrieve detail bills: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
        return convertView;
    }
}
