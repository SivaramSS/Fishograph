package com.sivaram.fishograph;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class EditProfile extends FragmentActivity {
	EditText etfname,etlname,ethandle,etbio,etdob,etgender,etlocation;
	TextView fnamelabel,lnamelabel,handlelabel,biolabel,doblabel,genderlabel,locationlabel;
	SharedPreferences spf ;
	String handle;
	Boolean changed = false;
	ProgressDialog pd;
	@Override
	protected void onCreate(Bundle b)
	{
		super.onCreate(b);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.profedit);
		spf = getSharedPreferences("prefs",Context.MODE_PRIVATE);
		Intent i = getIntent();
		handle = i.getExtras().getString("handle");
		etfname = (EditText) findViewById(R.id.firstname);
	    etlname = (EditText) findViewById(R.id.lastname);
	    ethandle = (EditText) findViewById(R.id.handle);
	    etbio = (EditText) findViewById(R.id.bio);
	    etdob = (EditText) findViewById(R.id.dob);
	    etgender = (EditText) findViewById(R.id.gender);
	    etlocation = (EditText) findViewById(R.id.location);
	    fnamelabel = (TextView) findViewById(R.id.fnamelabel);
		lnamelabel = (TextView) findViewById(R.id.lnamelabel);
		handlelabel = (TextView) findViewById(R.id.handlelabel);
		biolabel = (TextView) findViewById(R.id.biolabel);
		doblabel = (TextView) findViewById(R.id.doblabel);
		genderlabel = (TextView) findViewById(R.id.genderlabel);
		locationlabel = (TextView) findViewById(R.id.locationlabel);
	    
	    etdob.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
			 int y = Calendar.getInstance().get(Calendar.YEAR);
			 int m = Calendar.getInstance().get(Calendar.MONTH);
			 int d = Calendar.getInstance().get(Calendar.DATE);
			 new DatePickerDialog(EditProfile.this, new DatePickerDialog.OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int month,
						int day) {
					
					etdob.setText(year+"-"+(month+1)+"-"+day);
				}
			}, y, m+1, d).show();
		 }
		});
	    setWatchers();
	    RequestParams rp = new RequestParams();
	    rp.add("handle",handle);
	    pd = new ProgressDialog(this,R.style.MyTheme);
	    pd.setCancelable(false);
	    pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
	    pd.show();
	    
	    //To get the user profile first
	    new AsyncHttpClient().post("http://fishograph.com/scripts/getprofile.php", rp, new AsyncHttpResponseHandler(){
	    	
	    	//fname ; lname ; bio ; dob ; gender ; location ;
	    	@Override
	    	public void onSuccess(String response)
	    	{   
	    		pd.dismiss();
	    		response = response.trim();
	    		Log.d("response", response);
	    		if(!response.equals("null"))
	    		{
	    			final String[] info = response.split(";");
	    			if(info[0].equals("not specified"))
	    			    etfname.setText("");
	    			else
	    				etfname.setText(info[0]);
	    			if(!info[1].contains("not specified"))
	    			    etlname.setText(info[1]);
	    			else
	    				etlname.setText("");
	    			if(!info[2].contains("not specified"))
	    			    etbio.setText(info[2]);
	    			else
	    				etbio.setText("");
	    			if(info[3].contains("0000-00-00"))
	    				etdob.setText("1969-02-01");
	    			else
	    				etdob.setText(info[3]);
	    			if(!info[4].contains("not specified"))
	    			    etgender.setText(info[4]);
	    			else
	    				etgender.setText("");
	    			if(!info[5].contains("not specified"))
	    			    etlocation.setText(info[5]);
	    			else
	    				etlocation.setText("");
	    			
	    			ethandle.setText(handle);
	    		 
	    	  }	
               ((Button) findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
	                    Intent i = new Intent(EditProfile.this,Home.class);
	                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						EditProfile.this.finish();	
					}
				});
	    		
	    		((Button) findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(changed==true)
				        save(); 
					}
				});
	    	}
	    	
	    	@Override
	    	public void onFailure(int errorCode,Throwable error,String content)
	    	{ 
	    	  pd.dismiss();
	    	  Toast.makeText(getApplicationContext(), "Something went wrong.. please try again", Toast.LENGTH_SHORT).show();	
	    	  EditProfile.this.finish();
	    	}
	    }); 
	
	}
	
	
	public void save()
	{    
		pd.show();
		RequestParams rp = new RequestParams();
        rp.add("handle", handle);
        rp.add("fname", etfname.getText().toString());
        rp.add("lname", etlname.getText().toString());
        rp.add("bio",etbio.getText().toString());
        rp.add("dob",etdob.getText().toString());
        rp.add("gender",etgender.getText().toString());
        rp.add("location",etlocation.getText().toString());
        final String hc = ethandle.getText().toString();
        rp.add("hc", hc);
        
		 new AsyncHttpClient().post("http://fishograph.com/scripts/saveprof.php", rp, new AsyncHttpResponseHandler(){
			  @Override
			  public void onSuccess(String response)
			  { pd.dismiss();
				response = response.trim();  
			    if(response.equals("1"))
				 {
			    	Toast.makeText(getApplicationContext(), "Changes made", Toast.LENGTH_SHORT).show();
			    	Editor edit = spf.edit();
			    	edit.putString("handle", hc);
			    	edit.putBoolean("credAvail", true);
			    	edit.commit();
			    	Intent i = new Intent(EditProfile.this,Home.class);
			    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    	startActivity(i);
				 }
			    else
			     Toast.makeText(getApplicationContext(), "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();	
			    EditProfile.this.finish();
			  }
			  @Override
			  public void onFailure(int error,Throwable errors, String content)
			  {
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "Error on saving you profile changes. Please try again", Toast.LENGTH_SHORT).show();  
			  }
		 });
	}
	
	void setWatchers()
	{   
		etfname.addTextChangedListener(new TextWatcher(){
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		       fnamelabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				fnamelabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
			    changed = true;
				if(etfname.getText().equals(""))
					fnamelabel.setVisibility(View.VISIBLE);
				else
					fnamelabel.setVisibility(View.INVISIBLE);
			}
		});
		
		etlname.addTextChangedListener(new TextWatcher(){
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		       lnamelabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				lnamelabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(etlname.getText().equals(""))
					lnamelabel.setVisibility(View.VISIBLE);
				else
					lnamelabel.setVisibility(View.INVISIBLE);
			}
		});
		
		ethandle.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		     handlelabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				handlelabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(ethandle.getText().equals(""))
					handlelabel.setVisibility(View.VISIBLE);
				else
					handlelabel.setVisibility(View.INVISIBLE);
			}
		});
		
		etbio.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		     biolabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				biolabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(etbio.getText().equals(""))
					biolabel.setVisibility(View.VISIBLE);
				else
					biolabel.setVisibility(View.INVISIBLE);
			}
		});
		
		etdob.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		     doblabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				doblabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(etdob.getText().equals(""))
					doblabel.setVisibility(View.VISIBLE);
				else
					doblabel.setVisibility(View.INVISIBLE);
			}
		});
		
		etgender.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		     genderlabel.setVisibility(View.VISIBLE);		 
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				genderlabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(etgender.getText().equals(""))
					genderlabel.setVisibility(View.VISIBLE);
				else
					genderlabel.setVisibility(View.INVISIBLE);
			}
		});
		
		etlocation.addTextChangedListener(new TextWatcher(){
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
		     locationlabel.setVisibility(View.VISIBLE);		 
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				locationlabel.setVisibility(View.INVISIBLE);
			}
			@Override
			public void afterTextChanged(Editable s) {
				changed = true;
				if(etlocation.getText().equals(""))
					locationlabel.setVisibility(View.VISIBLE);
				else
					locationlabel.setVisibility(View.INVISIBLE);
			}
		});
		
	}
}
