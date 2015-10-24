package com.sivaram.fishograph;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;



public class Profile extends Fragment {
	TextView catches,followers, following,tvname,postsbtn,photosbtn,taggedbtn;
	ImageView dp;
	Bitmap dpbm;
	ProgressBar pg;
	Integer intfollowers=-1, intcatches=-1, intfollowing=-1;
	String name,dppath,han;
	Jsparser jp;
	Boolean ownprofile;
	ProgressDialog pd;
	ArrayList<eachpost> posts;
	FragmentManager fm;
	int width,height;
	public Profile()
	{
		super();
	}
	public Profile(String handle, Boolean own,FragmentManager fm)
	{
	  super();
	  jp = new Jsparser();
	  ownprofile = own;
	  han = handle;
	  getBasicProfile(handle);
	  getImage(handle);
	  posts = new ArrayList<eachpost>();
	}
    @Override
	public void onCreate(Bundle b)
    {
      super.onCreate(b);
      setHasOptionsMenu(true);
      
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b)
	{   
		View v = inflater.inflate(R.layout.profile, null);
		 ActionBar ab = ((ActionBarActivity) getActivity()).getSupportActionBar();
		 ab.setBackgroundDrawable(new ColorDrawable(Color.RED));
		 ab.show();
		 catches = (TextView) v.findViewById(R.id.catches);
		 followers = (TextView) v.findViewById(R.id.followers);
		 following = (TextView) v.findViewById(R.id.following);
		 tvname = (TextView) v.findViewById(R.id.tvname);
		 postsbtn = (TextView) v.findViewById(R.id.postsbtn);
		 photosbtn = (TextView) v.findViewById(R.id.photosbtn);
		 taggedbtn = (TextView) v.findViewById(R.id.taggedbtn);
		 dp = (ImageView) v.findViewById(R.id.dp);
		 pg = (ProgressBar) v.findViewById(R.id.progress);
		 fm = getChildFragmentManager();
		 pd = new ProgressDialog(getActivity());
		 Display d = getActivity().getWindowManager().getDefaultDisplay();
		 width = d.getWidth();
		 height = d.getHeight();
		 pd.setCancelable(false);
		 pd.setMessage("Loading profile");
		 pd.show();
	   postsbtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postsbtn.setBackgroundColor(0xffffffff);
				postsbtn.setTextColor(0xff7f0000);
				photosbtn.setBackgroundColor(0x00000000);
			  	photosbtn.setTextColor(0xffffffff);
			  	taggedbtn.setBackgroundColor(0x00000000);
			  	taggedbtn.setTextColor(0xffffffff);
			  	fm.beginTransaction().replace(R.id.profcontent, new UserPost(han,dpbm)).commit();
			}
		});
		 
	   photosbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
               photosbtn.setBackgroundColor(0xffffffff);
               photosbtn.setTextColor(0xff7f0000);
               postsbtn.setBackgroundColor(0x00000000);
               postsbtn.setTextColor(0xffffffff);
               taggedbtn.setBackgroundColor(0x00000000);
               taggedbtn.setTextColor(0xffffffff);
               fm.beginTransaction().replace(R.id.profcontent, new UserPhotos(han)).commit();
			}
		});
	   taggedbtn.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
             taggedbtn.setBackgroundColor(0xffffffff);
             taggedbtn.setTextColor(0xff7f0000);
             photosbtn.setBackgroundColor(0x00000000);
             photosbtn.setTextColor(0xffffffff);
             postsbtn.setBackgroundColor(0x00000000);
             postsbtn.setTextColor(0xffffffff);
             fm.beginTransaction().replace(R.id.profcontent, new TaggedPhotos(han)).commit();
		  }
	    });
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
	{  if(ownprofile==true)
		inflater.inflate(R.menu.profedit, menu);
	else
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId()==R.id.edit)
		{
			Intent i = new Intent(getActivity(),EditProfile.class);
			i.putExtra("handle", han);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			getActivity().finish();
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle b)
	{
		super.onActivityCreated(b);
		((ActionBarActivity) getActivity() ).getSupportActionBar().setTitle("@"+han);
	}
	
	public void getBasicProfile(String handle)
	{
	 RequestParams rp = new RequestParams();
	 rp.put("handle", handle);
	 AsyncHttpClient client = new AsyncHttpClient(); 
	 
	 client.post("http://fishograph.com/scripts/basicprofile.php", rp, new AsyncHttpResponseHandler(){
		 
		 //0 name, 1 dppath, 2 catches, 3 followers, 4 following
		 @Override
			public void onSuccess(String response) {
				// Hide Progress Dialog
			 pd.dismiss(); 
			 String s = response.toString();
			  //Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
			 s = s.trim();
			if(!s.equals("null"))
			{ 
			  String[] info = s.split(";");
			  name = info[0];
			  dppath = info[1];
			  info[2]=info[2].trim();
			  info[3]=info[3].trim();
			  info[4]=info[4].trim();
			  SpannableString sscatch = new SpannableString(info[2]+"\nposts");
		      SpannableString ssfollowers = new SpannableString(info[3]+"\nfollowers");
		      SpannableString ssfollowing = new SpannableString(info[4]+"\nfollowing");
			  
		      sscatch.setSpan(new RelativeSizeSpan(2f), 0, info[2].length(), 0);
			  ssfollowers.setSpan(new RelativeSizeSpan(2f), 0, info[3].length(), 0);
			  ssfollowing.setSpan(new RelativeSizeSpan(2f), 0, info[4].length() , 0);
				 
			  catches.setText(sscatch);
			  followers.setText(ssfollowers);
			  following.setText(ssfollowing);
			  tvname.setTypeface(null,Typeface.BOLD);
		      tvname.setText(name);
		      
			 } 
			    
		   }

			@Override
			public void onFailure(int statusCode, Throwable error,
					String content) {
				if (statusCode == 404) {
					Toast.makeText(getActivity(),
							"Requested resource not found",
							Toast.LENGTH_LONG).show();
				}
				else if (statusCode == 500) {
					Toast.makeText(getActivity(),
							"Something went wrong at server end",
							Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(
							getActivity(),
							"Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
									+ statusCode, Toast.LENGTH_LONG)
							.show();
				}
			}
			
			
		});
	 
	}

	public void getImage(String handle)
	{
		RequestParams rpdp = new RequestParams();
	    rpdp.add("handle", handle);
	    new AsyncHttpClient().post("http://fishograph.com/scripts/getpic.php", rpdp, new AsyncHttpResponseHandler(){
	    	  @Override
	    	  public void onSuccess(String response)
	    	  {
	    		  byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
	    		  Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	    		  dpbm = decodedByte;
	    		  LayoutParams lp = dp.getLayoutParams();
	    		  lp.width = width;
	    		  lp.height = width;
	    		  dp.requestLayout();
	    		  dp.setImageBitmap(decodedByte);
	    		  pg.setVisibility(View.INVISIBLE);
	    	  }
	    	  @Override
	    	  public void onFailure(int statusCode,Throwable error,String t)
	    	  {
	    		  Toast.makeText(getActivity(), "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
	    		  pg.setVisibility(View.INVISIBLE);
	    	  }
	      });
	}
	
}


class eachpost
{
	String handle,photourl,caption,dpurl;
	int likes,comments,isliked,photoid;
	String ul;
	public eachpost(String handle, String photourl, String caption,
			String uploadtime, int likes, int comments) {
		super();
		this.handle = handle;
		this.photourl = photourl;
		this.caption = caption;
		this.ul = uploadtime;
		this.likes = likes;
		this.comments = comments;
	}
	
	public eachpost(String handle, String photourl, String caption,
			String uploadtime, int likes, int comments, String dpurl, int isliked,int id) {
		super();
		this.handle = handle;
		this.photourl = photourl;
		this.caption = caption;
		this.ul = uploadtime;
		this.likes = likes;
		this.comments = comments;
		this.dpurl = dpurl;
		this.isliked = isliked;
		this.photoid = id;
	}
	public String getDpurl() {
		return dpurl;
	}

	public int getIsliked() {
		return isliked;
	}

	public int getPhotoid() {
		return photoid;
	}

	public String getHandle() {
		return handle;
	}
	public String getPhotoUrl() {
		return photourl;
	}
	public String getCaption() {
		return caption;
	}
	public int getLikes() {
		return likes;
	}
	public int getComments() {
		return comments;
	}
	
	public String getUl()
	{
		return ul;
	}
	public void incrementLikes(String h)
	{
	  this.likes++;	
	  isliked = 1;
	  RequestParams rp = new RequestParams();
		rp.add("handle",h);
		rp.add("url",this.photourl);
		
		new AsyncHttpClient().post("http://fishograph.com/scripts/like.php",rp,new AsyncHttpResponseHandler(){
			
			@Override
			public void onFailure(int code,Throwable t,String err)
			{   
				Log.d("onError like",err);
				likes--;
				isliked = 0; 
			}
			
			@Override
			public void onSuccess(String response)
			{
				response = response.trim();
				Log.d("like", response);
				if(!response.equals("liked"))
					{ 
                     likes--;
     				 Log.d("onError like",response);
     				 isliked = 0;
					}
			}
		});
	}
	
	public void decrementLikes(String h)
	{
		this.likes--;
		isliked = 0;
		RequestParams rp = new RequestParams();
		rp.add("handle",h);
		rp.add("url", this.photourl);
		
		new AsyncHttpClient().post("http://fishograph.com/scripts/unlike.php",rp,new AsyncHttpResponseHandler(){
			@Override
			public void onFailure(int code,Throwable t,String err)
			{
				Log.d("onError unlike", err);
			    likes++;
			    isliked = 1;
			}
			@Override
			public void onSuccess(String response)
			{
				Log.d("UnLiked",response);
				response = response.trim();
				if(!response.equals("unliked"))
					 {
					   likes++;
					   isliked = 1;
					 }
			}
		});
		
	}
}	

class UserPost extends Fragment 
{
    ArrayList<eachpost> posts;
    String handle;
    Bitmap dpbm;
    LayoutInflater inflater;
    ListView lv;
    int populate=0;
    int width,height;
    ScrollView scroll;
    Jsparser jp = new Jsparser();
	UserPost(String han, Bitmap dpbm)
	{
		super();
		handle = han;
		this.dpbm = dpbm;
	}
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, @Nullable Bundle savedInstanceState) {

	      View v = inflater.inflate(R.layout.listfragment, container,false);
          lv = (ListView) v.findViewById(R.id.olyposts);
          scroll = (ScrollView) v.findViewById(R.id.scrollvu);
          this.inflater = inflater;
          posts = new ArrayList<eachpost>();
          Display d = getActivity().getWindowManager().getDefaultDisplay();
          width = d.getWidth();
          height = d.getHeight();
          lv.setOnTouchListener(new OnTouchListener() {
        	    // Setting on Touch Listener for handling the touch inside ScrollView
        	    @Override
        	    public boolean onTouch(View v, MotionEvent event) {
        	    // Disallow the touch request for parent scroll on touch of child view
        	    v.getParent().requestDisallowInterceptTouchEvent(true);
        	    return false;
        	    }
        	});
	      return v;
	}
 	@Override
 	public void onActivityCreated(Bundle b)
 	{
 	  super.onActivityCreated(b);
 	  new GetData(0).execute();
 	}
	class GetData extends AsyncTask<String,String,String>
	  {
	    String msg;
	    Integer limit,success=0;
	    ProgressDialog pd;
	    Bitmap dpbm;
	    GetData(int l)
	    {
	    	limit = l;
	    }
	    @Override
	    public void onPreExecute()
	    {
	    	pd = new ProgressDialog(getActivity());
	    	pd.setCancelable(false);
	    	pd.show();
	    }
		@Override
		protected String doInBackground(String... params) {
			List<NameValuePair> lp = new ArrayList<NameValuePair>();
			lp.add(new BasicNameValuePair("handle",handle));
			lp.add(new BasicNameValuePair("start",limit.toString()));
			JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/getposts.php", "POST", lp);
			
			try
			{
			  Log.d("json", job.toString());
			  success = job.getInt("success");
			  msg = job.getString("message");
			  if(success==1)
			  {
			    JSONArray ja = job.getJSONArray("posts");
			    for(int c = 0; c<ja.length(); c++)
			    {
			      JSONObject jb = ja.getJSONObject(c);
			      posts.add(new eachpost(jb.getString("handle"),jb.getString("url"), jb.getString("caption"),
				  jb.getString("uldatetime"), jb.getInt("likes"), jb.getInt("comments")));
			      Log.d("arraylist content",posts.get(c).toString());
			    }
			  }
			}
			catch(Exception e)
			{
			  e.printStackTrace();
			}
		  	return msg;
		 }
		
		@Override
		public void onPostExecute(String url)
		{
	 	    pd.dismiss();
	 		PostAdapter pa = new PostAdapter(getActivity(),R.layout.listfragment,posts);
			lv.setAdapter(pa);
		}
    }
	
	class PostAdapter extends ArrayAdapter<eachpost> 
	{  
	  ViewHolder vh;
		public PostAdapter(Context context, int resource, ArrayList<eachpost> list) {
			super(context, R.layout.postcontent, list);
		}
		
		@SuppressLint("ViewHolder") @Override
		public View getView(int pos,View vu,ViewGroup vg)
		{
		   if(vu==null)
		   { 
			vu = inflater.inflate(R.layout.postcontent,vg,false);
			vh = new ViewHolder();
			vh.handle = ((TextView) vu.findViewById(R.id.handle));
			vh.caption = ((TextView) vu.findViewById(R.id.caption));
			vh.likesnum = ((TextView) vu.findViewById(R.id.likesnum));
			vh.comnum = ((TextView) vu.findViewById(R.id.comnum));
			vh.uploadtime = ((TextView) vu.findViewById(R.id.uploadtime));
			vh.photo = (ImageView) vu.findViewById(R.id.imgpost);
			vh.realdp = (ImageView) vu.findViewById(R.id.realdp);
			LayoutParams lp = vh.photo.getLayoutParams();
			lp.width = width;
			lp.height = width;
			vu.setTag(vh);
		   }
		   else
			vh = (ViewHolder) vu.getTag();
			//Toast.makeText(getActivity(), ""+posts.size(), Toast.LENGTH_SHORT).show();
			vh.handle.setText(posts.get(pos).getHandle());
			vh.caption.setText(posts.get(pos).getCaption());
			vh.likesnum.setText(posts.get(pos).getLikes()+"");
			vh.comnum.setText(posts.get(pos).getComments()+"");
			vh.uploadtime.setText(posts.get(pos).getUl());
			Glide.with(getActivity()).load("http://fishograph.com/scripts/"+posts.get(pos).getPhotoUrl()).into(vh.photo);
		    vh.realdp.setImageBitmap(dpbm);
		    
			return vu;
		}
	}
	
	static class ViewHolder
	{
			TextView handle;
			TextView caption;
			TextView likesnum;
			TextView comnum;
			TextView uploadtime;
			ImageView photo;
			ImageView realdp;
			ProgressBar pgpost;
	}
}
	
