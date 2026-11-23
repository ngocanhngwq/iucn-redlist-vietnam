package com.technifysoft.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.technifysoft.bookapp.BooksUserFragment;
import com.technifysoft.bookapp.Util;
import com.technifysoft.bookapp.databinding.ActivityPaintBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class PaintActivity extends AppCompatActivity {
    private static final String TAG = "Paint";

    private ActivityPaintBinding binding;
    private Intent parentIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentIntent = getIntent();
        if(parentIntent != null) {
            try {
                String value = parentIntent.getStringExtra("value");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        binding = ActivityPaintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.paintImvTest.setVisibility(View.INVISIBLE);

        binding.paintBtnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isErase = binding.simpleDrawingView1.onClickEraser();
                if(isErase) {
                    binding.paintBtnErase.setText("Pen");
                } else {
                    binding.paintBtnErase.setText("Eraser");
                }
            }
        });

        binding.paintBtnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.simpleDrawingView1.onClickUndo();
            }
        });

        binding.paintBtnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.simpleDrawingView1.onClickRedo();
            }
        });

        binding.paintBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.simpleDrawingView1.onClickClearAll();
            }
        });

        binding.paintBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap mBitmap = binding.simpleDrawingView1.onClickSearch();
                if(mBitmap != null) {
                    //Log.w(TAG, "bmp size:" + mBitmap.getByteCount());
                    //Test show ảnh xem nó thế nào
                    //binding.paintImvTest.setImageBitmap(mBitmap);
                    
                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                    byte[] byteArray = bStream.toByteArray();

                    //Release all paint resource and back to parent screen
                    binding.simpleDrawingView1.onClickExit();
                    if(parentIntent == null) {
                        Log.w(TAG, "parentIntent = null!");
                        finish();
                    } else {
                        parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        parentIntent.putExtra("image", byteArray);
                        setResult(Activity.RESULT_OK, parentIntent);
                        finish();
                    }
                } else {
                    Toast.makeText(PaintActivity.this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.paintBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.simpleDrawingView1.onClickExit();
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "onBackPressed");
        binding.simpleDrawingView1.onClickExit();
        super.onBackPressed();
    }


    private void saveImage(Bitmap srcBitmap, String filePath) {
        boolean isFileCreated = false;
        try {
            Log.w(TAG, "save file -> " + filePath);
            File jpegFile = new File(filePath);
            isFileCreated = jpegFile.createNewFile();

            //Compress to JPEG: not limit output size
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream);
            byte[] bitmapData = byteOutputStream.toByteArray();
            //Write the bytes in file
            FileOutputStream fos = new FileOutputStream(jpegFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close(); //do not forget to close the stream

            //Check dung lượng file, nếu file không tồn tại thì exception luôn, không lưu DB
            Log.d(TAG, "JPEG: " + jpegFile.length());
        } catch (Exception e) {
            Log.e(TAG, "SAVE_IMG_ERR:" + e.getMessage() + " :" + isFileCreated);
        }
    }
}