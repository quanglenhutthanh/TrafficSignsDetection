package com.example.trafficsignsdetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.NativeCameraView;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;



import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity implements CvCameraViewListener2{
	
	private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
	private CameraBridgeViewBase mCameraView;
	private ListView listDetectedSigns;
	private RelativeLayout listRelativeLayout;
	private CascadeClassifier cascadeClassifier;
	private ArrayList<Sign> listSign;
	private Detector detector;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	        @Override
	        public void onManagerConnected(int status) {
	            switch (status) {
	                case LoaderCallbackInterface.SUCCESS:
	                	mCameraView.enableView();
	                	Initialze();
	                	detector = new Detector(CameraActivity.this);
	                    break;
	                default:
	                    super.onManagerConnected(status);
	                    break;
	            }
	        }
	    };
	private Mat mRgba;
	private Mat mGray;
	
	private void Initialze(){
		//detector = new Detector(CameraActivity.this);
		//loadCascadeFile(1, cascadeClassifier);
		
		try {
			mCameraView.enableView();
			
			InputStream is = getResources().openRawResource(R.raw.biennguyhiem);
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir, "biennguyhiem.xml");
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.camera_preview);
		mCameraView = (CameraBridgeViewBase)findViewById(R.id.mCameraView);
		listDetectedSigns = (ListView)findViewById(R.id.listView1);
		listRelativeLayout = (RelativeLayout)findViewById(R.id.listViewLayout);
		mCameraView.setCvCameraViewListener(this);
		listRelativeLayout.setVisibility(View.GONE);
	}
	@Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		// TODO Auto-generated method stub
		  // Create a grayscale image
		mRgba = inputFrame.rgba();
		
		//mRgba.setTo)
        mGray = inputFrame.gray();
        Imgproc.equalizeHist(mGray, mGray);
        MatOfRect signs = new MatOfRect();
        //faces.
        /*if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, signs, 1.1, 3, 0, new Size(30,30),new Size());
        }*/
        listSign = new ArrayList<Sign>();  
        detector.Detect(mGray, signs,1);
        Rect[] facesArray = signs.toArray();
        Draw(facesArray);
        detector.Detect(mGray, signs,2);
        Rect[] signssArray = signs.toArray();
        Draw(signssArray); 
        //Core.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);*/
        return mRgba;
	}
	
	public void Draw(Rect[] facesArray){
		if(facesArray.length<=0){
        	runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					listRelativeLayout.setVisibility(View.GONE);
				}
			});
        	
        }
        for (int i = 0; i <facesArray.length; i++){
        	final int ii = i;
        	Mat subMat = new Mat();
        	subMat = mRgba.submat(facesArray[i]);
        	Sign.myMap.put("image"+i, Utilities.convertMatToBitmap(subMat));
        	
        	Core.rectangle(mRgba,facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 2);
        	
        	runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Sign sign = new Sign("unknown", "image"+ii);
		        	listSign.add(sign);
					listRelativeLayout.setVisibility(View.VISIBLE);
					itemAdapter adapter= new itemAdapter(listSign, CameraActivity.this);
					adapter.notifyDataSetChanged();
					listDetectedSigns.setAdapter(adapter);
				}
			});
        	
        }
	}
}
