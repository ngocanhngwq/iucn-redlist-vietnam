package com.technifysoft.bookapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.MyApplication;
import com.technifysoft.bookapp.activities.PdfDetailActivity;
import com.technifysoft.bookapp.databinding.RowPdfFavoriteBinding;
import com.technifysoft.bookapp.models.ModelPdf;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    //view binding for row_pdf_favorite.xml
    private RowPdfFavoriteBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    //constructor
    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind/inflate row_pdf_favorite.xml layout
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        /*---Get data, set data, handle click---*/
        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model, holder);

        //handle click, open pdf details page, already done in previous videos
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId()); //pass book id not category id
                context.startActivity(intent);
            }
        });

        //long click to delete book
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                deleteBookDialog(model.getId(), model.getTitle());
                return false;
            }
        });

        //handle click, remove from favorite
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context, model.getId()); //pass book id not category id
            }
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: Book Details of Book ID: "+bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get book info
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = String.valueOf(snapshot.child("timestamp").getValue());
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        String imageBase64 = snapshot.child("image").getValue() + "";
                        String content = snapshot.child("content").getValue() + "";

                        //validate timestamp
                        if(timestamp == null || TextUtils.isEmpty(timestamp)) {
                            timestamp = "1659293326000";    //01/08/2022
                        }
                        Log.w(TAG, "timestamp: " + timestamp);

                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        try {
                            model.setTimestamp(Long.parseLong(timestamp));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);
                        model.setImage(imageBase64);
                        model.setContent(content);

                        //format Date
                        String date = "N/A";
                        try {
                            date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadPdfFromUrlSinglePage(imageBase64, ""+bookTitle,
                                holder.imgView, holder.progressBar, null);

                        //content size
                        if(!TextUtils.isEmpty(content)) {
                            if(content.length() < 1024) {
                                binding.sizeTv.setText(String.format(Locale.getDefault(), "%d B", content.length()));
                            } else {
                                binding.sizeTv.setText(String.format(Locale.getDefault(), "%.1f KB", (float) content.length() / 1024));
                            }
                        }

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return list size || nuber of records
    }

    //ViewHolder class
    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        ImageView imgView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;

        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            //init ui views of row_pdf_favorite.xml
            imgView = binding.imgView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }

    private void deleteBookDialog(String bookId, String bookTitle) {
        //setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Delete book");
        alertDialog.setMessage("Are you sure to delete?");
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.w(TAG, "Delete book: " + bookId);
                MyApplication.deleteBook(context, bookId, "bookUrl", bookTitle);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
