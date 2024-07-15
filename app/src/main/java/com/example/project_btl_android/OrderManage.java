package com.example.project_btl_android;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderManage extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    TabHost tabHost;
    ListView lvOrderWait, lvOrderShipping, lvOrderFinish;
    TextView txtBackToManagementHomepage;
    ArrayList<Bill> myListBill;
    ArrayAdapterOrder arrayAdapterOrder;
    int currentTab = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);

        tabHost = findViewById(R.id.tabhost2);
        txtBackToManagementHomepage = findViewById(R.id.txtBackToManagementHomepage);
        lvOrderWait = findViewById(R.id.lvOrderWait);
        lvOrderShipping = findViewById(R.id.lvOrderShipping);
        lvOrderFinish = findViewById(R.id.lvOrderFinish);
        myListBill = new ArrayList<>();

        tabHost.setup();
        TabHost.TabSpec spec1, spec2, spec3;
        //Tab1
        spec1 = tabHost.newTabSpec("t1");
        spec1.setContent(R.id.tab_wait);
        spec1.setIndicator("Chờ đơn", getResources().getDrawable(R.drawable.history));
        tabHost.addTab(spec1);
        //Tab2
        spec2 = tabHost.newTabSpec("t2");
        spec2.setContent(R.id.tab_shipping);
        spec2.setIndicator("Chờ giao hàng", getResources().getDrawable(R.drawable.history));
        tabHost.addTab(spec2);
        // Tab3
        spec3 = tabHost.newTabSpec("t3");
        spec3.setContent(R.id.tab_finish);
        spec3.setIndicator("Đã giao", getResources().getDrawable(R.drawable.history));
        tabHost.addTab(spec3);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
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

        txtBackToManagementHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void tab_wait() {
        currentTab = 1;
        show("wait", lvOrderWait);
    }

    private void tab_shipping() {
        currentTab = 2;
        show("shipping", lvOrderShipping);
    }

    private void tab_finish() {
        currentTab = 3;
        show("finish", lvOrderFinish);
    }

    private void show(String status, ListView lv) {
        myListBill.clear();
        db.getReference("Bill").orderByChild("status").equalTo(status).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Bill> bills = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Bill bill = snapshot.getValue(Bill.class);
                    bills.add(bill);
                }
                myListBill = bills;
                arrayAdapterOrder = new ArrayAdapterOrder(OrderManage.this, R.layout.layout_item_bill_manage, myListBill);
                lv.setAdapter(arrayAdapterOrder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}