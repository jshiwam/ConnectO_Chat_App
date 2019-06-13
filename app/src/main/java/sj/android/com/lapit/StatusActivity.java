package sj.android.com.lapit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText changestatusText;
    private Button changestatusBtn;
    private ProgressBar mProgressBar;

    //Firebase
    private DatabaseReference mDatabaseStatus;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar=findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value=getIntent().getStringExtra("status_value");

        mProgressBar=findViewById(R.id.status_progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        changestatusText=findViewById(R.id.change_status);
        changestatusBtn=findViewById(R.id.change_status_btn);

        changestatusText.setText(status_value);

        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        String uid=mCurrentUser.getUid();
        mDatabaseStatus=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabaseStatus.keepSynced(true);

        changestatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status=changestatusText.getText().toString();
                mProgressBar.setVisibility(View.VISIBLE);
                mDatabaseStatus.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressBar.setVisibility(View.INVISIBLE);


                        }else{
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(StatusActivity.this,"Problem in Updating Status",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