class UserPhotos extends Fragment
{
  String handle;
  Jsparser jp = new Jsparser();
  ArrayList<String> urllist = new ArrayList<String>();
  LayoutInflater inflater;
  GridView gv;
  UserPhotos(String han)
  {
    super();
    handle = han;
  }
  @Override
  public View onCreateView(LayoutInflater inflater,ViewGroup vg,Bundle b)
  {
    View v = inflater.inflate(R.layout.userphotos,vg,false);
    gv = (GridView) v.findViewById(R.id.photogrid);
    this.inflater = inflater;
    new GetGridUrls().execute();
    return v;
  }
  class GetGridUrls extends AsyncTask<String,String,String>
  {
    String msg;
    int success;
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		List<NameValuePair> lp = new ArrayList<NameValuePair>();
		lp.add(new BasicNameValuePair("handle",handle));
		JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/getphotosforhandle.php", "POST", lp);
		try
		{
		 success = job.getInt("success");
		 JSONArray ja = job.getJSONArray("photos");
		 for(int c = 0; c<ja.length();c++)
		 {
		  JSONObject jb = ja.getJSONObject(c);
		  Log.e("json",jb.toString());
		  urllist.add(jb.getString("url"));
		 }
		}
		catch(Exception e)
		{
		   e.printStackTrace();	
		}
		return msg;
	}
	
