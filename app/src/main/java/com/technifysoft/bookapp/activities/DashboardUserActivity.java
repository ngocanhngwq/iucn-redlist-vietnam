package com.technifysoft.bookapp.activities;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.BooksUserFragment;
import com.technifysoft.bookapp.R;
import com.technifysoft.bookapp.databinding.ActivityDashboardUserBinding;
import com.technifysoft.bookapp.models.ModelCategory;

import java.util.ArrayList;
import java.util.Locale;

public class DashboardUserActivity extends AppCompatActivity {
    private final String TAG = DashboardUserActivity.class.getSimpleName();

    //to show in tabs
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    //view binding
    private ActivityDashboardUserBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //Start loading, show dialog waiting for...
        showProgressDialog("Loading", "Please wait...");

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle click, logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        //handle click, open profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });

//        TextView newtext = (TextView) findViewById(R.id.note1);
//        newtext.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        categoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories"); //be careful of spellings
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.w(TAG, "setupViewPagerAdapter: onDataChange");
                //clear before adding to list
                categoryArrayList.clear();

                /*Load Categories - Static e.g. All, Most Viewed, Most Downloaded*/
                //Add data to models
                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);
//                ModelCategory modelMostDownloaded = new ModelCategory("03", "Most Downloaded", "", 1);
                //add models to list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
//                categoryArrayList.add(modelMostDownloaded);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCategory(),
                        ""+modelAll.getUid()
                ), modelAll.getCategory());
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUid()
                ), modelMostViewed.getCategory());
//                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
//                        ""+modelMostDownloaded.getId(),
//                        ""+modelMostDownloaded.getCategory(),
//                        ""+modelMostDownloaded.getUid()
//                ), modelMostDownloaded.getCategory());

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //Now Load from firebase
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add data to list
                    categoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getCategory(),
                            ""+model.getUid()), model.getCategory());
                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }

                //load done, hide dialog
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        //TODO: load sẵn (remain nội dung) bao nhiêu tab trên viewPager -> max tab
        int MAX_REMAIN_TAB = 3; //Math.max(categoryArrayList.size(), 3);
        viewPager.setOffscreenPageLimit(MAX_REMAIN_TAB);

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter( FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        public ViewPagerAdapter( FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BooksUserFragment fragment, String title){
            //add fragment passed as parameter in fragmentList
            fragmentList.add(fragment);
            //add title passed as parameter in fragmentTitleList
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }


    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            //not logged in
            binding.subTitleTv.setText("Not Logged In");
        }
        else {
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subTitleTv.setText(email);
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
                progressDialog = new ProgressDialog(this);
            }
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(false);
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