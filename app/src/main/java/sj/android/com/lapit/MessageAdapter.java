package sj.android.com.lapit;

import android.app.ActionBar;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;

import java.util.List;



public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();


    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList=mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String currentUid=mAuth.getCurrentUser().getUid();

        Messages c=mMessageList.get(position);
        String from_user=c.getFrom();

        if(from_user.equals(currentUid)){
            holder.messageText.setBackgroundResource(R.drawable.bubble1);
            holder.messageText.setTextColor(Color.BLACK);
            holder.params.gravity=Gravity.RIGHT;
            }else{
            holder.messageText.setBackgroundResource(R.drawable.bubble2);
            holder.messageText.setTextColor(Color.BLACK);
            holder.params.gravity=Gravity.LEFT;
        }
        holder.messageText.setText(c.getMessage());
        holder.messageText.setLayoutParams(holder.params);


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public ImageView messageImage;
        LinearLayout.LayoutParams params;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_text_layout);
            messageImage=itemView.findViewById(R.id.message_image_layout);
            params=(LinearLayout.LayoutParams) messageText.getLayoutParams();

        }
    }
}

