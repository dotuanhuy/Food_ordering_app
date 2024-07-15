package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText edtUserName, edtPassword;
    TextView txtToRegisterActivity, txtForgetPassword;
    Button btnLogin;
    FirebaseDatabase db;
    DatabaseReference myRef;
    String cartId;
    String idUser = "";

    @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                FirebaseApp.initializeApp(this);
                //Xóa CSDL trong device
        //        deleteDatabase("qlSP.db");
                //Copy CSDL từ asset vào device
//                processCopy();
                edtUserName = findViewById(R.id.UserName);
                edtPassword = findViewById(R.id.PassWord);
                txtForgetPassword = findViewById(R.id.txtForgetPassword);
                edtUserName.setText("");
                edtPassword.setText("");
                btnLogin = findViewById(R.id.btnLogin);
                txtToRegisterActivity = findViewById(R.id.txtToRegisterActivity);
                db = FirebaseDatabase.getInstance();
                myRef = db.getReference("Account");
                //Xử lý sự kiện khi người dùng yêu cầu đăng ký -> chuyển đến activity đăng ký
                txtToRegisterActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        intent.setAction("FromMain");
                        startActivity(intent);
                    }
                });

                txtForgetPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage();
                    }
                });

                //Khi người dùng đăng nhập
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userName = edtUserName.getText().toString().trim();
                        String passWord = edtPassword.getText().toString();
                        String encryptedPassword = PasswordUtils.encryptPassword(passWord);
                        if(userName.isEmpty()){
                            edtUserName.setError("Vui lòng nhập tên đăng nhập/Email");
                            edtUserName.requestFocus();
                        } else if(passWord.isEmpty()){
                            edtPassword.setError("Vui lòng nhập mật khẩu");
                            edtPassword.requestFocus();
                        }
                        else{
                            myRef.orderByChild("userName").equalTo(userName).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean userFound = false;
                                    int role = -1;
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                                        Log.d("test", "check: " + userSnapshot.child("userName").getValue(String.class));
                                        boolean active = Boolean.TRUE.equals(userSnapshot.child("active").getValue(Boolean.class));
                                        if (!active) {

                                            Toast.makeText(MainActivity.this, "Tài khoản chưa được xác thực", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        else if (!dbPassword.equals(encryptedPassword)) {
                                            Toast.makeText(MainActivity.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        else {
                                            idUser = userSnapshot.getKey();
                                            userFound = true;
                                            role = userSnapshot.child("roleId").getValue(Integer.class);
                                            break;
                                        }
                                    }
                                    if (!userFound) {
                                        Toast.makeText(MainActivity.this, "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                        if (role == 1) {
                                            db.getReference("Cart").orderByChild("userId").equalTo(idUser).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            cartId = dataSnapshot.getValue(Cart.class).getId();
                                                        }
                                                        Account user = new Account(idUser, userName);
                                                        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                                                        intent.putExtra("user", user);
                                                        intent.putExtra("cartId", cartId);
                                                        startActivity(intent);
                                                    } else {
                                                        CartService cartService = new CartService();
                                                        cartService.addCart(idUser).thenAccept(cart -> {
                                                            Account user = new Account(idUser, userName);
                                                            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                                                            intent.putExtra("user", user);
                                                            intent.putExtra("cartId", cart.getId());
                                                            startActivity(intent);
                                                        }).exceptionally(ex -> {
                                                            // Xử lý trường hợp ngoại lệ (nếu có)
                                                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                            return null;
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(getApplicationContext(), "Load data failed "+error.toString(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                        else {
                                            Intent intent = new Intent(MainActivity.this, ManagementHomepageActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(MainActivity.this, "Error getting data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }

    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_forget_password, null);
        builder.setView(dialogView);
        TextView txtCancelInForgetPassword = dialogView.findViewById(R.id.txtCancelInForgetPassword);
        TextView txtSubmitInForgetPassword = dialogView.findViewById(R.id.txtSubmitInForgetPassword);
        EditText edtPhoneInForgetPassword = dialogView.findViewById(R.id.edtPhonelInForgetPassword);
        AlertDialog dialog2 = builder.create();
        txtCancelInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialog2.dismiss();}
        });
        txtSubmitInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edtPhoneInForgetPassword.getText().toString().trim();
                if(phone.isEmpty()){
                    edtPhoneInForgetPassword.setError("Vui lòng nhập số điện thoại");
                    edtPhoneInForgetPassword.requestFocus();
                    return;
                }
                else{
                    FirebaseAuth db = FirebaseAuth.getInstance();
                    String formatedPhoneNumber = "+84" + phone.substring(1);
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(db)
                            .setPhoneNumber(formatedPhoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Log.e("test", "OTP: " +  e.getMessage());
                                    Toast.makeText(MainActivity.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCodeSent(@NonNull String verificationId,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    Intent intent = new Intent(MainActivity.this, OTPVerify.class);
                                    intent.putExtra("verificationId", verificationId);
                                    intent.putExtra("type", "forgot");
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                }
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                }
                            }).build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
                dialog2.dismiss();
            }
        });
        dialog2.show();

    }
}