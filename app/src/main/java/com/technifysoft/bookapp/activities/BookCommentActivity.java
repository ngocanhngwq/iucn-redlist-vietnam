package com.technifysoft.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.MyApplication;
import com.technifysoft.bookapp.R;
import com.technifysoft.bookapp.adapters.AdapterComment;
import com.technifysoft.bookapp.databinding.ActivityBookCommentBinding;
import com.technifysoft.bookapp.databinding.ActivityBookDetailBinding;
import com.technifysoft.bookapp.databinding.DialogCommentAddBinding;
import com.technifysoft.bookapp.models.ModelComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class BookCommentActivity extends AppCompatActivity {
    private static final String TAG = "COMMENT_TAG";

    //view binding
    private ActivityBookCommentBinding binding;

    //pdf id, get from intent
    private String bookId, bookTitle, bookUrl;
    private String comment = "";

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //arraylist to hold comments
    private ArrayList<ModelComment> commentArrayList;
    //adapter to set to recyclerview
    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.g. bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadComments();

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, show comment add dialog
        binding.btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Requirements: User must be logged in to add comment*/
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(BookCommentActivity.this, "You're not logged in...", Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });
    }

    private void loadComments() {
        //init arraylist before adding data into it
        commentArrayList = new ArrayList<>();

        try {
            //db path to load comments
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
            ref.child(bookId).child("Comments")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //clear arraylist before start adding data into it
                            commentArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                //get data as model, spellings of variables in model must be as same as in firebase
                                ModelComment model = ds.getValue(ModelComment.class);
                                //add to arraylist
                                commentArrayList.add(model);
                            }
                            //setup adapter
                            adapterComment = new AdapterComment(BookCommentActivity.this, commentArrayList);
                            //set adapter to recyclerview
                            binding.commentsRv.setAdapter(adapterComment);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            Toast.makeText(BookCommentActivity.this, "Can't load comment...", Toast.LENGTH_SHORT).show();
        }
    }


    private void addCommentDialog() {
        //inflate bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        //setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //handle click, dismis dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //handle click, add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data
                comment = commentAddBinding.commentEt.getText().toString().trim();
                //validate data
                if (TextUtils.isEmpty(comment)){
                    Toast.makeText(BookCommentActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        //show progress
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        //timestamp for comment id, comment time
        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //DB path to add data into it
        //Books > bookId > Comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(BookCommentActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add comment
                        progressDialog.dismiss();
                        Toast.makeText(BookCommentActivity.this, "Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //request storage permission
//    private final ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted){
//                    Log.d(TAG, "Permission Granted");
//                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
//                }
//                else {
//                    Log.d(TAG, "Permission was denied...: ");
//                    Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show();
//                }
//            });
}