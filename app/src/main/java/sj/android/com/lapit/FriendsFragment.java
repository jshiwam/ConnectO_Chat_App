package sj.android.com.lapit;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private View mMainView;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView=inflater.inflate(R.layout.fragment_friends,container,false);

        mFriendsList=mMainView.findViewById(R.id.fragment_friends_list);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendsDatabase,Friends.class).build();
        final FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {
                holder.setDate(model.getDate());
                final String list_user_id=getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName=dataSnapshot.child("name").getValue().toString();
                        final String user_thumb_Image=dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){
                            String userOnline=dataSnapshot.child("online").getValue().toString();
                            holder.setOnline(userOnline);
                        }

                        holder.setName(userName);
                        holder.setUserImage(user_thumb_Image);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[]=new CharSequence[]{"View Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options")
                                        .setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                              switch (i){
                                                  case 0:
                                                      Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                                      profileIntent.putExtra("user_id",list_user_id);
                                                      startActivity(profileIntent);
                                                      break;
                                                  case 1:
                                                      Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                                      chatIntent.putExtra("user_id",list_user_id);
                                                      chatIntent.putExtra("user_name",userName);
                                                      chatIntent.putExtra("user_thumb_image",user_thumb_Image);
                                                      startActivity(chatIntent);
                                                      break;
                                              }
                                            }
                                        });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
                return new FriendsViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        mFriendsList.setAdapter(firebaseRecyclerAdapter);

    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setDate(String date){
            TextView friendDateView=mView.findViewById(R.id.users_single_status);
            friendDateView.setText(date);
        }
        public void setName(String name){
            TextView friendNameView=mView.findViewById(R.id.users_single_name);
            friendNameView.setText(name);
        }
        public void setUserImage(final String image){
            final CircleImageView friendImageView=mView.findViewById(R.id.users_single_image);

            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.deafult_image_icon_xhdpi).into(friendImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(image).placeholder(R.drawable.deafult_image_icon_xhdpi).into(friendImageView);
                }
            });
        }
        public void setOnline(String online_status){
            ImageView userOnlineStatus=mView.findViewById(R.id.user_single_online_icon);
            if(online_status.equals("true")){
                userOnlineStatus.setVisibility(View.VISIBLE);
            }else{
                userOnlineStatus.setVisibility(View.INVISIBLE);
            }
        }

    }
}
