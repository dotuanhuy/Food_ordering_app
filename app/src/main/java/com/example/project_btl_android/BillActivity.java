package com.example.project_btl_android;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * @noinspection ALL
 */
public class BillActivity extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    ArrayList<Product> myProductListInBill;
    ArrayAdapterInBill myAdapterInBill;
    ListView lvProductInBill;
    Double totalMoney = 0.0;
    Cart cart;
    String address = "Hà Nội";
    FusedLocationProviderClient fusedLocationClient;
    TextView txtBackFromBillToCart, txtOrder, txtTaxMoney, txtTotalMoneyWithTax, txtTotalMoneyProductInBill;
    EditText edtAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        //Mở database và ánh xạ ID
        txtTaxMoney = findViewById(R.id.txtTaxMoney);
        txtTotalMoneyWithTax = findViewById(R.id.txtTotalMoneyWithTax);
        txtTotalMoneyProductInBill = findViewById(R.id.txtTotalMoneyProductInBill);
        lvProductInBill = findViewById(R.id.lvProductInBill);
        myProductListInBill = new ArrayList<>();
        txtOrder = findViewById(R.id.txtToOrder);
        edtAddress = findViewById(R.id.edtAddress);
        txtOrder.setText("Đặt hàng");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getPlaceName(latitude, longitude);
                        } else {
                            Toast.makeText(BillActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Xử lý Intent đến từ DetailActivity
        if (getIntent().getAction() != null && getIntent().getAction().equals("FromDetail")) {
//            Product productFromDetail = (Product) getIntent().getSerializableExtra("ProductFromDetail");
            String id = getIntent().getStringExtra("productId");
            cart = (Cart) getIntent().getSerializableExtra("Cart");
            db.getReference("Product").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Product productFromDetail = snapshot.getValue(Product.class);
                    productFromDetail.quantityInCart(cart, productFromDetail.getId()).thenAccept(quantitiCart -> {
                        totalMoney = productFromDetail.toMoney(quantitiCart);
                        productFromDetail.setQuantityInCart(quantitiCart);
                        myProductListInBill.add(productFromDetail);
                        display();
                    }).exceptionally(ex -> {
                        Toast.makeText(BillActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        return null;
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        }
//        Intent đến từ Activity khác (Cart)
        else {
            ArrayList<String> arrIdProducts = (ArrayList<String>) getIntent().getStringArrayListExtra("selectProductList");
            totalMoney = getIntent().getDoubleExtra("Total", 1);
            cart = (Cart) getIntent().getSerializableExtra("Cart");

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String id : arrIdProducts) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                futures.add(future);

                db.getReference("Product").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Product productFromDetail = snapshot.getValue(Product.class);
                        productFromDetail.quantityInCart(cart, productFromDetail.getId()).thenAccept(quantitiCart -> {
//                            totalMoney = productFromDetail.toMoney(quantitiCart);
                            productFromDetail.setQuantityInCart(quantitiCart);
                            myProductListInBill.add(productFromDetail);

                            // Đánh dấu future này đã hoàn thành
                            future.complete(null);
                        }).exceptionally(ex -> {
                            Toast.makeText(BillActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            // Đánh dấu future này đã hoàn thành, ngay cả khi có lỗi
                            future.completeExceptionally(ex);
                            return null;
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        // Đánh dấu future này đã hoàn thành, ngay cả khi có lỗi
                        future.completeExceptionally(new Exception(error.getMessage()));
                    }
                });
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                // Tất cả các sản phẩm đã được xử lý, bây giờ hiển thị thông tin
                display();
            }).exceptionally(ex -> {
                Toast.makeText(BillActivity.this, "Failed to load all products: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            });
        }
    }

    private void display() {
        // Đưa danh sách sản phẩm lên giao diện
        myAdapterInBill = new ArrayAdapterInBill(BillActivity.this, R.layout.layout_product_inbill, myProductListInBill);
        lvProductInBill.setAdapter(myAdapterInBill);

        //Đưa tổng tiền (cùng thuế) lên giao diện
        txtTotalMoneyProductInBill.setText(formatToVND(totalMoney));
        double tax = totalMoney * 10.0 / 100;
        txtTaxMoney.setText(formatToVND(tax));
        txtTotalMoneyWithTax.setText(formatToVND(totalMoney + tax));

        //Tạo dialog xác nhận đặt hàng
        AlertDialog.Builder dialog = new AlertDialog.Builder(BillActivity.this);
        dialog.setTitle("XÁC NHẬN ĐẶT HÀNG");

        //Cancel => huỷ dialog
        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //OK => xử lý
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
                Toast.makeText(BillActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                //Đổi định dạng ngày
                LocalDate currentDate = LocalDate.now();
                int year = currentDate.getYear();
                int month = currentDate.getMonthValue();
                int dayOfMonth = currentDate.getDayOfMonth();
                String formattedDate = String.format("%04d-%02d-%02d", year, month, dayOfMonth);// Định dạng thành "YYYY-mm-dd"

                //Tạo bill mới với username, ngày, total
                String id = db.getReference("Bill").push().getKey();
                Bill bill = new Bill(id, cart.getUserId(), formattedDate, totalMoney, edtAddress.getText().toString() , "wait");

                db.getReference("Bill").child(id).setValue(bill)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                List<CompletableFuture<Void>> futures = new ArrayList<>();
                                for (Product product : myProductListInBill) {
                                    // Thêm detail bill
                                    DetailBillProduct detailBillProduct = new DetailBillProduct(bill.getId(), product.getId(), product.getQuantityInCart());
                                    DetailBillProductService detailBillProductService = new DetailBillProductService();
                                    CompletableFuture<Void> futureAddDetail = detailBillProductService.addDetailProductBill(detailBillProduct);
                                    futures.add(futureAddDetail);
                                    // Lấy số lượng sản phẩm
                                    CompletableFuture<Integer> futureGetQuantity = product.quantityInCart(cart, product.getId());
                                    futures.add(futureGetQuantity.thenCompose(quantity -> {
                                        // Giảm số lượng sản phẩm có trong hệ thống
                                        return product.updateProductQuantity(product.getId(), product.getQuantity() - quantity);
                                    }).exceptionally(throwable -> {
                                        Toast.makeText(getApplicationContext(), "Failed to update product quantity: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        return null;
                                    }));

                                    // Xóa sản phẩm ra khỏi giỏ hàng
                                    CompletableFuture<Void> futureDeleteCart = product.deleteProductFromCart(cart, product.getId());
                                    futures.add(futureDeleteCart);

                                    // Đặt lại quantityInCart
                                    product.setQuantityInCart(0);
                                }

                                // Chờ tất cả các tác vụ hoàn tất
                                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                                    txtOrder.setText("Đặt hàng thành công");
                                    txtOrder.setEnabled(false);
                                    myAdapterInBill.notifyDataSetChanged();
                                    showMessage();
                                }).exceptionally(throwable -> {
                                    Toast.makeText(getApplicationContext(), "Failed to complete all tasks: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    return null;
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to add bill", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        txtOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.create().show();
            }
        });

        txtBackFromBillToCart = findViewById(R.id.txtBackFromBillToCart);
        txtBackFromBillToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Thông báo đặt hàng thành công
    public void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_success, null);
        builder.setView(dialogView);
        AlertDialog dialog2 = builder.create();
        dialog2.show();
        TextView txtDone = dialogView.findViewById(R.id.txtDone);
        txtDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, gọi lại hàm lấy vị trí
                fusedLocationClient.getLastLocation()
                        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    Location location = task.getResult();
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    Toast.makeText(BillActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
//                                    getPlaceName(latitude, longitude);
                                } else {
                                    Toast.makeText(BillActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private void getPlaceName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String placeName = address.getAddressLine(0);
                edtAddress.setText(placeName);
            } else {
                Toast.makeText(BillActivity.this, "No address found for the location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(BillActivity.this, "Failed to get address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        // Tạo request để lấy tên địa điểm
//        String latLng = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
//        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
//        FetchPlaceRequest request = FetchPlaceRequest.builder(latLng, placeFields).build();

//        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
//            Place place = response.getPlace();
//            String placeName = place.getName();
//            address = placeName;
//            Toast.makeText(this, "Place: " + placeName, Toast.LENGTH_SHORT).show();
//        }).addOnFailureListener((exception) -> {
//            Toast.makeText(this, "Failed to get place name: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//        });
    }
    public String formatToVND(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount); // Định dạng số tiền
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}