package com.example.project_btl_android;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CartActivity extends AppCompatActivity {
    TextView txtBackFromCartToHomepage, txtToOrder, txtSelectAllInCart, txtTotalMoneyProductSelect;
    ListView lvProductInCart;
    ArrayList<Product> myListProductInCart, mySelectProductList;
    ArrayAdapterInCart myArrayAdapterInCart;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    Cart cart;
    float x1 = 0, x2 = 0;
    Boolean check = false;
    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        //Hiển thị danh sách trong giỏ hàng và biến check selectAll = fasle
        showCart();
        check = false;

        //Chuyển về lại homepage
        txtBackFromCartToHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Thanh toán
        // Nếu danh sách các sản phẩm được chọn không rỗng -> chuyển dữ liệu sang trang thanh toán
        txtToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mySelectProductList != null && !mySelectProductList.isEmpty()) {
                    ArrayList<String>arrIdProducts = new ArrayList<>();
                    for (Product p : mySelectProductList) {
                        arrIdProducts.add(p.getId());
                    }
                    android.content.Intent intent = new android.content.Intent(CartActivity.this, BillActivity.class);
                    intent.putExtra("Total", Double.parseDouble(cart.getTotal() + ""));
                    intent.putStringArrayListExtra("selectProductList", arrIdProducts);
                    intent.putExtra("Cart", cart);

                    startActivity(intent);
                } else {
                    Toast.makeText(CartActivity.this, "Bạn chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Chỉ hủy chọn tất cả khi tất cả các sản phaarmd đang được chọn
        //Nếu 1 hoặc nhiều sản phẩm đang được chọn -> chọn tất
        txtSelectAllInCart = findViewById(R.id.txtSelectAllInCart);
        txtSelectAllInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    ArrayList<Product> tmpListProductInCart = new ArrayList<>();
                    tmpListProductInCart.addAll(myListProductInCart);
                    for (Product product:myListProductInCart) {
                        CompletableFuture<Integer> quantityInCartFuture = product.quantityInCart(cart, product.getId());
                        CompletableFuture<Integer> quantityVisibleFuture = product.checkQuantityVisible();
                        CompletableFuture.allOf(quantityInCartFuture, quantityVisibleFuture).thenRun(() -> {
                            try {
                                int quantityInCart = quantityInCartFuture.get();
                                int quantityVisible = quantityVisibleFuture.get();

                                if (quantityInCart > quantityVisible) {
                                    tmpListProductInCart.remove(product);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    if(mySelectProductList.size() == tmpListProductInCart.size()){
                        for (int i = 0; i<myListProductInCart.size(); i++){
                            //Các biến lưu trữ trạng thái check box được cập nhật
                            myArrayAdapterInCart.checkboxStates.set(i, false);
                        }
                        mySelectProductList.removeAll(myListProductInCart);
                    }
                    else {
                        mySelectProductList.removeAll(myListProductInCart);
                        for (int i = 0; i<myListProductInCart.size(); i++){
                            //Các biến lưu trữ trạng thái check box được cập nhật
                            myArrayAdapterInCart.checkboxStates.set(i, true);
                            mySelectProductList.add(myListProductInCart.get(i));
                            myArrayAdapterInCart.checkEnable(myListProductInCart.get(i), i);
                        }
                    }
                }
                catch (Exception e){
                    Toast.makeText(CartActivity.this, "buggg: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //Cập nhật lại giao diện
                myArrayAdapterInCart.toStringTotal(cart);
                myArrayAdapterInCart.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        showCart();
//        myArrayAdapterInCart.toStringTotal(cart);
    }

    //Thêm sản phẩm vào giỏ hàng -> đưa vào danh sách sản phẩm được chọn
    public void addSelectProduct(Product product){
        mySelectProductList.add(product);
    }

    //Xóa ra khỏi giỏ hàng
    public void removeSelectProduct(Product product){
        mySelectProductList.remove(product);
    }

    public void showCart(){
        myListProductInCart = new ArrayList<>();
        mySelectProductList = new ArrayList<>();
        txtTotalMoneyProductSelect = findViewById(R.id.txtTotalMoneyProductSelect);
        lvProductInCart = findViewById(R.id.lvProductInCart);
        txtBackFromCartToHomepage = findViewById(R.id.txtBackFromCartToHomepage);
        txtToOrder = findViewById(R.id.txtToOrder);
        //Lấy cart được gửi từ homepage -> xác định cart theo người dùng
        cart = (Cart) getIntent().getSerializableExtra("Cart");

        // Lấy danh sách các id sản phẩm có trong chi tiết giỏ hàng
        db.getReference("Detail_ProductCart").orderByChild("cartId").equalTo(cart.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myListProductInCart.clear();
                    ArrayList<ProductDetailCart> productDetailCartsList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ProductDetailCart productDetailCart = snapshot.getValue(ProductDetailCart.class);
                        productDetailCartsList.add(productDetailCart);
                    }
                    getProductDetails(productDetailCartsList);
                }
                else {
                    LinearLayout layout = findViewById(R.id.main);
                    layout.setBackgroundResource(R.drawable.rong);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Load data failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private CompletableFuture<Void> getProductDetailAsync(ProductDetailCart productDetailCart) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.getReference("Product").child(productDetailCart.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    product.setQuantityInCart(productDetailCart.getQuantity());
                    if (product != null) {
                        myListProductInCart.add(product);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show();
                }
                // Gắn giá trị null vào CompletableFuture để đánh dấu rằng yêu cầu lấy dữ liệu đã hoàn thành
                future.complete(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Load data failed", Toast.LENGTH_SHORT).show();
                future.complete(null);
            }
        });
        return future;
    }

    private void getProductDetails(ArrayList<ProductDetailCart> productDetailCartsList) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Duyệt qua danh sách các productId và thêm mỗi yêu cầu lấy dữ liệu vào danh sách CompletableFuture
        for (ProductDetailCart productDetailCart : productDetailCartsList) {
            futures.add(getProductDetailAsync(productDetailCart));
        }

        // Tạo một CompletableFuture chứa tất cả các yêu cầu lấy dữ liệu từ Firebase
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // Đợi cho đến khi tất cả các yêu cầu lấy dữ liệu hoàn thành
        allFutures.thenRun(() -> {
            //Đưa trạng thái các sản phẩm trong giỏ hàng về chưa được chọn
            //Xóa các sản phẩm trong danh sách sản phẩm được chọn

            for (Product product:myListProductInCart) {
                product.setIscheck(false);
                mySelectProductList.clear();
            }
            // Hiển thị các sản phẩm lên giao diện
            myArrayAdapterInCart = new ArrayAdapterInCart(CartActivity.this, R.layout.layout_product_incart, myListProductInCart);
            //set cart để đưa cart vào các phương thức cần thực hiện trong adapter
            myArrayAdapterInCart.setCart(cart);
            myArrayAdapterInCart.setDatabase(db);
            lvProductInCart.setAdapter(myArrayAdapterInCart);
        });
    }
}