package com.sivaram.fishograph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SignUpForm extends ActionBarActivity {
	Boolean fb;
	String email,pass,dob="0000-00-00",gender="not specified",fname="not specified",lname="not specified",imgPath,fileName,dp;
	EditText ethandle,etpass;
	TextView labelhandle,labelpass;
	Button create;
	int GALLERY=5;
	ImageView avatar;
	ProgressDialog pd;
	Bitmap scaled_and_compressed;
	SharedPreferences spf;
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_signupform);
		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
		getSupportActionBar().setTitle("Create");
		spf = getSharedPreferences("prefs",Context.MODE_PRIVATE);
		avatar = (ImageView) findViewById(R.id.setdp);
		Intent i = getIntent();
		fb = i.getBooleanExtra("fb", false);
		if(fb == true)
		{
		 email = i.getExtras().getString("email");
		 pass =	i.getExtras().getString("pass");
		 dob =	i.getExtras().getString("dob");
		 gender = i.getExtras().getString("gender");
		 fname = i.getExtras().getString("fname");
		 lname = i.getExtras().getString("lname");
		}
		else
		{
		 email = i.getExtras().getString("email");
	      	i.putExtra("fb", false);
	      	i.putExtra("signup",true);
		}
		
		pd = new ProgressDialog(this, R.style.MyTheme);
		pd.setCancelable(false);
		pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
		LayoutParams lp = avatar.getLayoutParams();
		Display d = getWindowManager().getDefaultDisplay();
		lp.width = d.getWidth();
		lp.height = d.getWidth();
		avatar.requestLayout();
		setupLayout();
		
	}
	
	void setupLayout()
	{
		etpass = (EditText) findViewById(R.id.etpass);
		labelpass = (TextView) findViewById(R.id.etlabelpass);
		ethandle = (EditText) findViewById(R.id.ethandle);
		labelhandle = (TextView) findViewById(R.id.etlabelhandle);
		create = (Button) findViewById(R.id.create);
		CheckBox cb = (CheckBox) findViewById(R.id.show);
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		      if(isChecked==true)
		    	  { 
		    	   etpass.setInputType(InputType.TYPE_CLASS_TEXT);
		    	  }
		      else
		    	  {
		    	   etpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		    	  }
		       etpass.setSelection(etpass.getText().toString().length());
			}
		});
		etpass.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			  labelpass.setVisibility(View.VISIBLE);	
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			  labelpass.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(etpass.getText().toString().equals(""))
					labelpass.setVisibility(View.VISIBLE);
				else
					labelpass.setVisibility(View.INVISIBLE);
			}
		});
	
	
	ethandle.addTextChangedListener(new TextWatcher(){
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		  labelhandle.setVisibility(View.VISIBLE);	
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		  labelhandle.setVisibility(View.INVISIBLE);
		}
		@Override
		public void afterTextChanged(Editable s) {
			if(ethandle.getText().toString().equals(""))
				labelhandle.setVisibility(View.VISIBLE);
			else
				labelhandle.setVisibility(View.INVISIBLE);
		}
	});
	avatar.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent galleryIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(galleryIntent, GALLERY);				
		}
	});
	
	create.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			pd.show();
			handleOkay();
		}
	});
 }
	void handleOkay()
	{
	  RequestParams rp = new RequestParams();
	  final String handle = ethandle.getText().toString();
	  rp.add("handle", handle);
	  new AsyncHttpClient().post("http://fishograph.com/scripts/checkhandle.php", rp, new AsyncHttpResponseHandler(){
		  @Override
		  public void onSuccess(String response)
		  {
			response = response.trim();
			if(response.equals("null"))
			{ 
			  addAvatar();
			}
			else if(!response.equals("NV"))
			{
			  pd.dismiss();
			  Toast.makeText(getApplicationContext(), "Oops! Handle already taken", Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
		  }
		  @Override
		  public void onFailure(int code,Throwable t,String err)
		  { 
			pd.dismiss();
		    Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();   
		  }
	  });
	}
	void addAvatar()
	{    
		avatar.setDrawingCacheEnabled(true);
		avatar.buildDrawingCache(true);
		Bitmap pic = Bitmap.createBitmap(avatar.getDrawingCache());
		if(pic!=null)
		{ 
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			pic.compress(Bitmap.CompressFormat.JPEG, 80, stream); 
			byte[] byte_arr = stream.toByteArray();
		
			String encodedString = Base64.encodeToString(byte_arr, 0);
			RequestParams rp = new RequestParams();
			String file = ethandle.getText().toString() + "_";
			rp.add("image", encodedString);
			rp.add("filename",file);
			new AsyncHttpClient().post("http://fishograph.com/scripts/upload.php", rp, new AsyncHttpResponseHandler(){
			@Override
			public void onFailure(int code,Throwable t,String err)
			{ 
			  pd.dismiss();
			  Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			}
		    @Override
		    public void onSuccess(String response)
		    {   
		    	
		    	response = response.trim();
		    	if(response.equals("null"))
		          { 
		    		pd.dismiss();
		    		Toast.makeText(getApplicationContext(), "Coudn't upload Dp..Please try again", Toast.LENGTH_SHORT).show();
		          }
		    	else
		    	{ dp = response;
		    	  addProfile();
		    	}
		    }
		 });	
	   }
		else
			Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
	}
	
	void addProfile()
	{  
	  RequestParams rp = new RequestParams();
	  rp.add("handle", ethandle.getText().toString());
	  rp.add("email", email);
	  rp.add("password", etpass.getText().toString());
	  rp.add("fname",fname);
	  rp.add("lname",lname);
	  rp.add("bio","not specified");
	  rp.add("dob",dob);
	  rp.add("gender",gender);
	  rp.add("location","not specified");
	  rp.add("dp",dp);
	  new AsyncHttpClient().post("http://fishograph.com/scripts/addprofile.php",rp, new AsyncHttpResponseHandler(){
		  @Override
		  public void onFailure(int code,Throwable t,String err)
		  {
			pd.dismiss();  
			Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();  
		  }
		  @Override
		  public void onSuccess(String response)
		  {
			response = response.trim();
			if(response.equals("null"))
			{   
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			}
			else
				addUser();
		  }
	  });
	}
	
	void addUser()
	{
		RequestParams rp = new RequestParams();
		final String handle = ethandle.getText().toString();
		rp.add("handle", handle);
		rp.add("email", email);
		rp.add("password", etpass.getText().toString());
		new AsyncHttpClient().post("http://fishograph.com/scripts/adduser.php",rp,new AsyncHttpResponseHandler(){
			@Override
			public void onFailure(int code,Throwable t,String err)
			{   
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onSuccess(String response)
			{
			 pd.dismiss();
			 response = response.trim();
			 if(response.equals("null"))
			    Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			 else
			 {
				 Editor edit = spf.edit();
				 edit.putBoolean("credAvail", true);
				 edit.putString("handle", handle);
				 edit.commit();
				 Intent i = new Intent(SignUpForm.this,Home.class);
				 startActivity(i);
				 finish();
			 }
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == GALLERY && resultCode == RESULT_OK
					&& null != data) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				// Get the cursor
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				// Move to first row
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				imgPath = cursor.getString(columnIndex);
				cursor.close();
				scaled_and_compressed = compressImage(imgPath);
				// Set the Image in ImageButton
				avatar.setImageBitmap(scaled_and_compressed);
				// Get the Image's file name
				String fileNameSegments[] = imgPath.split("/");
				fileName = "dp"+fileNameSegments[fileNameSegments.length - 1]+""+System.currentTimeMillis();
				// Put file name in Async Http Post Param which will used in Php web app
				
			} else {
				Toast.makeText(this, "You haven't picked Image",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}
	}
	
	
	
	public Bitmap compressImage(String imageUri) {
		 
        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;
 
        BitmapFactory.Options options = new BitmapFactory.Options();
 
//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
 
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
 
//      max Height and width values of the compressed image is taken as 816x612
 
        float maxHeight = 1024.0f;
        float maxWidth = 1024.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;
 
//      width and height values are set maintaining the aspect ratio of the image
 
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
 
            }
        }
 
//      setting inSampleSize value allows to load a scaled down version of the original image
 
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
 
//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;
 
//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
 
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
 
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
 
        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;
 
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
 
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
 
//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
 
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        return scaledBitmap;
 
    }
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    if (height > reqHeight || width > reqWidth) {
	        final int heightRatio = Math.round((float) height/ (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      
	      }       
	    final float totalPixels = width * height;       
	    final float totalReqPixelsCap = reqWidth * reqHeight * 2;       
	    while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
	        inSampleSize++;
	    }
	    return inSampleSize;
	}
	
	private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

}
