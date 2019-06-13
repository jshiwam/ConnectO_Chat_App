package sj.android.com.lapit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    private EditText mUsername;
    private EditText mEmail;
    private EditText mPassword;
    private Button mCreatbtn;
    private Toolbar mToolbar;

    //ProgerssDialog
    private ProgressDialog mRegProgress;

    //Firebase
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar=(Toolbar) findViewById(R.id.reg_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Android Fields
        mUsername=findViewById(R.id.reg_dispname);
        mEmail=findViewById(R.id.reg_email);
        mPassword=findViewById(R.id.reg_password);
        mCreatbtn=findViewById(R.id.reg_create_btn);

        mRegProgress=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        mCreatbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String disp_name=mUsername.getText().toString();
                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();
                if(!TextUtils.isEmpty(disp_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password))

                { mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_User(disp_name,email,password);}
                    else{
                    Toast.makeText(RegisterActivity.this,"Can't Leave the Fields Empty",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register_User(final String disp_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    mRegProgress.dismiss();
                    Log.d("Lapit", "createUserWithEmail:success");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = user.getUid();
                    String deviceToken=FirebaseInstanceId.getInstance().getToken();
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("device_token",deviceToken);
                    userMap.put("name", disp_name);
                    userMap.put("status", "hey there,I am using Lapit Chat App");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");

                    mDatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });


                }else{
                    mRegProgress.hide();
                    Log.d("Lapit","createUserWithEmail:failure",task.getException());
                    Toast.makeText(RegisterActivity.this,"Authentication failed. ",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }



}
