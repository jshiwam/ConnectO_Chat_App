package sj.android.com.lapit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef;
    private TextView mNameView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUid;

    private ImageButton mSendBtn;
    private ImageButton mAttachButton;
    private EditText mMessageBox;

    private List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD=10;
    private int mCurrentPage=1;

    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";

    private static final int GALLERY_PICK=1;
    private StorageReference mImageStorage;

    private RecyclerView mMessageList;
    private SwipeRefreshLayout mRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mSendBtn = findViewById(R.id.send_image_btn);
        mAttachButton = findViewById(R.id.attach_image_btn);
        mMessageBox = findViewById(R.id.write_message_text);

        mMessageList = findViewById(R.id.message_list);
        mRefreshLayout=findViewById(R.id.swipe_refresh_layout);
        mChatToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mChatToolbar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUid = mAuth.getCurrentUser().getUid();
        ActionBar actionBar = getSupportActionBar();

        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        mAdapter = new MessageAdapter(messagesList);
        mMessageList.setAdapter(mAdapter);



        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        String userImage = getIntent().getStringExtra("user_thumb_image");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custombar, null);
        actionBar.setCustomView(action_bar_view);

        mNameView = findViewById(R.id.custom_chat_name);
        mNameView.setText(userName);

        mImageStorage=FirebaseStorage.getInstance().getReference();
        mLastSeenView = findViewById(R.id.custom_chat_last_seen);

        mProfileImage = findViewById(R.id.custom_display_image);
        Picasso.get().load(userImage).placeholder(R.drawable.deafult_image_icon_xhdpi).into(mProfileImage);



        loadMessages();
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();


                if (online.equals("true")) {
                    mLastSeenView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRootRef.child("Chat").child(mCurrentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUid + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUid, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null)
                                Log.d("Chat", databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
               loadMoreMessages();
            }
        });

        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("Image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri = data.getData();

            final String current_user_ref ="messages/"+mCurrentUid+"/"+mChatUser;
            final String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUid;

            DatabaseReference user_message_push = mRootRef.child(current_user_ref).push();

            final String push_id = user_message_push.getKey();

            final StorageReference filepath=mImageStorage.child("message_images").child(push_id+".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                 if(task.isSuccessful()){
                     filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {
                           String downloadUrl=uri.toString();

                           Map messageMap=new HashMap();
                           messageMap.put("message ",downloadUrl);
                           messageMap.put("seen",false);
                           messageMap.put("type","image");
                           messageMap.put("time",ServerValue.TIMESTAMP);
                           messageMap.put("from",mCurrentUid);

                           Map messageUserMap=new  HashMap();
                           messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                           messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                           mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                                 @Override
                                 public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                     if(databaseError!=null){
                                         Log.d("Chat",databaseError.getMessage());
                                     }
                                 }
                             });

                         }
                     });
                 }
                }
            });

        }
    }

    private void loadMoreMessages() {
        String currentUserref="messages/"+mCurrentUid+"/"+mChatUser;
        DatabaseReference messageRef=mRootRef.child(currentUserref);

        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                String messageKey=dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }else{
                    mPrevKey=mLastKey;
                }

                if(itemPos==1){

                    mLastKey=messageKey;
                }
                Log.d("TotalKeys","LastKey: "+mLastKey+"| Prev key: "+ mPrevKey + "| Message Key:" + messageKey);

                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadMessages() {
        String currentUserref="messages/"+mCurrentUid+"/"+mChatUser;
        DatabaseReference messageRef=mRootRef.child(currentUserref);
        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(){
        String message=mMessageBox.getText().toString();
        if(!TextUtils.isEmpty(message)){
        String currentUserref="messages/"+mCurrentUid+"/"+mChatUser;
        String chatUserref="messages/"+mChatUser+"/"+mCurrentUid;

        Map messagesItemMap=new HashMap();
        messagesItemMap.put("message",message);
        messagesItemMap.put("seen",false);
        messagesItemMap.put("time",ServerValue.TIMESTAMP);
        messagesItemMap.put("type","text");
        messagesItemMap.put("from",mCurrentUid);


        String pushId=mRootRef.child("messages").child(mCurrentUid).child(mChatUser).push().getKey();

        Map messageMap=new HashMap();
        messageMap.put(currentUserref+"/"+pushId,messagesItemMap);
        messageMap.put(chatUserref+"/"+pushId,messagesItemMap);

        mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Log.d("Chat",databaseError.getMessage());
                }
            }
        });
    }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mCurrentUid).child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("Users").child(mCurrentUid).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
