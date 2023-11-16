package com.example.chitchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.model.UserModel;
import com.example.chitchat.utils.AndroidUtil;
import com.example.chitchat.utils.FirebaseUtil;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView1, textView2;
    Animation top, bottom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        imageView = findViewById(R.id.imageView);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);


        top = AnimationUtils.loadAnimation(this, R.anim.top);
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom);
        imageView.setAnimation(top);
        textView1.setAnimation(bottom);
        textView2.setAnimation(bottom);

        if(FirebaseUtil.isLoggedIn() && getIntent().getExtras() != null){
            ///From notification
            String userId = getIntent().getExtras().getString("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    UserModel model = task.getResult().toObject(UserModel.class);

                    Intent mainIntent = new Intent(this,MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);

                    Intent intent = new Intent(this, ChatActivity.class);
                    assert model != null;
                    AndroidUtil.passUserModelAsIntent(intent,model);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });


        }
        else{

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (FirebaseUtil.isLoggedIn()){
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    }
                    else{
                        startActivity(new Intent(SplashActivity.this,LoginPhoneNumberActivity.class));
                    }

                    finish();
                }
            },3000);
        }

    }
}