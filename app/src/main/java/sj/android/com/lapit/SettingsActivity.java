package sj.android.com.lapit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button mChange_status;
    private Button mChange_image;
    private FirebaseUser mcurrentUser ;
    private ProgressBar mProgressBar;

    private DatabaseReference mDatabaseUser;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = findViewById(R.id.settings_image);
        mDisplayName = findViewById(R.id.settings_dispname);
        mStatus = findViewById(R.id.settings_status);
        mChange_status = findViewById(R.id.settings_status_button);
        mChange_image = findViewById(R.id.settings_image_button);
        mProgressBar = findViewById(R.id.prog_ba);
        mProgressBar.setVisibility(View.INVISIBLE);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        String currentUid = mcurrentUser.getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mDatabaseUser.keepSynced(true);

        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.deafult_image_icon_xhdpi).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.deafult_image_icon_xhdpi).into(mDisplayImage);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mChange_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = mStatus.getText().toString();

                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);
            }
        });

        mChange_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseUser.child("online").setValue(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseUser.child("online").setValue(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

            //Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);
                final Uri resultUri = result.getUri();
                File thumb_file_path=new File(resultUri.getPath());

                Bitmap thumb_bitmap=null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75).compressToBitmap(thumb_file_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();





                mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                final String currentUid = mcurrentUser.getUid();
                StorageReference filepath = mImageStorage.child("profile_images").child(currentUid + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(currentUid+".jpg");
                if (resultUri != null) {

                    //We Will setup an OnCompleteListener to store the image in the desired location in the storage.
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            //If the task is Successful we will display a toast.
                            if (task.isSuccessful()) {

                                mImageStorage.child("profile_images").child(currentUid+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        final String downloadUrl = uri.toString();
                                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {
                                                mImageStorage.child("profile_images").child("thumbs").child(currentUid+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri thumb_uri) {
                                                    String thumb_downloadUrls= thumb_uri.toString();
                                                        if(thumb_task.isSuccessful()){
                                                            Map<String,Object> updateHashMap=new HashMap<String,Object>();
                                                            updateHashMap.put("image",downloadUrl);
                                                            updateHashMap.put("thumb_image",thumb_downloadUrls);

                                                            mDatabaseUser.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(SettingsActivity.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                                                        mProgressBar.setVisibility(View.INVISIBLE);

                                                                    }

                                                                }
                                                            });
                                                        }else{
                                                            Toast.makeText(SettingsActivity.this, "Problem in Uploading Thumb image", Toast.LENGTH_LONG).show();
                                                            mProgressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                });


                                            }
                                        });



                                    }
                                });
                            } else {
                                Toast.makeText(SettingsActivity.this, "Problem in Storing image", Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }

    }

}

