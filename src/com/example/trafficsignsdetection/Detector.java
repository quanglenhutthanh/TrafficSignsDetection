package com.example.trafficsignsdetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;

public class Detector {
	private Activity activity;
	private CascadeClassifier cascadeClassifier;
	public Detector(Activity activity){
		this.activity = activity;
	}
	public void Detect(Mat mGray,MatOfRect signs,int type){
		//loadCascadeFile(type, cascadeClassifier);
		loadCascadeFile(type);
		if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, signs, 1.1, 3, 0, new Size(30,30),new Size());
        }
	}
	private void loadCascadeFile(int type){
		try {
			InputStream is = null;
			File cascadeDir = activity.getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile=null;
			switch (type) {
			case 1:
				is = activity.getResources().openRawResource(R.raw.bienbaocam);
				
				cascadeFile = new File(cascadeDir, "bienbaocam.xml");
				break;
			case 2:
			default:
				is = activity.getResources().openRawResource(R.raw.biennguyhiem);
				
				cascadeFile = new File(cascadeDir, "biennguyhiem.xml");
				break;
			}
			
			FileOutputStream os = new FileOutputStream(cascadeFile);
			byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
 
 
            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
