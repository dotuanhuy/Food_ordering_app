package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class OTPVerify extends AppCompatActivity {
    EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    Button btnConfirm;
    EditText[] otpEditTexts;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    String phone;
    private String verificationId;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverify);

        mAuth = FirebaseAuth.getInstance();

        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);

        etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etOtp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etOtp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        android.content.Intent intent = getIntent();
        verificationId = intent.getStringExtra("verificationId");
        phone = intent.getStringExtra("phone");
        type = intent.getStringExtra("type");

        otpEditTexts = new EditText[]{etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder otp = new StringBuilder();
                for (EditText editText : otpEditTexts) {
                    otp.append(editText.getText().toString());
                }
                if (otp.length() == 6) {
                    verifyOTP(verificationId, otp.toString());
                } else {
                    Toast.makeText(getApplicationContext(), "OTP không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyOTP(String verificationId, String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (type.equals("register")) {
                            // Cập nhật trường isVerify trong cơ sở dữ liệu
                            CompletableFuture<Void> updateFuture = updateActiveStatusByPhoneNumber(phone);
                            updateFuture.thenAccept(aVoid -> {
                                Toast.makeText(OTPVerify.this, "Xác minh thành công", Toast.LENGTH_SHORT).show();
                                android.content.Intent intent = new android.content.Intent(OTPVerify.this, MainActivity.class);
                                startActivity(intent);
                            }).exceptionally(ex -> {
                                Toast.makeText(getApplicationContext(), "Đã xảy ra lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                return null;
                            });
                        }
                        else {
                            Intent intent = new Intent(OTPVerify.this, MainActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        }

                    } else {
                        Toast.makeText(OTPVerify.this, "Xác minh thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private CompletableFuture<Void> updateActiveStatusByPhoneNumber(String phoneNumber) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        // Tìm tài khoản theo số điện thoại
        firebaseDatabase.getReference("Account").orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Cập nhật trường active thành true cho tất cả các tài khoản tìm thấy
                            for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                                accountSnapshot.getRef().child("active").setValue(true)
                                        .addOnSuccessListener(aVoid -> future.complete(null))
                                        .addOnFailureListener(future::completeExceptionally);
                            }
                        } else {
                            // Số điện thoại không tồn tại
                            future.completeExceptionally(new Exception("Số điện thoại không tồn tại trong hệ thống"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý khi có lỗi
                        future.completeExceptionally(databaseError.toException());
                    }
        });

        return future;
    }

//    private void updateIsVerify(String phone) {
//        firebaseDatabase.getReference("Account").orderByChild("phoneNumber").equalTo(phone).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()) {
//                    String key = data.getKey();
//                    firebaseDatabase.getReference("Account").child(key).child("isVerify").setValue(true);
//                }
//            }
//
//            @Override
//            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}