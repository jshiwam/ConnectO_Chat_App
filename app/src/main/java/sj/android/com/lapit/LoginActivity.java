package sj.android.com.lapit;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mToolbar;

    //Android Fields
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;

    //Firebase Authentication
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    //Progress Dialog
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail=findViewById(R.id.login_email);
        mPassword=findViewById(R.id.login_password);
        mLogin=findViewById(R.id.login_btn);
        mAuth=FirebaseAuth.getInstance();
        mProgressBar=(ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             String email=mEmail.getText().toString();
             String password=mPassword.getText().toString();
            if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){
             login_user(email,password);
            }else{
                Toast.makeText(LoginActivity.this,"Can't Leave the Fields Empty",Toast.LENGTH_SHORT);
            }
            }
        });


    }

    public void login_user(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
             if(task.isSuccessful()){
                 mProgressBar.setVisibility(View.VISIBLE);

                 String deviceToken= FirebaseInstanceId.getInstance().getToken();
                 String CurrentUid=mAuth.getCurrentUser().getUid();

                 mUserDatabase.child(CurrentUid).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Log.d("Lapit","There is a problem in setting a token id in the database");
                        }
                     }
                 });

             } else{
                 mProgressBar.setVisibility(View.INVISIBLE);
                 Toast.makeText(LoginActivity.this,"Authentication failed",Toast.LENGTH_SHORT).show();
             }
            }
        });

    }
}
