package com.sivaram.fishograph;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class LoginNew extends Activity {
    int width,height;
    Button login,signup;
    TextView header;
    Button fblogin,next;
    FragmentManager fm;
    CallbackManager callbackManager;
    EditText email,pass;
    TextView labelem, labelps;
    Boolean logginIn=false;
    String handle,hpresponse;
    SharedPreferences spf;
    LoginButton lb;
    ProgressDialog pd;
    String gender,fname,lname,dob,em,ps,fbuserid,fbdp;
	@Override
	protected void onCreate(Bundle b)
	{
		super.onCreate(b);
		FacebookSdk.sdkInitialize(getApplicationContext());
    	callbackManager = CallbackManager.Factory.create();
		setContentView(R.layout.activity_loginnew);
		spf = getSharedPreferences("prefs", Context.MODE_PRIVATE);
		setUpLayout();
		email = (EditText) findViewById(R.id.etemail);
		final TextView emaillabel = (TextView) findViewById(R.id.etlabelemail);
		lb = (LoginButton) findViewById(R.id.fbsignup);
		email.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
                 emaillabel.setVisibility(View.VISIBLE);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
                 emaillabel.setVisibility(View.INVISIBLE);				
			}

			@Override
			public void afterTextChanged(Editable s) {

				if(email.getText().toString().equals(""))
					emaillabel.setVisibility(View.VISIBLE);
				else
					emaillabel.setVisibility(View.INVISIBLE);
			}
			
		});
		
		ImageButton ib = (ImageButton) findViewById(R.id.joinbtn);
		ib.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 
				 if(isValid(email.getText().toString()))
				 { 
			      logginIn = false;
				  emailExists(email.getText().toString());
				 }
				 else
					 Toast.makeText(getApplicationContext(), "Incorrect Format", Toast.LENGTH_SHORT).show();
			}
		});
		
		lb.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
		fbSetUp();
	}
	
	void emailExists(String s)
	{
	  RequestParams rp = new RequestParams();
	  rp.add("email",email.getText().toString());
	  pd = new ProgressDialog(this,R.style.MyTheme);
	  pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
	  pd.setCancelable(false);
	  pd.show();
	  new AsyncHttpClient().post("http://fishograph.com/scripts/checkuser.php",rp, new AsyncHttpResponseHandler(){
		  @Override
		public void onSuccess(String response)
		  {  
			 response = response.trim();
			 if(response.equals("null"))
			 {   
				 pd.dismiss();
				 Intent i = new Intent(LoginNew.this,SignUpForm.class);
			      	i.putExtra("email", email.getText().toString());
			      	i.putExtra("fb", false);
			      	i.putExtra("signup",true);
			      	startActivity(i);
			      	finish();
			 }
			 else
			 {   
				 hpresponse = response;
				 getBasicProfile();
			 }
		  }
		  @Override
		  public void onFailure(int code,Throwable t,String err)
		  {
			pd.dismiss();
		   	Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();  
		  }
	  }); 
	}
	
	void fbSetUp()
	{
		 lb.registerCallback(callbackManager,
	                new FacebookCallback<LoginResult>() {
	                    @Override
	                    public void onSuccess(LoginResult loginResult) {
	                        Log.d("Success", "Login");
	                        final AccessToken at = loginResult.getAccessToken();
	                        GraphRequest request = GraphRequest.newMeRequest(
						            at,
						            new GraphRequest.GraphJSONObjectCallback() {
						                @Override
						                public void onCompleted(
						                        JSONObject object,
						                        GraphResponse response) {
						                    // Application code
						                    Log.d("response","response"+object.toString());
    										ps = "NA";
    										try {
    											em = object.getString("email");
												gender = object.getString("gender");
												fname = object.getString("first_name");
	    										lname = object.getString("last_name");
	    										dob = object.getString("birthday");
	    										fbuserid = object.getString("id");
	    										JSONObject picture = object.getJSONObject("picture");
	    										JSONObject data = picture.getJSONObject("data");
	    										fbdp = data.getString("url");
	    										fbEmailExists();
											} catch (JSONException e) {
												e.printStackTrace();
											}
    										
						                    
						                }
						            });
						    Bundle parameters = new Bundle();
						    parameters.putString("fields", "id,first_name,last_name,email,gender, birthday,picture.width(512)");
						    request.setParameters(parameters);
						    request.executeAsync();
	                    }

	                    @Override
	                    public void onCancel() {
	                        //Toast.makeText(Login.this, "Login Cancel", Toast.LENGTH_LONG).show();
	                    }

	                    @Override
	                    public void onError(FacebookException exception) {
	                        //Toast.makeText(Login.this, exception.getMessage(), Toast.LENGTH_LONG).show();
	                    }
	                });

   }
	
	void setUpLayout()
	{
		Display d = getWindowManager().getDefaultDisplay();
		width = d.getWidth();
		height = d.getHeight();
		ImageView logo = (ImageView) findViewById(R.id.logo);
		LayoutParams lp = (LayoutParams) logo.getLayoutParams();
		lp.width = width/2;
		lp.height = width/2;
		logo.setImageResource(R.drawable.withcamwithborder);
		TextView tv = (TextView) findViewById(R.id.title);
		Typeface tf = Typeface.createFromAsset(getAssets(),"berlin.ttf");
		tv.setTypeface(tf);
		Spannable sp = new SpannableString("FISHOGRAPH");
		sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		//sp = new SpannableString("FISHO");
		//Spannable sp2 = new SpannableString("Graph");
		
		sp.setSpan(new ForegroundColorSpan(Color.RED), 5, 10, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		//sp2 = new SpannableString("GRAPH");
		tv.setText(sp);
	}
	
	 @Override 
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);

	        callbackManager.onActivityResult(requestCode, resultCode, data); 
	    }
	@Override
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			LoginNew.this.finish();
		}
    	
    	return true;
    	
    }
	@Override
    protected void onResume() {
      super.onResume();

      // Logs 'install' and 'app activate' App Events.
     //AppEventsLogger.activateApp(this);
    }
    
    @Override
    protected void onPause() {
      super.onPause();

      // Logs 'app deactivate' App Event.
      //AppEventsLogger.deactivateApp(this);
      //finish();
    }
    
    void getBasicProfile()
	{
	  final String[] info2 = hpresponse.split(";");
	  handle = info2[0];
	  RequestParams rp = new RequestParams();
	  rp.put("handle", handle);
	  Log.d("sign in", hpresponse);
	  new AsyncHttpClient().post("http://fishograph.com/scripts/basicprofile.php", rp, new AsyncHttpResponseHandler(){
			 
	   //0 name, 1 dppath, 2 catches, 3 followers, 4 following
		  @Override
		  public void onSuccess(String response) 
		  {
			  pd.dismiss();	 
			  response = response.trim();
			  Log.d("sign in response", response);
			  if(!response.equals("null"))
				{ 
				  if(!info2[1].equals("NA"))
				  {
					  String[] info = response.split(";");
					  info[0] = info[0].trim();
					  info[1] = info[1].trim();
					  info[2]=info[2].trim();
					  info[3]=info[3].trim();
					  info[4]=info[4].trim();
					  Intent i = new Intent(LoginNew.this,Password.class);
					  i.putExtra("handle", info2[0]);
					  i.putExtra("password", info2[1]);
					  i.putExtra("name", info[0]);
					  Log.d("dppath",info[1]);
					  i.putExtra("dppath", info[1]);
					  i.putExtra("catches", info[2]);
					  i.putExtra("followers", info[3]);
					  i.putExtra("following", info[4]);
					  i.putExtra("fb", false);
					  i.putExtra("signup", false);
					  startActivity(i);
				  }
				  else
				  {
				    Toast.makeText(getApplicationContext(), "Please Login with Facebook", Toast.LENGTH_SHORT).show();  
				  }
				 } 
			     else
			    	 Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			   }
				@Override
				public void onFailure(int statusCode, Throwable error,
						String content) {
					pd.dismiss();
					if (statusCode == 404) {
						Toast.makeText(getApplicationContext(),"Requested resource not found",Toast.LENGTH_LONG).show();
					}
					else if (statusCode == 500) {
						Toast.makeText(getApplicationContext(),"Something went wrong at server end",Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(
								getApplicationContext(),"Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "+ statusCode, Toast.LENGTH_LONG).show();
					}
				}
			}); 
	}
    
    void fbEmailExists()
    {
    	final ProgressDialog pd = new ProgressDialog(LoginNew.this,R.style.MyTheme);
		pd.setCancelable(false);
		pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
		pd.show();
		AsyncHttpClient ahc = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("email", em);
		ahc.post("http://fishograph.com/scripts/checkuser.php",params,new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String response) {
				// Hide Progress Dialog
				pd.dismiss();
				String[] info = response.toString().split(";");
				if(info[0].equals("null"))
					 { 
						signupFb();
					 }
					 else
					 {  
					   Intent i = new Intent(LoginNew.this, Home.class); 
					   Editor edit = spf.edit();
					   edit.putBoolean("credAvail", true);
					   edit.putString("handle",info[0]);
					   edit.commit();
					   i.putExtra("handle", info[0]);
					   i.putExtra("signup",false);
					   startActivity(i);
					   LoginManager.getInstance().logOut();
					   LoginNew.this.finish();
					 }
			}

			@Override
			public void onFailure(int statusCode, Throwable error,
					String content) {
				pd.dismiss();
				Toast.makeText(getApplicationContext(),"Something went wrong.. Please try again",Toast.LENGTH_SHORT).show();
			
			}
		 } //end of AsyncHttpResponseHandler 	
		);

    }
	
    boolean isValid(String s)
    {
      if(s.isEmpty())
    	  return false;
      else
    	  return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();
    }
    
    void signupFb()
    {
      RequestParams rp = new RequestParams();
      rp.add("fname",fname);
  	  rp.add("lname",lname);
  	  fname = fname.replace(" ", "_");
      lname = lname.replace(" ", "_");
      final String handle = fname+"_"+lname;
      rp.add("handle",handle);
  	  rp.add("email", em);
  	  rp.add("password", ps);
  	  rp.add("bio","");
  	  rp.add("dob",dob);
  	  rp.add("gender",gender);
  	  rp.add("location","");
  	  rp.add("dp",fbdp);
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
  			{
  				addUser(handle);
  			}
  		  }
  	  });
  	
    }
    
    void addUser(String h)
	{
		RequestParams rp = new RequestParams();
		rp.add("handle", h);
		rp.add("email", em);
		rp.add("password",ps);
		final String han = h;
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
			 response = response.trim();
			 if(response.equals("null"))
			    Toast.makeText(getApplicationContext(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			 else
			 {   
				 Editor edit = spf.edit();
				 edit.putBoolean("credAvail", true);
				 edit.putString("handle", han);
				 edit.commit();
				 Intent i = new Intent(LoginNew.this,Home.class);
				 startActivity(i);
				 LoginManager.getInstance().logOut();
				 finish();
			 }
			}
		});
	}
	

}
