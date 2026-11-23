package com.technifysoft.bookapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.technifysoft.bookapp.MyApplication;
import com.technifysoft.bookapp.activities.PdfDetailActivity;
import com.technifysoft.bookapp.activities.PdfEditActivity;
import com.technifysoft.bookapp.databinding.RowPdfAdminBinding;
import com.technifysoft.bookapp.filters.FilterPdfAdmin;
import com.technifysoft.bookapp.models.ModelPdf;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private static final String TAG = "AdapterPdfAdmin";

    //context
    private final Context context;

    //arraylist to hold list of data of type ModelPdf
    public ArrayList<ModelPdf> pdfArrayList, filterList;

    //view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    //progress
    private ProgressDialog progressDialog;

    //constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout using view binding
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        /*Get data, set data, handle clicks etc.*/

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();
        String content = model.getContent();
        String imageBase64 = model.getImage();

        //we need to convert timestamp to dd/MM/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //content size
        if(!TextUtils.isEmpty(content)) {
            if(content.length() < 1024) {
                binding.sizeTv.setText(String.format(Locale.getDefault(), "%d B", content.length()));
            } else {
                binding.sizeTv.setText(String.format(Locale.getDefault(), "%.1f KB", (float) content.length() / 1024));
            }
        }

        //load further details like category, pdf from url, pdf size in seprate functions
        MyApplication.loadCategory(
                ""+ categoryId,
                holder.categoryTv);

        //we dont need page number here, pass null
        MyApplication.loadPdfFromUrlSinglePage(
                imageBase64,
                ""+title,
                holder.imgView,
                holder.progressBar,
                null
        );

        //handel click, show dialog with options 1) Edit, 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        //handle book/pdf click, open pdf details page, pass pdf/book id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        //options to show in dialog
        String[] options = {"Edit", "Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if (which==0){
                            //Edit clicked, Open PdfEditActivity to edit the book info
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        }
                        else if (which==1) {
                            //Delete Clicked
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                        }
                    }
                })
                .show();
    }







    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return number of records | list size
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    /*View Holder class for row_pdf_admin.xml*/
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //UI Views of row_pdf_admin.xml
        ImageView imgView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull  View itemView) {
            super(itemView);

            //init ui views
            imgView = binding.imgView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;

        }
    }
}
