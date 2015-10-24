package com.sivaram.fishograph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class Capture extends Activity {
	
	String capturedPhotoPath,handle;
	CropImageView cropimg;
	Button rotate,done,crop,cancel;
	Bitmap finalImage;
	String caption;
	ProgressDialog  pd;
	Dialog d;
	@Override
	protected void onCreate(Bundle b)
	{
		super.onCreate(b);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cropcaptured);
	    handle = getIntent().getExtras().getString("handle");
	    capturedPhotoPath = getIntent().getExtras().getString("path");
	    rotate = (Button) findViewById(R.id.rotate);
	    done = (Button) findViewById(R.id.done);
	    crop = (Button) findViewById(R.id.crop);
	    cancel = (Button) findViewById(R.id.cancel);
	    cropimg = (CropImageView) findViewById(R.id.captured);
	    
	    File sd = Environment.getExternalStorageDirectory();
	    File image =  new File(capturedPhotoPath);
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inPurgeable = true;
	    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        cropimg.setImageBitmap(bitmap);
        cropimg.setAspectRatio(1,1);
        cropimg.setFixedAspectRatio(true);
	    rotate.setOnClickListener(new View.OnClickListener() {
        
	    @Override
		public void onClick(View v) {
		   cropimg.rotateImage(90);    	
		 }
	  }); 		   
     done.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		 Bitmap bm = cropimg.getCroppedImage();
		 cropimg.setImageBitmap(bm);
		 
		 done.setVisibility(View.INVISIBLE);
		 crop.setVisibility(View.INVISIBLE);
		 rotate.setVisibility(View.INVISIBLE);
		 cancel.setVisibility(View.INVISIBLE);
		 d = new Dialog(Capture.this,R.style.Dialog);
		 d.setCancelable(false);
		 View vu = getLayoutInflater().inflate(R.layout.captiondialog, null);
		 d.setContentView(vu);
		 WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		 Window w = d.getWindow();
		 lp.copyFrom(w.getAttributes());
		 lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		 lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
         w.setAttributes(lp);	
         Button post = (Button) vu.findViewById(R.id.makepost);
         final EditText et = (EditText) vu.findViewById(R.id.typecaption);
         final TextView label = (TextView) vu.findViewById(R.id.label);
         et.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				label.setVisibility(View.VISIBLE);
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
                label.setVisibility(View.INVISIBLE);		    	
			}
			@Override
			public void afterTextChanged(Editable s) {
			    if(et.getText().toString().equals(""))
			    	label.setVisibility(View.VISIBLE);
			    else
			    	label.setVisibility(View.INVISIBLE);
			}
         });
         pd = new ProgressDialog(Capture.this);
         pd.setCancelable(false);
         post.setOnClickListener(new View.OnClickListener() {
        	 	@Override
				public void onClick(View v) {
        	 		  pd.show();
        	 		  finalImage = cropimg.getCroppedImage();
        	 		  caption = et.getText().toString();
        	 		  d.dismiss();
        	 		  uploadPic();
					}
		      });
         
		 d.show();
		 
		}
	 });
     crop.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		  Bitmap bm = cropimg.getCroppedImage();
		  cropimg.setImageBitmap(bm);			
		}
	});
     cancel.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
		}
	});
  }

	@Override
	  public void onActivityResult(int requestCode,int resultCode,Intent data)
	  {
	   //if(requestCode == 1 && resultCode==RESULT_OK)
	   {
		    
	   }
	  }

	 void uploadPic()
	 {
		 ByteArrayOutputStream stream = new ByteArrayOutputStream();
			// Must compress the Image to reduce image size to make upload easy
			finalImage.compress(Bitmap.CompressFormat.JPEG, 50, stream); 
			byte[] byte_arr = stream.toByteArray();
			// Encode Image to String
			String imgstring = Base64.encodeToString(byte_arr, 0);
			RequestParams rp = new RequestParams();
			rp.add("filename", handle);
			rp.add("image", imgstring);
			new AsyncHttpClient().post("http://fishograph.com/scripts/upload.php",rp, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(String response)
				{
				  //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
				 if(response.contains("imgs"))
				  uploadRest(response.trim());	
				}
			});
	 }
	 
	 void uploadRest(String url)
	 {
       RequestParams rp = new RequestParams();
       rp.add("handle",handle);
       rp.add("url",url);
       rp.add("caption",caption);
       
       String tags = new String();
       Pattern MY_PATTERN = Pattern.compile("@(\\w+|\\W+)");
       Matcher mat = MY_PATTERN.matcher(caption);
       while (mat.find()) {
         //System.out.println(mat.group(1));
    	   tags = mat.group(1) + ";";
    	   Log.d("tags", tags);
       }
       if(tags.isEmpty())
       rp.add("tags","null");
       else
    	   rp.add("tags", tags);
       String hashtags = new String();
       Pattern MY_PATTERN2 = Pattern.compile("#(\\w+|\\W+)");
       Matcher mat2 = MY_PATTERN2.matcher(caption);
       while (mat2.find()) {
         //System.out.println(mat.group(1));
    	   hashtags = mat2.group(1) + ";";
    	   Log.d("hashtags", hashtags);
       }
       
       if(hashtags.isEmpty())
    	   rp.add("hashtags", "null");
       else
    	   rp.add("hashtags", hashtags);
       
       Log.d("uploadrest",handle+";"+url+";"+caption);
       new AsyncHttpClient().post("http://fishograph.com/scripts/uploadpost.php", rp, new AsyncHttpResponseHandler()
       {
    	 @Override
    	 public void onSuccess(String response)
    	 {
    		 pd.dismiss();
    		 Log.d("post upload",response);
    		 finish();
    	 }
       });
       
	 }
}