	@Override
	public void onPostExecute(String url)
	{
	  if(success==1)
	  {
	   PhotoAdapter pa = new PhotoAdapter(getActivity(), R.layout.photocontent, urllist, inflater);
	   gv.setAdapter(pa);
	  }
	}
	  
  }
  class PhotoAdapter extends ArrayAdapter<String>
  {
   ArrayList<String> urllist;
   LayoutInflater inflater;
	public PhotoAdapter(Context context, int resource,
			ArrayList<String> objects,LayoutInflater in) {
		super(context, resource, objects);
		urllist = objects;
		inflater = in;
	}
	@Override
	public View getView(int pos, View v, ViewGroup vg)
	{
	  View vu = inflater.inflate(R.layout.photocontent, vg, false);
	  final ImageView iv = (ImageView) vu.findViewById(R.id.userphoto);
	  Glide.with(getActivity()).load("http://fishograph.com/scripts/"+urllist.get(pos)).into(iv);
	  return vu;
	}
  }
}

class TaggedPhotos extends Fragment
{
  String handle;
  Jsparser jp = new Jsparser();
  ArrayList<grid> urllist = new ArrayList<grid>();
  LayoutInflater inflater;
  GridView gv;
  TaggedPhotos(String han)
  {
    super();
    handle = han;
  }
  @Override
  public View onCreateView(LayoutInflater inflater,ViewGroup vg,Bundle b)
  {
    View v = inflater.inflate(R.layout.userphotos,null,false);
    gv = (GridView) v.findViewById(R.id.photogrid);
    this.inflater = inflater;
    new GetGridUrls().execute();
    return v;
  }
  class GetGridUrls extends AsyncTask<String,String,String>
  {
    String msg;
    int success;
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		List<NameValuePair> lp = new ArrayList<NameValuePair>();
		lp.add(new BasicNameValuePair("handle",handle));
		JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/gettaggedphotos.php", "POST", lp);
		Log.d("tagged", job.toString());
		try
		{
		 success = job.getInt("success");
		 msg = job.getString("message");
		 JSONArray ja = job.getJSONArray("photos");
		 for(int c = 0; c<ja.length();c++)
		 {
		  JSONObject jb = ja.getJSONObject(c);
		  Log.e("json",jb.toString());
		  urllist.add(new grid(jb.getString("url")));
		 }
		}
		catch(Exception e)
		{
		  e.printStackTrace();
		}
		return msg;
	}
	
