package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;

/** @noinspection ALL*/
public class HomePageActivity extends AppCompatActivity {
    GridView gvProductInHomePage;
    TabHost tabHost;
    TabHost tabHostHis;
    SearchView svProductInHomePage;
    TextView txtNumberProductOfCart;
    ImageView imgBtnCartInHomePage;
    Cart cart;
    ListView lvBillHistory, lvHisWait, lvHisShipping, lvHisFinish;
    ArrayList<Product> myListProduct, displayedProducts, myListProductToCategory;
    ArrayAdapterInHomePage myAdapterInHomePage;
    ArrayAdapterHistory myArrayAdapterInHistory;
    ArrayList<Bill> myListBill;
    ArrayList<Category> myListCategory;
    int currentTab = 1, currentItemInSpn = 0, currentTabHis = 1;
    long backPressTime;
    Spinner spnCategoryInHomePage;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Boolean isTabHostSetup = false;
    Account user;
    /** @noinspection deprecation*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        cart = new Cart();
        user = new Account();
        user = (Account) getIntent().getSerializableExtra("user");

        getCartByUserName();
        //Khi load giao diện, thực hiện thêm các control và khởi tạo tab 1
        addControl();
        tab1_action();
        spnCategoryInHomePage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myListProductToCategory.clear();
                currentItemInSpn = position;
                Category category = (Category) parentView.getItemAtPosition(position);
                if (category.getId().equals("#1")) {
                    loadProducts();
                }
                else{
                    loadProductByCategory(category.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không có mục nào được chọn trong Spinner
            }
        });
    }

    private void addControl() {
        //Định nghĩa tabHost
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1, spec2;
        //Tab1
        spec1 = tabHost.newTabSpec("t1");
        spec1.setContent(R.id.tab_homepage);
        spec1.setIndicator("", getResources().getDrawable(R.drawable.home));
        tabHost.addTab(spec1);
        //Tab2
        spec2 = tabHost.newTabSpec("t2");
        spec2.setContent(R.id.tab_history);
        spec2.setIndicator("", getResources().getDrawable(R.drawable.history));
        tabHost.addTab(spec2);

        //Khi chuyển tab
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("t2")) {
                    tab2_action();
                }
                else {
                    tab1_action();
                }
            }
        });

        //Ánh xạ ID
        svProductInHomePage = findViewById(R.id.svProductInHomePage);
        gvProductInHomePage = findViewById(R.id.gvProductInHomePage);
        txtNumberProductOfCart = findViewById(R.id.txtNumberProductOfCart);
        imgBtnCartInHomePage = findViewById(R.id.imgBtnCartInHomePage);
//        lvBillHistory = findViewById(R.id.lvBillHistory);
        spnCategoryInHomePage = findViewById(R.id.spnCategoryInHomePage);
        myListProduct = new ArrayList<>();
        displayedProducts = new ArrayList<>();
        myListCategory = new ArrayList<>();
        myListProductToCategory = new ArrayList<>();
    }

    //Khi load lại activity, load lại số sản phẩm trong giỏ hàng lên giao diện
    @Override
    protected void onResume() {
        super.onResume();
        setNumberProductInCart();
    }

    public void tab1_action(){
        setNumberProductInCart();
        currentTab = 1;
        //Tránh trùng lặp khi load giao diện nhiều lần
        myListProduct.clear();
        myListCategory.clear();
        //Lấy danh sách sản phẩm có trong CSDL
        loadProducts();
        Category defaultCategory = new Category("#1", "Tất cả");
        myListCategory.add(0, defaultCategory);
        db.getReference("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data: snapshot.getChildren()) {
                    Category category = data.getValue(Category.class);
                    if (category != null) {
                        myListCategory.add(category);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Load data failed "+error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, myListCategory) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position trong danh sách drop-down
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoryInHomePage.setAdapter(adapter);

        //Load dữ liệu lên giao diện
        myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, myListProduct);
        gvProductInHomePage.setAdapter(myAdapterInHomePage);

        //Khi click vào 1 sản phẩm -> chuyển đến trang chi tiết sản phẩm
        gvProductInHomePage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentToDetailActivity = new Intent(HomePageActivity.this, DetailProductActivity.class);
                Product SelectProduct = myListProduct.get(position);
                intentToDetailActivity.putExtra("product", SelectProduct.getId());
                intentToDetailActivity.putExtra("Cart", cart);
                intentToDetailActivity.putExtra("user", user);
                startActivity(intentToDetailActivity);
            }
        });

        //Khi click vào biểu tượng giỏ hàng -> chuyển đến trang giỏ hàng
        imgBtnCartInHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToCartActivity = new Intent(HomePageActivity.this, CartActivity.class);
                intentToCartActivity.putExtra("Cart", cart);
                startActivity(intentToCartActivity);
            }
        });

        //Xử lý sự kiện tìm kiếm
        svProductInHomePage.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                tab1_action();

                filter(newText);
                return false;
            }
        });
    }

    //Tab lịch sử đơn hàng
    public void tab2_action(){
        tabHostHis = findViewById(R.id.tabHostHis);
        lvHisWait = findViewById(R.id.lvHisWait);
        lvHisShipping = findViewById(R.id.lvHisShipping);
        lvHisFinish = findViewById(R.id.lvHisFinish);
        myListBill = new ArrayList<>();
        setUpTab();
        tabHostHis.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("t1")) {
                    tab_wait();
                }
                else if (tabId.equals("t2")) {
                    tab_shipping();
                }
                else {
                    tab_finish();
                }
            }
        });
        tab_wait();
    }
    private void setUpTab() {
        if (!isTabHostSetup) {
            tabHostHis.setup();
            TabHost.TabSpec spec1, spec2, spec3;
            //Tab1
            spec1 = tabHostHis.newTabSpec("t1");
            spec1.setContent(R.id.tab_his_wait);
            spec1.setIndicator("Chờ đơn", getResources().getDrawable(R.drawable.history));
            tabHostHis.addTab(spec1);
            //Tab2
            spec2 = tabHostHis.newTabSpec("t2");
            spec2.setContent(R.id.tab_his_shipping);
            spec2.setIndicator("Chờ giao hàng", getResources().getDrawable(R.drawable.history));
            tabHostHis.addTab(spec2);
            // Tab3
            spec3 = tabHostHis.newTabSpec("t3");
            spec3.setContent(R.id.tab_his_finish);
            spec3.setIndicator("Đã giao", getResources().getDrawable(R.drawable.history));
            tabHostHis.addTab(spec3);

            isTabHostSetup = true;
        }
    }
    private void tab_wait() {
        currentTabHis = 1;
        showDataTab("wait", lvHisWait);
    }
    private void tab_shipping() {
        currentTabHis = 2;
        showDataTab("shipping", lvHisShipping);
    }
    private void tab_finish() {
        currentTabHis = 3;
        showDataTab("finish", lvHisFinish);
    }

    private void showDataTab(String status, ListView lv) {
        myListBill.clear();
        db.getReference("Bill").orderByChild("userId").equalTo(cart.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Bill> bills = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bill bill = snapshot.getValue(Bill.class);
                    if (bill != null && status.equals(bill.getStatus())) {
                        bills.add(bill);
                    }
                }
                myListBill = bills;
                myArrayAdapterInHistory = new ArrayAdapterHistory(HomePageActivity.this, R.layout.layout_item_bill_history, myListBill);
                lv.setAdapter(myArrayAdapterInHistory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //Đếm số sản phẩm trong giỏ hàng
    public void setNumberProductInCart(){
        myRef = db.getReference("Detail_ProductCart");
        myRef.orderByChild("cartId").equalTo(cart.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int quantityInCart = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        quantityInCart += 1;
                    }
                }
                cart.setProductQuantity(quantityInCart);
                txtNumberProductOfCart.setText(String.valueOf(cart.getProductQuantity()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Load data failed "+error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Tìm kiếm
    private void filter(String searchText) {
        ArrayList<Product> filteredProducts = new ArrayList<>();
        if (!searchText.isEmpty()) {
            // Lặp qua danh sách sản phẩm đã lọc theo danh mục và thêm các sản phẩm phù hợp vào danh sách lọc
            // Xóa dấu và đưa về chữ in thường
            String searchTextWithoutAccents = removeAccents(searchText.toLowerCase());
            for (Product item : myListProductToCategory) {
                String productName = removeAccents(item.getName().toLowerCase());
                if (productName.contains(searchTextWithoutAccents)) {
                    filteredProducts.add(item);
                }
            }
        } else {
            // Nếu searchText rỗng, hiển thị toàn bộ danh sách sản phẩm mặc định hoặc theo danh mục
            if(spnCategoryInHomePage.getSelectedItemPosition() == 0){
                filteredProducts.addAll(myListProduct);
            }
            else {
                filteredProducts.addAll(displayedProducts);
            }
        }

        // Cập nhật lại danh sách hiển thị với danh sách đã lọc
        myAdapterInHomePage.clear();
        myAdapterInHomePage.addAll(filteredProducts);
        myAdapterInHomePage.notifyDataSetChanged();
    }


    //Xóa dấu thanh
    public static String removeAccents(String input) {
        String regex = "\\p{InCombiningDiacriticalMarks}+";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll(regex, "");
    }

    //Khi onBack: tab2->tab1, tab1->nhấn lần 2->login
    @Override
    public void onBackPressed() {
        if (!svProductInHomePage.getQuery().toString().isEmpty()) {
            // Clear the text in searchView
            svProductInHomePage.setQuery("", false);
            svProductInHomePage.clearFocus();
            return; // Do not proceed further
        }

        if (currentTab == 2) { // Nếu đang ở tab 2
            // Chuyển tab về tab 1
            tabHost.setCurrentTab(0);
            currentTab = 1; // Cập nhật lại trạng thái hiện tại của tab
            tab1_action();
        } else {
            if(backPressTime + 3000 > System.currentTimeMillis()){
                super.onBackPressed();
                return;
            }
            else{
                Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
            }
            backPressTime = System.currentTimeMillis();
        }
    }

    private void loadProductByCategory(String categoryId) {
        db.getReference("Product").orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Product product = userSnapshot.getValue(Product.class);
                        myListProductToCategory.add(product);
                    }

                }
                else {
                    myListProductToCategory.clear();
                    Toast.makeText(HomePageActivity.this, "Không tồn tại sản phẩm thỏa mãn", Toast.LENGTH_SHORT).show();
                }
                myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, myListProductToCategory);
                gvProductInHomePage.setAdapter(myAdapterInHomePage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Error getting data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        db.getReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myListProduct.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Product product = userSnapshot.getValue(Product.class);
                    myListProduct.add(product);
                }
//                myAdapterInHomePage.notifyDataSetChanged();
                myListProductToCategory.addAll(myListProduct);
                myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, myListProductToCategory);
                gvProductInHomePage.setAdapter(myAdapterInHomePage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Load data failed "+error.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCartByUserName() {
        //Lấy ra userName để lấy đúng giỏ hàng của người dùng
        String cartId = getIntent().getStringExtra("cartId");
        cart.setId(cartId);
        cart.setUserId(user.getId());

    }
}