package com.example.trafficsignsdetection;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.Bitmap;

public class Utilities {
	public static Bitmap convertMatToBitmap(Mat src){
		Bitmap bm = Bitmap.createBitmap(src.cols(), 
				src.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bm);
        //Imgproc.cvtColor(src, dst, code, dstCn)
        return bm;
	}
}
