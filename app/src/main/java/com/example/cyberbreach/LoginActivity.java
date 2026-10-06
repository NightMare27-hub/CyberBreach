package com.example.cyberbreach;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbHelper db = DbHelper.get(this);
        if (!TextUtils.isEmpty(db.getUsername())) {
            proceedToNext();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConnect.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "Enter a valid handle", Toast.LENGTH_SHORT).show();
                return;
            }
            db.setUsername(username);
            proceedToNext();
        });
    }

    private void proceedToNext() {
        DbHelper db = DbHelper.get(this);
        if (!db.isTutorialCompleted()) {
            Intent i = new Intent(this, TutorialActivity.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        finish();
    }
}

