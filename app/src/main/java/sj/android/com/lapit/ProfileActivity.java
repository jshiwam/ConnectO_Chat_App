package sj.android.com.lapit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn,mDeclineReqBtn;

    private String mCurrentState;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootref;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mProfileImage=(ImageView) findViewById(R.id.profile_image);
        mProfileName=findViewById(R.id.profile_displayName);
        mProfileStatus=findViewById(R.id.profile_status);
        mProfileFriendsCount=findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn=findViewById(R.id.profile_send_req_btn);
        mDeclineReqBtn=findViewById(R.id.profile_decline_btn);


        mDeclineReqBtn.setEnabled(false);
        mDeclineReqBtn.setVisibility(View.INVISIBLE);

        mCurrentState="not_Friends";
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDatabase.keepSynced(true);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend Req");
        mFriendReqDatabase.keepSynced(true);
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootref=FirebaseDatabase.getInstance().getReference();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.deafult_image_icon_xhdpi).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.deafult_image_icon_xhdpi).into(mProfileImage);
                    }
                });

                //**********Friend List/Request Feature**************************************//
                mFriendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type= dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrentState="req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineReqBtn.setEnabled(true);
                                mDeclineReqBtn.setVisibility(View.VISIBLE);
                            }else if(req_type.equals("sent")){
                                mCurrentState="req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                            }
                        }else{
                            mFriendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrentState="friends";
                                        mProfileSendReqBtn.setText("Unfriend This Person");


                                        mDeclineReqBtn.setEnabled(false);
                                        mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                    }else{
                                        mCurrentState="not_Friends";
                                        mProfileSendReqBtn.setText("Send Friend Request");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);
                //****************************NOT Friends State***************************************
                if (mCurrentState.equals("not_Friends")) {
                    DatabaseReference newNotificationref = mRootref.child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap<>();
                    notificationData.put("from",currentUser.getUid());
                    notificationData.put("type","request");


                    Map requestMap=new HashMap();
                    requestMap.put("Friend Req/"+currentUser.getUid()+"/"+user_id+"/request_type","sent");
                    requestMap.put("Friend Req/"+user_id+"/"+currentUser.getUid()+"/request_type","received");

                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootref.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                          if(databaseError!=null){
                              Toast.makeText(ProfileActivity.this,"There is an error in sending request",Toast.LENGTH_SHORT).show();
                          }

                          mProfileSendReqBtn.setEnabled(true);

                            mCurrentState="req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                            Toast.makeText(ProfileActivity.this,"Request Sent",Toast.LENGTH_SHORT).show();


                        }
                    });
                }

                //****************************Cancel Request State***************************************
                if(mCurrentState.equals("req_sent")){
                    final String currentUid= currentUser.getUid();
                    Map removeReqMap= new HashMap();
                    removeReqMap.put(currentUid+"/"+user_id+"/request_type",null);
                    removeReqMap.put(user_id+"/"+currentUid+"/request_type",null);

                    mFriendReqDatabase.updateChildren(removeReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError==null){
                            mCurrentState="not_Friends";
                            mProfileSendReqBtn.setText("Send Friend Request");
                            Toast.makeText(ProfileActivity.this,"Request Cancelled",Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(ProfileActivity.this,"Request not Cancelled",Toast.LENGTH_SHORT).show();

                        }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                        }

                //******************************************Receive Request State*************************************
                if(mCurrentState.equals("req_received")){
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap=new HashMap();
                    friendsMap.put("Friends/"+currentUser.getUid()+"/"+user_id+"/date",currentDate);
                    friendsMap.put("Friends/"+user_id+"/"+currentUser.getUid()+"/date",currentDate);

                    friendsMap.put("Friend Req/"+currentUser.getUid()+"/"+user_id+"/request_type",null);
                    friendsMap.put("Friend Req/"+user_id+"/"+currentUser.getUid()+"/request_type",null);

                    mRootref.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                Toast.makeText(ProfileActivity.this,"You are friends now",Toast.LENGTH_SHORT).show();
                                mProfileSendReqBtn.setText("Unfriend This Person");
                                mCurrentState="friends";


                                mDeclineReqBtn.setEnabled(false);
                                mDeclineReqBtn.setVisibility(View.INVISIBLE);
                            }
                            else{
                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });


                                    }
                //********************************************Friends State Or Unfriend function****************************************//
                if(mCurrentState.equals("friends")){
                    Map unFriendmap=new HashMap();
                    unFriendmap.put(currentUser.getUid()+"/"+user_id+"/date",null);
                    unFriendmap.put(user_id+"/"+currentUser.getUid()+"/date",null);

                    mFriendDatabase.updateChildren(unFriendmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                       if(databaseError==null){
                           mProfileSendReqBtn.setText("Send Friend Request");
                           mCurrentState="not_Friends";
                       }else{
                           String error=databaseError.getMessage();
                           Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

                       }
                       mProfileSendReqBtn.setEnabled(true);
                        }
                    });



                }
            }

    });
        mDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map reqDeclinemap=new HashMap();
                    reqDeclinemap.put(currentUser.getUid()+"/"+user_id+"/request_type",null);
                    reqDeclinemap.put(user_id+"/"+currentUser.getUid()+"/request_type",null);

                    mFriendReqDatabase.updateChildren(reqDeclinemap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                         if(databaseError==null){
                             mProfileSendReqBtn.setText("Send Friend Request");
                             mCurrentState="not_Friends";

                             mDeclineReqBtn.setVisibility(View.INVISIBLE);
                             mDeclineReqBtn.setEnabled(false);
                         }   else{
                             String error=databaseError.getMessage();
                             Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                         }
                        }
                    });

            }
        });
}

    @Override
    protected void onStart() {
        super.onStart();
        mUserDatabase.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabase.child("online").setValue(false);
    }
}