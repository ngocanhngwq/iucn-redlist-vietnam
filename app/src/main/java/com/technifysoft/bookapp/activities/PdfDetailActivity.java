package com.technifysoft.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

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
import com.technifysoft.bookapp.databinding.ActivityBookDetailBinding;
import com.technifysoft.bookapp.databinding.DialogCommentAddBinding;
import com.technifysoft.bookapp.models.ModelComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PdfDetailActivity extends AppCompatActivity {
    private static final String TAG = "PDFDetailActivity";

    //view binding
    private ActivityBookDetailBinding binding;

    //pdf id, get from intent
    String bookId, bookTitle, bookUrl;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;
    private Bitmap bookImageBmp = null;

    //arraylist to hold comments
//    private ArrayList<ModelComment> commentArrayList;
    //adapter to set to recyclerview
//    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.g. bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetails();

        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        //handle click on the thumbnail image
        binding.imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hiển thị ảnh phóng to
                zoomImageFromThumb(binding.imgView);
            }
        });

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    if (isInMyFavorite) {
                        //in favorite, remove from favorite
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                    } else {
                        //not in favorite, add to favorite
                        try {
                            MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                        } catch (Exception e) {
                            Toast.makeText(PdfDetailActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        //handle click, show comment add dialog
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Requirements: User must be logged in to add comment*/
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in...", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(PdfDetailActivity.this, BookCommentActivity.class);
                    intent.putExtra("bookId", bookId);
                    startActivity(intent);
                }
            }
        });

        //handle delete click
//        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MyApplication.deleteBook(PdfDetailActivity.this, bookId, bookUrl, bookTitle);
//            }
//        });
    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        //get data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String bookImage = "" + snapshot.child("image").getValue();
                        String bookContent = "" + snapshot.child("content").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        bookImageBmp = MyApplication.loadPdfFromUrlSinglePage(
                                bookImage,
                                ""+bookTitle,
                                binding.imgView,
                                binding.progressBar,
                                null
                        );
                        //content size
                        if(!TextUtils.isEmpty(bookContent)) {
                            if(bookContent.length() < 1024) {
                                binding.sizeTv.setText(String.format(Locale.getDefault(), "%d B", bookContent.length()));
                            } else {
                                binding.sizeTv.setText(String.format(Locale.getDefault(), "%.1f KB", (float) bookContent.length() / 1024));
                            }
                            binding.tvContent.setText(bookContent);
                        }

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
    }


    private void checkIsFavorite(){
        //logged in check if its in favorite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists(); //true: if exists, false if not exists
                        if (isInMyFavorite){
                            //exists in favoirte
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else {
                            //not exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Add Favorite");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    /**
     * Hiển thị ảnh phóng to của thumbnail
     * https://developer.android.com/develop/ui/views/animations/zoom
     * @param thumbView :
     */
    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        if(bookImageBmp == null) return;

        // Load the high-resolution "zoomed-in" image.
        binding.zoomImageView.setImageBitmap(bookImageBmp);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.bookDetailContainer).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        //thumbView.setAlpha(0f);
        binding.zoomImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        binding.zoomImageView.setPivotX(0f);
        binding.zoomImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(binding.zoomImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        binding.zoomImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(binding.zoomImageView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.Y,startBounds.top))
                        .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(binding.zoomImageView, View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        binding.zoomImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        binding.zoomImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }



//    private void loadComments() {
//        //init arraylist before adding data into it
//        commentArrayList = new ArrayList<>();
//
//        //db path to load comments
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
//        ref.child(bookId).child("Comments")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        //clear arraylist before start adding data into it
//                        commentArrayList.clear();
//                        for (DataSnapshot ds: snapshot.getChildren()){
//                            //get data as model, spellings of variables in model must be as same as in firebase
//                            ModelComment model = ds.getValue(ModelComment.class);
//                            //add to arraylist
//                            commentArrayList.add(model);
//                        }
//                        //setup adapter
//                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);
//                        //set adapter to recyclerview
//                        binding.commentsRv.setAdapter(adapterComment);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

//    private String comment = "";
//    private void addCommentDialog() {
//        //inflate bind view for dialog
//        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));
//
//        //setup alert dialog builder
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
//        builder.setView(commentAddBinding.getRoot());
//
//        //create and show alert dialog
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//
//        //handle click, dismis dialog
//        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//
//        //handle click, add comment
//        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //get data
//                comment = commentAddBinding.commentEt.getText().toString().trim();
//                //validate data
//                if (TextUtils.isEmpty(comment)){
//                    Toast.makeText(PdfDetailActivity.this, "Enter your comment...", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    alertDialog.dismiss();
//                    addComment();
//                }
//            }
//        });
//
//    }

//    private void addComment() {
//        //show progress
//        progressDialog.setMessage("Adding comment...");
//        progressDialog.show();
//
//        //timestamp for comment id, comment time
//        String timestamp = ""+System.currentTimeMillis();
//
//        //setup data to add in db for comment
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("id", ""+timestamp);
//        hashMap.put("bookId", ""+bookId);
//        hashMap.put("timestamp", ""+timestamp);
//        hashMap.put("comment", ""+comment);
//        hashMap.put("uid", ""+firebaseAuth.getUid());
//
//        //DB path to add data into it
//        //Books > bookId > Comments > commentId > commentData
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
//        ref.child(bookId).child("Comments").child(timestamp)
//                .setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Toast.makeText(PdfDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
//                        progressDialog.dismiss();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //failed to add comment
//                        progressDialog.dismiss();
//                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }

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