package com.technifysoft.bookapp.filters;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.Filter;

import com.technifysoft.bookapp.Util;
import com.technifysoft.bookapp.adapters.AdapterPdfUser;
import com.technifysoft.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    //arraylist in which we want to serch
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to be implementd
    AdapterPdfUser adapterPdfUser;

    //constructor
    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value to be searched should not be null/empty
        if (!TextUtils.isEmpty(constraint)) {
            //not null nor empty
            //change to uppercase or lower case to avoid case sensitivity
            String contentKeyWord = constraint.toString().toLowerCase();
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            //search theo title hoặc content
            for (int i = 0; i < filterList.size(); i++) {
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //search matches, add to list
                    filteredModels.add(filterList.get(i));
                } else {
                    //not matches title, search by content if keyword length >= 3
                    if(contentKeyWord.length() >= 3) {
                        if (filterList.get(i).getContent().contains(contentKeyWord)) {
                            filteredModels.add(filterList.get(i));
                        }
                    }
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            //empty or null, make original list/result
            results.count = filterList.size();
            results.values = filterList;
        }
        return results; ///dont misss it
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        //notify changes
        adapterPdfUser.notifyDataSetChanged();
    }

    public void applySearchResult(ArrayList<ModelPdf> filteredList) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = filteredList;

        //notify changes
        adapterPdfUser.notifyDataSetChanged();
    }
}
