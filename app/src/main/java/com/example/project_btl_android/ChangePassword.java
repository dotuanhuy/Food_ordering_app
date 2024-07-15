package com.example.project_btl_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class ChangePassword extends AppCompatActivity {
    EditText txtNewPassword, txtReNewPassword;
    TextView txtSubmitInChangePassword;
    String phone;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtReNewPassword = findViewById(R.id.txtReNewPassword);
        txtSubmitInChangePassword = findViewById(R.id.txtSubmitInChangePassword);

        String convertedPhoneNumber = getIntent().getStringExtra("phone");
        phone  = convertedPhoneNumber.replaceAll("^\\+84", "0");
        txtSubmitInChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtNewPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                }
                else if (!txtReNewPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                }
                else if (!txtNewPassword.getText().toString().equals(txtReNewPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                }
                else {
                    db.getReference("Account").orderByChild("phoneNumber").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                // Update password
                                userSnapshot.getRef().child("password").setValue(txtReNewPassword.getText().toString(), new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null) {
                                            Intent intent = new Intent(ChangePassword.this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Log.d("test", "Failed to update password: " + error.getMessage());
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("TAG", "onCancelled: " + error.getMessage());
                            // Xử lý khi có lỗi khi truy vấn dữ liệu
                        }
                    });
                }
            }
        });
    }
}