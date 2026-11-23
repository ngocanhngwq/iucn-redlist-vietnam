package com.technifysoft.bookapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.activities.BookAddActivity;
import com.technifysoft.bookapp.activities.PaintActivity;
import com.technifysoft.bookapp.adapters.AdapterPdfUser;
import com.technifysoft.bookapp.databinding.FragmentBooksUserBinding;
import com.technifysoft.bookapp.models.ModelPdf;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    //view binding
    private FragmentBooksUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";

    public BooksUserFragment() {
        // Required empty public constructor
    }


    public static BooksUserFragment newInstance(String categoryId, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate");

        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: Category: " + category);
        if (category.equals("All")) {
            //load all books
            loadAllBooks();
        } else if (category.equals("Most Viewed")){
            //load most viewed books
            loadMostViewedDownloadedBooks("viewsCount");
        }
//        else if (category.equals("Most Downloaded")){
//            //load most downloaded books
//            loadMostViewedDownloadedBooks("downloadsCount");
//        }
        else {
            //load selected category books
            loadCategorizedBooks();
        }

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type any letter
                try {
                    adapterPdfUser.getFilter().filter(s);
                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //handle click to take image
        binding.searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Pick image");
                showImageAttachMenu();
            }
        });

        //handle click to clear image search result
        binding.clearSearchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clear image search");
                clearSearchByImage();
                binding.clearSearchImageBtn.setVisibility(View.GONE);
                binding.imvPaintImage.setImageBitmap(null);
                binding.imvPaintImage.setVisibility(View.GONE);
            }
        });

        return binding.getRoot();
    }

    private void loadAllBooks() {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);

                    //Bỏ những model bị null category, null id
                    if(TextUtils.isEmpty(model.getCategoryId())) {
                        Log.w(TAG, "Category empty: " + model.getId());
                    } else if(TextUtils.isEmpty(model.getId())) {
                        Log.e(TAG, "bookId empty: " + model.getId());
                    } else {
                        //add to list
                        pdfArrayList.add(model);
                    }
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                //set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedBooks(String orderBy) {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10) //load 10 most viewed or downloaded books
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);

                    //Bỏ những model bị null category, null id
                    if(TextUtils.isEmpty(model.getCategoryId())) {
                        Log.w(TAG, "Category empty: " + model.getId());
                    } else if(TextUtils.isEmpty(model.getId())) {
                        Log.e(TAG, "bookId empty: " + model.getId());
                    } else {
                        //add to list
                        pdfArrayList.add(model);
                    }
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                //set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void loadCategorizedBooks(){
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //add to list
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        //set adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
    }

    /**
     * Pick image for search by image
     */
    private void showImageAttachMenu() {
        //init/setup popup menu
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.searchImage);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Tìm kiếm bằng ảnh từ...");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "Paint");
        popupMenu.show();

        //handle menu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get id of item clicked
                int which = item.getItemId();
                if (which == 1) {
                    //camera clicked
                    pickImageCamera();
                } else if (which == 2) {
                    //gallery clicked
                    pickImageGallery();
                } else if(which == 3) {
                    //paint click
                    pickImagePaint();
                }
                return false;
            }
        });
    }

    private Uri imageUri;
    private void pickImageCamera() {
        //intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick"); //image title
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImagePaint() {
        //intent to pick image from paint activity
        Intent intent = new Intent(getActivity(), PaintActivity.class);
        intent.putExtra("value", "Xin chao");
        paintActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of camera intent
                    //get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //uri: content://media/external/images/media/2573
                        Log.d(TAG, "onActivityResult: Picked From Camera " + imageUri);

                        //Lấy bitmap để compare
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                            searchByImage(bitmap);
                            bitmap.recycle();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    // get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked From Gallery " + imageUri);

                        //Lấy bitmap để compare
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                            searchByImage(bitmap);
                            bitmap.recycle();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> paintActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    // get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Log.d(TAG, "onActivityResult: " + Activity.RESULT_OK);
                        Intent intent = result.getData();
                        if(intent != null) {
                            byte[] byteArray = intent.getByteArrayExtra("image");
                            Log.w(TAG, "Get byteArray from Paint: " + byteArray.length);
                            //Convert byteArray to bitmap to compare
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray.length);
                                Log.w(TAG, "Pick bitmap from Paint: " + bitmap.getByteCount());
                                searchByImage(bitmap);
                                bitmap.recycle();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private int searchImageCount = 0;
    private void searchByImage(Bitmap bitmap) {
        Log.d(TAG, "Bitmap size: " + bitmap.getByteCount());

        showProgressDialog("Tìm kiếm", "Đang tìm kiếm bằng hình ảnh...");
        try {
            adapterPdfUser.getFilter();
            Bitmap bmp_Copy = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            binding.imvPaintImage.setImageBitmap(bmp_Copy);
            binding.imvPaintImage.setVisibility(View.VISIBLE);
            searchImageCount = adapterPdfUser.performFilterImage(bmp_Copy);
            if(searchImageCount > 0) {
                binding.clearSearchImageBtn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "Không tìm thấy kết quả phù hợp!", Toast.LENGTH_SHORT).show();
                binding.imvPaintImage.setImageBitmap(null);
                binding.imvPaintImage.setVisibility(View.GONE);
            }
        } catch (Exception e){
            Log.d(TAG, "searchByImage: " + e.getMessage());
        }
        hideProgressDialog();
    }

    private void clearSearchByImage() {
        try {
            if(searchImageCount > 0) {
                adapterPdfUser.clearSearchByImage();
            }
            searchImageCount = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ProgressDialog progressDialog = null;
    /**
     * Progress dialog
     * @param title
     * @param msg
     */
    private void showProgressDialog(String title, String msg) {
        try {
            if(progressDialog == null) {
                progressDialog = new ProgressDialog(getActivity());
            }
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        if(progressDialog != null) {
            progressDialog.cancel();
        }
        progressDialog = null;
    }

}