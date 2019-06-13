package sj.android.com.lapit;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionPageAdapter mSectionPageAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();
        currentUser= mAuth.getCurrentUser();
        if(currentUser!=null)
        {mUserref=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());}

        mToolbar= findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        mViewPager=findViewById(R.id.tabPager);
        mSectionPageAdapter=new SectionPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPageAdapter);

        mTabLayout=findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);




    }
    public void onStart(){
        super.onStart();


        if(currentUser == null){
            startActivityIntent();
    }else{
            mUserref.child("online").setValue(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if((currentUser)!=null) {
            mUserref.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void startActivityIntent() {

            Intent intent=new Intent(MainActivity.this,StartActivity.class);
            startActivity(intent);
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.menu_logout_btn){
            FirebaseAuth.getInstance().signOut();
            startActivityIntent();
        }
        if(item.getItemId()==R.id.menu_settings_btn){
            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);

        }
        if(item.getItemId()==R.id.menu_all_btn){
            Intent intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }
        return true;
    }
}