	@Override
	public void onPostExecute(String url)
	{
	  if(success==1)
	  { PhotoAdapter pa = new PhotoAdapter(getActivity(), R.layout.photocontent, urllist, inflater);
	   gv.setAdapter(pa);
	  }
	  else
          Toast.makeText(getActivity(), "No photos to show", Toast.LENGTH_SHORT).show();
	}
	  
  }
  class PhotoAdapter extends ArrayAdapter<grid> //we can go with ArrayAdater<string itself. Both works fine
  {
   ArrayList<grid> urllist;
   LayoutInflater inflater;
	public PhotoAdapter(Context context, int resource,
			ArrayList<grid> objects,LayoutInflater in) {
		super(context, resource, objects);
		urllist = objects;
		inflater = in;
	}
	@Override
	public View getView(int pos, View vu, ViewGroup vg)
	{
	   vu = inflater.inflate(R.layout.photocontent, vg, false);
	   final ImageView iv = (ImageView) vu.findViewById(R.id.userphoto);
	   //RequestParams rp = new RequestParams();
	   //rp.add("url", urllist.get(pos).getPic());
	   Glide.with(getActivity()).load("http://fishograph.com/scripts/"+urllist.get(pos).getPic()).into(iv);
	   /*if(iv.getDrawable()==null)
	   new AsyncHttpClient().post("http://fishograph.com/scripts/getphoto.php", rp, new AsyncHttpResponseHandler(){
		  @Override
		  public void onSuccess(String response)
		  {
		   photopg.setVisibility(View.INVISIBLE);
		   byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
		   
		   Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		   iv.setImageBitmap(decodedByte);
		  }
		  @Override
		  public void onFailure(int err, Throwable error, String response)
		  {
		    Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();	  
		  }
	   }); */
	  return vu;
	}
  }
}

class grid
{
  String pic;

public grid(String pic) {
	super();
	this.pic = pic;
}

public String getPic() {
	return pic;
}
  
	
}