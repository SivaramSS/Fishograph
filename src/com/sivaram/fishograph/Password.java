package com.sivaram.fishograph;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

public class Password extends ActionBarActivity{
    int width,height;
    TextView labelpass,tvhandle,tvfollowers;
    EditText etpass;
    String handle,password,name,dppath,catches,followers,following;
    CircularImageView realdp;
    SharedPreferences spf;
	@Override
	protected void onCreate(Bundle b)
	{
	 super.onCreate(b);
	 setContentView(R.layout.activity_password);
	 getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
	 getSupportActionBar().setTitle("Login As");
	 spf = getSharedPreferences("prefs",Context.MODE_PRIVATE);
	 
	 Intent i = getIntent();
	 handle = i.getExtras().getString("handle");
	 password = i.getExtras().getString("password");
	 dppath = i.getExtras().getString("dppath");
	 followers = i.getExtras().getString("followers");
	 catches = i.getExtras().getString("catches");
	 following = i.getExtras().getString("following");
	 
	 realdp = (CircularImageView) findViewById(R.id.realdp);
	 etpass = (EditText) findViewById(R.id.etpass);
	 labelpass = (TextView) findViewById(R.id.etlabelpass);
	 tvhandle = (TextView) findViewById(R.id.infohandle);
	 tvfollowers = (TextView) findViewById(R.id.infofollowers);
	 
	 Display d = getWindowManager().getDefaultDisplay();
     width = d.getWidth();
     height = d.getHeight();
     LayoutParams lp = realdp.getLayoutParams();
     lp.width = width / 2;
     lp.height = width / 2;
     realdp.requestLayout();
     realdp.setBorderColor(getResources().getColor(R.color.transparent));
     etpass.addTextChangedListener(new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			labelpass.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
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
     
     tvhandle.setTypeface(null,Typeface.BOLD);
	 tvhandle.setText("@"+handle);
     SpannableString ssfollowers = new SpannableString(followers+"\nfollowers");
	 ssfollowers.setSpan(new RelativeSizeSpan(2f), 0, followers.length(), 0);
	 tvfollowers.setText(ssfollowers);
     Glide.with(getApplicationContext()).load("http://fishograph.com/scripts/"+dppath).into(realdp);
     
     ImageButton login = (ImageButton) findViewById(R.id.login);
     login.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
		  if(etpass.getText().toString().equals(password))
		  {
			Editor edit = spf.edit();
			edit.putBoolean("credAvail", true);
			edit.putString("handle", handle);
			edit.commit();
			Intent i = new Intent(Password.this,Home.class);
			startActivity(i);
		  }
		}
	});
  }
	
}
