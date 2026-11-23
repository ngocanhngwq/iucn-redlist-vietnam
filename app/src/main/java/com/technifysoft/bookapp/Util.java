package com.technifysoft.bookapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import Catalano.IO.*;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.IProcessImage;
import Catalano.Imaging.Tools.ObjectiveFidelity;
import Catalano.Math.Dissimilarities.DiceDissimilarity;

public class Util {
    public static float compareImage(Bitmap srcBitmap, String imgBase64) {
//        long startTime = System.currentTimeMillis();
        try {
            // decode base64 string
            byte[] bytes = Base64.decode(imgBase64, Base64.DEFAULT);
            // Initialize bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap dstBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            FastBitmap fastBitmapDest = new FastBitmap(dstBitmap);

            //scale src bitmap to same size dst bitmap
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(srcBitmap, dstBitmap.getWidth(), dstBitmap.getHeight(), false);
            FastBitmap fastBitmapSrc = new FastBitmap(resizedBitmap);

            //compare image
            ObjectiveFidelity o = new ObjectiveFidelity(fastBitmapSrc, fastBitmapDest);

//        // Error total
//        int error = o.getTotalError();
//        //Signal Noise Ratio
//        double snr = o.getSNR();
//        //Peak Signal Noise Ratio
//        double psnr = o.getPSNR();
//        //getDerivativeSNR
//        double dsnr = o.getDerivativeSNR();

            //Root Mean Square Error
            //double similarity = 1 - rmse;
            //Log.w("Compare", "rmse: " + rmse);
            return (float) Math.sqrt(o.getMSE());
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getApplicationDataPath() {
        return Environment.getExternalStorageDirectory().toString() + "/Android/data/" +
                BuildConfig.APPLICATION_ID+ "/files";
    }
}
