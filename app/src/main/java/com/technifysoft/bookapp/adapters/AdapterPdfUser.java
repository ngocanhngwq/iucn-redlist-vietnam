package com.technifysoft.bookapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technifysoft.bookapp.MyApplication;
import com.technifysoft.bookapp.Util;
import com.technifysoft.bookapp.activities.PdfDetailActivity;
import com.technifysoft.bookapp.databinding.RowBookUserBinding;
import com.technifysoft.bookapp.databinding.RowPdfUserBinding;
import com.technifysoft.bookapp.filters.FilterPdfUser;
import com.technifysoft.bookapp.models.ModelPdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {
    private static final String TAG = "AdapterPdfUser";

    private final Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfUser filter;

    //row_book_user.xml
    private RowBookUserBinding binding;

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        //bind the view
        binding = RowBookUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfUser.HolderPdfUser holder, int position) {
        /*Get data, set data, handle click etc*/

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();
        String bookContent = model.getContent();
        String bookImage = model.getImage();

        //convert time
        String date = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        //content size
        if(!TextUtils.isEmpty(bookContent)) {
            if(bookContent.length() < 1024) {
                holder.sizeTv.setText(String.format(Locale.getDefault(), "%d B", bookContent.length()));
            } else {
                holder.sizeTv.setText(String.format(Locale.getDefault(), "%.1f KB", (float) bookContent.length() / 1024));
            }
        }

        //we dont need page number here, pass null
        MyApplication.loadPdfFromUrlSinglePage(
                bookImage,
                title,
                holder.imgView,
                holder.progressBar,
                null
        );
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );

        //handle click, show pdf details activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });

        //long click to delete book
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                deleteBookDialog(bookId, title);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return list size || number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            Log.d(TAG, "Create new filter...");
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    private class ImageFilterModels {
        ModelPdf modelPdf;
        float rmse;

        public ImageFilterModels(ModelPdf modelPdf, float rmse) {
            this.modelPdf = modelPdf;
            this.rmse = rmse;
        }
    }

    /**
     * Filter theo hình ảnh
     * @param bitmap
     */
    public int performFilterImage(Bitmap bitmap) {
        int searchCount = 0;
        long startTime = System.currentTimeMillis();

        //value to be searched should not be null/empty
        if (bitmap != null) {
            ArrayList<ImageFilterModels> filteredModels = new ArrayList<>();

            //compare ảnh chụp với ảnh của toàn bộ dữ liệu
            for (int i = 0; i< filterList.size(); i++) {
                if(!TextUtils.isEmpty(filterList.get(i).getImage())) {
                    float rmse = Util.compareImage(bitmap, filterList.get(i).getImage());
                    if(rmse >= 0) {
                        filteredModels.add(new ImageFilterModels(filterList.get(i), rmse));
                    }
                }
            }

            //Sort rmse theo thứ tự tăng dần
            Collections.sort(filteredModels, new Comparator<ImageFilterModels>() {
                @Override
                public int compare(ImageFilterModels t1, ImageFilterModels t2) {
                    return Float.compare(t1.rmse, t2.rmse);
                }
            });
            //Lấy 10 phần tử đầu có độ chính xác cao nhất
            ArrayList<ModelPdf> listModel = new ArrayList<>();
            int getCount = Math.min(filteredModels.size(), 10);
            for(int i = 0; i < getCount; i++) {
                listModel.add(filteredModels.get(getCount - i - 1).modelPdf);  //lấy ds ngược lại để hiển thị đúng độ ưu tiên rmse
                Log.w(TAG, "Top 10 kết quả tốt nhất:");
                Log.w(TAG, String.format("rmse: %.2f, title: %s",
                        filteredModels.get(i).rmse, filteredModels.get(i).modelPdf.getTitle()));
            }
            searchCount = listModel.size();
            //apply filter changes
            filter.applySearchResult(listModel);
        } else {
            filter.applySearchResult(filterList);
        }
        Log.w("Filter", "total time: " + (System.currentTimeMillis() - startTime));
        return searchCount;
    }

    /**
     * Clear kết quả tìm kiếm để hiển thị lại toàn bộ danh sách
     */
    public void clearSearchByImage() {
        if(filter != null) {
            filter.applySearchResult(filterList);
        }
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageView imgView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            imgView  = binding.imageView;
            progressBar = binding.progressBar;
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
