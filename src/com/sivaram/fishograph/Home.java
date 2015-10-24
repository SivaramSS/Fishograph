package com.sivaram.fishograph;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Home extends ActionBarActivity {
   SharedPreferences spf;
   String handle,email,pass;	
   ImageButton ibhome,ibsearch,ibpost,ibactivity,ibprofile;
   FragmentManager fm;
   String capturedPhotoPath;
   File image;
   
   @Override
	public void onCreate(Bundle b)
	{
	  super.onCreate(b);
	  setContentView(R.layout.home);
	  SharedPreferences spf = getSharedPreferences("prefs",Context.MODE_PRIVATE);
	  if(spf.getBoolean("credAvail", false)==false)
	  {
	    startActivity(new Intent(Home.this,LoginNew.class));
	    finish();
	  }
	  else
	  {
      getSupportActionBar().hide();
      handle = spf.getString("handle", null);
	  /*email = spf.getString("email", null);
	  pass = spf.getString("pass", null);
	  */
	  ibhome = (ImageButton) findViewById(R.id.home);
	  ibsearch = (ImageButton) findViewById(R.id.search);
	  ibpost = (ImageButton) findViewById(R.id.post);
	  ibactivity = (ImageButton) findViewById(R.id.activity);
	  ibprofile = (ImageButton) findViewById(R.id.profile);
	  fm = getSupportFragmentManager();
	  ibpost.setOnClickListener(new View.OnClickListener() {
	  
		@Override
		public void onClick(View v) {

		  final Dialog d = new Dialog(Home.this, R.style.Dialog);
		  d.setCancelable(true);
		  View vu = getLayoutInflater().inflate(R.layout.chooser, null);
		  d.setContentView(vu);
		  WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		  Window w = d.getWindow();
		  lp.copyFrom(w.getAttributes());
		  lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		  lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		  w.setAttributes(lp);
		  d.show();
		  
		  Button camera = (Button) vu.findViewById(R.id.camera);
		  camera.setOnClickListener(new View.OnClickListener() {
			
			  					@Override
			  					public void onClick(View v) {
			  						d.dismiss();
			  						Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			  					   if(camera.resolveActivity(getPackageManager())!=null)
			  					   {
			  						 File photo = null;
			  						 try
			  						 {
			  					 		photo = createImageFile();
			  				 		 }
			  						 catch(Exception e)
			  						 {
			  					 		Toast.makeText(getApplicationContext(), "Error in creating file", Toast.LENGTH_SHORT).show();
			  				 		 }
			  						 if(photo!=null)
			  						 {
			  							camera.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photo)); 
			  							startActivityForResult(camera,1);
			  						 }
			  					   }				
			  					}
		             });
		  
			Button gallery = (Button) vu.findViewById(R.id.gallery);
			gallery.setOnClickListener(new View.OnClickListener() {
				
						@Override
						public void onClick(View v) {
							d.dismiss();
							 Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
							    getIntent.setType("image/*");
                                
							    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							    pickIntent.setType("image/*");

							    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
							    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

							    startActivityForResult(chooserIntent,2);
						}
					});
		  
		}
		
	});
	
	  
	  ibprofile.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		   	Fragment f = new Profile(handle,true,fm);
		   	fm.beginTransaction().replace(R.id.content, f).commit();
		}
	});
	  
	  ibhome.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    
			Fragment f = new Feed(handle);
		    fm.beginTransaction().replace(R.id.content, f).addToBackStack("feed").commit();
		    
		 }
	 });
	  
	  ibsearch.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Fragment f = new Search(handle);
			fm.beginTransaction().replace(R.id.content, f).addToBackStack("search").commit();
		}
	});
	    Fragment f = new Feed(handle);
	    fm.beginTransaction().replace(R.id.content, f).commit();
	}
 }

  @Override
  public void onActivityResult(int requestCode,int resultCode,Intent data)
  {
    if(requestCode==1 && resultCode==RESULT_OK)
    { galleryAddPic();
      Intent i = new Intent(Home.this,Capture.class);
      i.putExtra("path",image.getAbsolutePath());
      Toast.makeText(getApplicationContext(), image.getAbsolutePath(), Toast.LENGTH_SHORT).show();
      i.putExtra("handle", handle);
      startActivity(i);
    }
    else if(requestCode==2 && resultCode==RESULT_OK)
    {
    	Uri uri = data.getData();
    	Intent i = new Intent(Home.this,Capture.class);
    	String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        i.putExtra("path", picturePath);
        i.putExtra("handle", handle);
        startActivity(i);
    }
  }
   
  
  private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "Fishograph_" + handle+ "_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    capturedPhotoPath = "file:" + image.getAbsolutePath();
	    this.image = image;
	    return image;
	}

private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(capturedPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}

 
}
