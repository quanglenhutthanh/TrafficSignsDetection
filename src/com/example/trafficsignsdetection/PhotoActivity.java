package com.example.trafficsignsdetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PhotoActivity extends Activity implements OnClickListener{
	private Button btPick;
	private Button btCapture;
	private ImageView ivDisplay;
	private LinearLayout layoutResult;
	private Button btDetect;
	private Uri mUri;
	private Mat photoMat;
	private CascadeClassifier cascadeClassifier;
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	
	public static int pickCode = 1;
	public static int captureCode = 2;
	
	private ArrayList<Sign> listSign;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                	photoMat = new Mat();
                	break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_layout);
		Initialize();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
	}
	
	public void Initialize(){
		btPick = (Button)findViewById(R.id.btPick);
		btCapture = (Button)findViewById(R.id.btCapture);
		btDetect = (Button)findViewById(R.id.btDetect);
		ivDisplay = (ImageView)findViewById(R.id.ivDisplay);
		layoutResult = (LinearLayout)findViewById(R.id.layoutResult);
		//layoutResult.setVisibility(View.GONE);
		btDetect.setVisibility(View.GONE);
		btPick.setOnClickListener(this);
		btCapture.setOnClickListener(this);
		btDetect.setOnClickListener(this);
	}
	public void loadCascadeFile(int detectTypeId){
		try {
			InputStream is = null;
			File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = null;
			
			switch (detectTypeId) {
			case 1:
				is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
				cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
				break;
			case 2:
				is = getResources().openRawResource(R.raw.bienbaocam);
				cascadeFile = new File(cascadeDir, "traffic_signs.xml");
				break;
			case 3:
				is = getResources().openRawResource(R.raw.haarcascade_eye);
				cascadeFile = new File(cascadeDir, "haarcascade_eye.xml");
				break;
			default:
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
	private String getRealPathFromURI(Uri contentURI) {
	    String path;
	    Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
	    if (cursor == null) { // Source is Dropbox or other similar local file path
	        path = contentURI.getPath();
	    } else { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        path = cursor.getString(idx);
	        cursor.close();
	    }
	    return path;
	}
	public void Detect(Mat mGray){
		Imgproc.equalizeHist(mGray, mGray);
		MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 3, 0, new Size(30,30),new Size());
        }
        
        Rect[] facesArray = faces.toArray();
        listSign = new ArrayList<Sign>();
        Imgproc.cvtColor(photoMat, photoMat, Imgproc.COLOR_RGBA2BGR, 3);
        
        //get signs from photo
        for(int i = 0; i < facesArray.length; i++){
        	Mat subMat = new Mat();
        	subMat = photoMat.submat(facesArray[i]);
        	ImageView ivv = new ImageView(this);
        	ivv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 
                    LayoutParams.WRAP_CONTENT));
        	ivv.setImageBitmap(Utilities.convertMatToBitmap(subMat));
        	
        	Sign.myMap.put("image"+i, Utilities.convertMatToBitmap(subMat));
        	Sign sign = new Sign("unknown", "image"+i);
        	listSign.add(sign);
        	layoutResult.addView(ivv);
        	btDetect.setVisibility(View.GONE);
        	layoutResult.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(PhotoActivity.this,RegconitionActivity.class);
					intent.putParcelableArrayListExtra("key", (ArrayList<? extends Parcelable>) listSign);
					startActivity(intent);
					return false;
				}
			});
        }
        //draw rectangle
        for (int i = 0; i < facesArray.length; i++){
        	Core.rectangle(photoMat,facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        ivDisplay.setImageBitmap(Utilities.convertMatToBitmap(photoMat));
    }
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.
	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btPick:
			layoutResult.removeAllViews();
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 1);
			break;
		case R.id.btCapture:
			layoutResult.removeAllViews();
			intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);    
			mUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);  // create a file to save the video in specific folder (this works for video only)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
			startActivityForResult(intent, captureCode);
			break;
		case R.id.btDetect:
			String photoPath = getRealPathFromURI(mUri);
			photoMat= Highgui.imread(photoPath);
			Mat mGray = new Mat();
			Imgproc.cvtColor(photoMat, mGray, Imgproc.COLOR_BGR2GRAY, 3);
			loadCascadeFile(2);
			Detect(mGray);
			photoPath = "";
			break;
		default:
			break;
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == pickCode && data!=null){
			mUri = data.getData();
			ivDisplay.setImageURI(mUri);
			btDetect.setVisibility(View.VISIBLE);
			
		}
		if(requestCode == captureCode && resultCode == RESULT_OK){
			ivDisplay.setImageURI(mUri);
			btDetect.setVisibility(View.VISIBLE);
		}
	}
	
}
