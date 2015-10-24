package com.sivaram.fishograph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikhaellopez.circularimageview.CircularImageView;


public class Search extends Fragment {
	final String handle;
	EditText etsearch;
	TextView bypost,people,hashtags;
	int choice = 0;
	FragmentManager cfm;
	Search(String h)
	{
	  this.handle = h;
	}
	
	@SuppressLint("InflateParams") @Override
	public View onCreateView(LayoutInflater inflater,ViewGroup vg,Bundle b)
	{
		View v = inflater.inflate(R.layout.search, null);
		((ActionBarActivity) getActivity()).getSupportActionBar().hide();
		//etsearch = (EditText) v.findViewById(R.id.etsearch);
		bypost = (TextView) v.findViewById(R.id.bypost);
		people = (TextView) v.findViewById(R.id.people);
		hashtags = (TextView) v.findViewById(R.id.hashtags);
		cfm = getChildFragmentManager();
		
		bypost.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bypost.setBackgroundColor(0xffffffff);
				bypost.setTextColor(0xffea5f4f);
				people.setBackgroundColor(0x00ffffff);
				people.setTextColor(0xffffffff);
				hashtags.setBackgroundColor(0x00ffffff);
				hashtags.setTextColor(0xffffffff);
				Fragment f = new DisplayPhotos();
		        cfm.beginTransaction().replace(R.id.searchcontent, f).commit();
			}
		});
		
        people.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bypost.setBackgroundColor(0x00ffffff);
				bypost.setTextColor(0xffffffff);
				people.setBackgroundColor(0xffffffff);
				people.setTextColor(0xffea5f4f);
				hashtags.setBackgroundColor(0x00ffffff);
				hashtags.setTextColor(0xffffffff);
				
				Fragment f = new SearchPeople(handle);
				cfm.beginTransaction().replace(R.id.searchcontent, f).commit();
			}
		});
        
        hashtags.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bypost.setBackgroundColor(0x00ffffff);
				bypost.setTextColor(0xffffffff);
				people.setBackgroundColor(0x00ffffff);
				people.setTextColor(0xffffffff);
				hashtags.setBackgroundColor(0xffffffff);
				hashtags.setTextColor(0xffea5f4f);
				Fragment f = new SearchHashtag(handle);
				cfm.beginTransaction().replace(R.id.searchcontent, f).commit();
			}
		});
        Fragment f = new DisplayPhotos();
        cfm.beginTransaction().replace(R.id.searchcontent, f).commit();
        bypost.setBackgroundColor(0xffffffff);
        bypost.setTextColor(0xffea5f4f);
		return v;
	}

}

class DisplayPhotos extends Fragment implements OnScrollListener
{
 GridView gvposts;
 Jsparser jp;
 ArrayList<String> urls;
 GridAdapter ga;
 int width;
 boolean loading = false;
 DisplayPhotos()
 {
   super();
   jp = new Jsparser();
   urls = new ArrayList<String>();
   loading = true;
   new GetRandomPosts().execute();
 }
 
 @Override
 public View onCreateView(LayoutInflater inflater,ViewGroup vg,Bundle b)
 {
   View v = inflater.inflate(R.layout.userphotos, vg,false);
   gvposts = (GridView) v.findViewById(R.id.photogrid);
   ga = new GridAdapter();
   Display d = getActivity().getWindowManager().getDefaultDisplay();
   width = d.getWidth();
   //gvposts.setColumnWidth(((width/3)+1));
   gvposts.setAdapter(ga);
   return v;
 }
 
 class GetRandomPosts extends AsyncTask<String,String,String> {
    int success;
    String msg;
    @Override
    public void onPreExecute()
    {
      loading = true;
    }
	@Override
	protected String doInBackground(String... params) {
       
		List<NameValuePair> lp = new ArrayList<NameValuePair>();
		JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/getrandomphotos.php", "POST", lp);
		
		try {
			success = job.getInt("success");
			msg = job.getString("message");
			Log.d("json Object",job.toString());
			if(success==1)
			{
			 JSONArray ja = job.getJSONArray("photos");
			 for(int c=0;c<ja.length();c++)
			 {
			 JSONObject jb = ja.getJSONObject(c);
			 urls.add(new String(jb.getString("url")));
			 }
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String url)
	{
	  ga.notifyDataSetChanged();
	}
	
   }
 
 class GridAdapter extends ArrayAdapter<String> {
		ViewHolder vh;
		public GridAdapter()
		{
			super(getActivity(), R.layout.photocontent,urls);
		}
		
		@Override
		public View getView(final int pos,View row,ViewGroup vg)
		{
		   if(row == null)
		   {
			 vh = new ViewHolder();
			 row = getActivity().getLayoutInflater().inflate(R.layout.photocontent, vg,false);
			 vh.photo = (ImageView) row.findViewById(R.id.userphoto);
			 LayoutParams lp = vh.photo.getLayoutParams();
			 lp.height = width / 3;
			 lp.width = width / 3;
			 row.setTag(vh);
		   }
		   else
		     vh = (ViewHolder) row.getTag();
		   Log.d("getview", urls.get(pos));
		   Glide.with(getActivity()).load("http://fishograph.com/scripts/"+urls.get(pos)).into(vh.photo);
		   return row;
		}

	} 
 
   static class ViewHolder
   {
	 ImageView photo;
   }
   
   @Override
   public void onScrollStateChanged(AbsListView view, int scrollState) {
 	 if (this.currentVisibleItemCount > 0 && scrollState == SCROLL_STATE_IDLE) {
         /*** In this way I detect if there's been a scroll which has completed ***/
         /*** do the work for load more date! ***/
         
         	if(currentFirstVisibleItem > (currentVisibleItemCount - 5) && loading!=true)
             {
                 new GetRandomPosts().execute();
             }       
     }
   }

   @Override
   public void onScroll(AbsListView view, int firstVisibleItem,
 	 	int visibleItemCount, int totalItemCount) {
     	this.currentFirstVisibleItem = firstVisibleItem;
         this.currentVisibleItemCount = visibleItemCount;
         
   }
   
   int currentFirstVisibleItem,currentVisibleItemCount;
 }

class Person
{
  String dpurl,han,name;
  int isfollowing;
  Person(String han,String name,String dpurl)
  {
   this.han = han;
   this.dpurl = dpurl;
   this.name = name;
  }
  	public String getDpurl() {
  		return dpurl;
  	}

  	public String getHan() {
  		return han;
  	}
	public String getName() {
		return name;
	}
}

class SearchPeople extends Fragment
{
 String handle;
 EditText etsearch;
 Jsparser jp = new Jsparser();
 TextView searchlabel;
 ArrayList<Person> peoplelist;
 boolean loading = false;
 ListView lvpeople;
 ListAdapter la;
 ImageButton search;
 ProgressDialog pd;
 SearchPeople(String s)
 {
   super();
   handle = s;
   peoplelist = new ArrayList<Person>();
   new GetRandomPeople().execute();
 }
 
 @Override
 public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b)
 {
   View v = (View) inflater.inflate(R.layout.people, vg,false);    
   etsearch = (EditText) v.findViewById(R.id.etsearch);
   searchlabel = (TextView) v.findViewById(R.id.etlabelsearch);
   lvpeople = (ListView) v.findViewById(R.id.listofpeople);
   search = (ImageButton) v.findViewById(R.id.go);
   la = new ListAdapter();
   lvpeople.setAdapter(la);
   etsearch.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			    searchlabel.setVisibility(View.VISIBLE);	
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				searchlabel.setVisibility(View.INVISIBLE);
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				if(etsearch.getText().toString().equals(""))
 					 { searchlabel.setVisibility(View.VISIBLE);
 					   new GetRandomPeople().execute(); 
 					 }
 				else
 					searchlabel.setVisibility(View.INVISIBLE);
 			}
 		});
 		
    search.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final String searchtext = etsearch.getText().toString();
			  
			pd = new ProgressDialog(getActivity(),R.style.MyTheme);
			pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			pd.setCancelable(false);
			pd.show();
			  
			new AsyncTask<String,String,String>(){
                int success;
				String msg;
				@Override
				protected String doInBackground(String... params) {
					List<NameValuePair> lp = new ArrayList<NameValuePair>();
					lp.add(new BasicNameValuePair("search",etsearch.getText().toString()));
					Log.d("search string ", searchtext);
					JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/searchbyhandle.php", "POST", lp);
					try
					{
					  success =  job.getInt("success");
					  msg = job.getString("message");
					  Log.d("Search people 330", job.toString());
					  if(success == 1)
					  {
					   peoplelist.clear();
					   JSONArray ja = job.getJSONArray("info");	  
					   for(int c=0;c<ja.length();c++)  
					   {   
						   JSONObject jb = ja.getJSONObject(c);
						   peoplelist.add(new Person(jb.getString("handle"),jb.getString("name"),jb.getString("dpurl")));	  
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
					if(success == 1)
					{
					   	la.notifyDataSetChanged();
					}
				}
				
			}.execute();
		}
	});
    
    lvpeople.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
             Person p = peoplelist.get(position);
             Intent i = new Intent(getActivity(),ShowUser.class);
             i.putExtra("handle", p.getHan());
             if(p.getHan().equals(handle))
            	 i.putExtra("own", true);
             else
            	 i.putExtra("own", false);
             startActivity(i);
		}
    });
    
   return v;
 }
 
 class GetRandomPeople extends AsyncTask<String,String,String> {
	    int success;
	    String msg;
	    
	    @Override
	    public void onPreExecute()
	    {
	      loading = true;
	    }
		@Override
		protected String doInBackground(String... params) {
	       
			List<NameValuePair> lp = new ArrayList<NameValuePair>();
			lp.add(new BasicNameValuePair("handle",handle));
			JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/getrandompeople.php", "POST", lp);
			
			try {
				success = job.getInt("success");
				msg = job.getString("message");
				Log.d("Search people 329",job.toString());
				if(success==1)
				{
				 JSONArray ja = job.getJSONArray("peoplelist");
				 for(int c=0;c<ja.length();c++)
				 {
				 JSONObject jb = ja.getJSONObject(c);
				 peoplelist.add(new Person(jb.getString("handle"),jb.getString("name"),jb.getString("dpurl")) );
				 }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String url)
		{
		  la.notifyDataSetChanged();
		}
	 }
 
   class ListAdapter extends ArrayAdapter<Person> {
	   ViewHolder vh;
	   Map<Integer,View> myViews;
	   ListAdapter()
	   {
		  super(getActivity(),R.layout.person,peoplelist);
		  myViews = new HashMap<Integer,View>();
	   }
	   
	   @Override
	   public View getView(final int pos,View row, ViewGroup vg)
	   {
		  Person p = peoplelist.get(pos);
		  row = myViews.get(new Integer(pos));
		  if(row == null)
		  {
			row = getActivity().getLayoutInflater().inflate(R.layout.person, vg,false); 
			vh = new ViewHolder();
			vh.civ = (CircularImageView) row.findViewById(R.id.persondp);
			vh.tvhan = (TextView) row.findViewById(R.id.phandle);
			vh.tvname = (TextView) row.findViewById(R.id.pname);
			vh.follow = (Button) row.findViewById(R.id.follow);
			row.setTag(vh);
			myViews.put(new Integer(pos), row);
			if(p.getHan().equals(handle))
			{
				vh.follow.setEnabled(false);
				vh.follow.setVisibility(View.INVISIBLE);
			}
				
			 vh.tvhan.setText("@"+p.getHan());
			  if(p.getName().contains("not specified"))
			  vh.tvname.setText(" ");
			  else
			  vh.tvname.setText(p.getName());
		  }
		  else
			 vh = (ViewHolder) row.getTag(); 
		  if(p.getDpurl().contains("http"))
			  Glide.with(getActivity()).load(p.getDpurl()).into(vh.civ);
		  else
			  Glide.with(getActivity()).load("http://fishograph.com/scripts/"+p.getDpurl()).into(vh.civ);

		  vh.follow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vh.follow.getText().equals("Following"))
				{ //unfollow
					final Button follow = (Button) myViews.get(new Integer(pos)).findViewById(R.id.follow);
					RequestParams rp = new RequestParams();
					rp.add("handle", handle);
					rp.add("friend", peoplelist.get(pos).getHan());
				    new AsyncHttpClient().post("http://fishograph.com/scripts/unfollow.php",rp,new AsyncHttpResponseHandler(){
				    	@Override
				    	public void onFailure(int code,Throwable t,String err)
				    	{
				    		Log.d("Search SearchByPeople", err);
				    	}
				    	
				    	@Override
				    	public void onSuccess(String response)
				    	{
				         response = response.trim();

				         if(response.equals("success"))
				           follow.setText("Follow");
				         else
				           Toast.makeText(getActivity(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
				    	}
				    });				
				}
				else
				{ //follow
				final Button follow = (Button) myViews.get(new Integer(pos)).findViewById(R.id.follow);
				RequestParams rp = new RequestParams();
				rp.add("handle", handle);
				rp.add("friend", peoplelist.get(pos).getHan());
			    new AsyncHttpClient().post("http://fishograph.com/scripts/follow.php",rp,new AsyncHttpResponseHandler(){
			    	@Override
			    	public void onFailure(int code,Throwable t,String err)
			    	{
			    		Log.d("UpdateDB follow()", err);
			    	}
			    	
			    	@Override
			    	public void onSuccess(String response)
			    	{
			    	 response = response.trim();
			    	 if(response.equals("success"))
			    	   follow.setText("Following");
			    	 else
			    	   Toast.makeText(getActivity(), "Something went wrong..Please try again", Toast.LENGTH_SHORT).show();
			    	}
			    });
				}//end of if
			}
		});
		  return row;
	   }
   }
 
   static class ViewHolder
   {
	 CircularImageView civ;
	 TextView tvhan;
	 TextView tvname;
	 Button follow;
   }
}


class SearchHashtag extends Fragment
{
 String handle;
 EditText etsearch;
 Jsparser jp = new Jsparser();
 TextView searchlabel;
 ArrayList<String> hashtaglist;
 boolean loading = false;
 ListView lvhashtag;
 
 ArrayAdapter<String> la;
 ImageButton search;
 ProgressDialog pd;
 SearchHashtag(String s)
 {
   super();
   handle = s;
   hashtaglist = new ArrayList<String>();
   new GetTrendingTags().execute();
 }
 
 @Override
 public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b)
 {
   View v = (View) inflater.inflate(R.layout.people, vg,false);    
   etsearch = (EditText) v.findViewById(R.id.etsearch);
   searchlabel = (TextView) v.findViewById(R.id.etlabelsearch);
   lvhashtag = (ListView) v.findViewById(R.id.listofpeople);
   search = (ImageButton) v.findViewById(R.id.go);
   la = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,hashtaglist);
   lvhashtag.setAdapter(la);
   etsearch.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			    searchlabel.setVisibility(View.VISIBLE);	
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				searchlabel.setVisibility(View.INVISIBLE);
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				if(etsearch.getText().toString().equals(""))
 					 { searchlabel.setVisibility(View.VISIBLE);
 					   new GetTrendingTags().execute(); 
 					 }
 				else
 					searchlabel.setVisibility(View.INVISIBLE);
 			}
 		});
 		
    search.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final String searchtext = etsearch.getText().toString();
			  
			pd = new ProgressDialog(getActivity(),R.style.MyTheme);
			pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
			pd.setCancelable(false);
			pd.show();
			  
			new AsyncTask<String,String,String>(){
                int success;
				String msg;
				@Override
				protected String doInBackground(String... params) {
					List<NameValuePair> lp = new ArrayList<NameValuePair>();
					lp.add(new BasicNameValuePair("search",etsearch.getText().toString()));
					Log.d("search string ", searchtext);
					JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/searchbyhashtag.php", "POST", lp);
					try
					{
					  success =  job.getInt("success");
					  msg = job.getString("message");
					  Log.d("Search people 637", job.toString());
					  if(success == 1)
					  {
					   hashtaglist.clear();
					   JSONArray ja = job.getJSONArray("hashtaglist");	  
					   for(int c=0;c<ja.length();c++)  
					   {   
						   JSONObject jb = ja.getJSONObject(c);
						   hashtaglist.add("#"+jb.getString("hashtag"));	  
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
					if(success == 1)
					{
					   	la.notifyDataSetChanged();
					}
				}
				
			}.execute();
		}
	});
    
    lvhashtag.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
            
             Intent i = new Intent(getActivity(),ShowHashtag.class);
             i.putExtra("hashtag", hashtaglist.get(position));
             startActivity(i);
		}
    });
    
   return v;
 }
 
 class GetTrendingTags extends AsyncTask<String,String,String> {
	    int success;
	    String msg;
	    
	    @Override
	    public void onPreExecute()
	    {
	      loading = true;
	    }
		@Override
		protected String doInBackground(String... params) {
	       
			List<NameValuePair> lp = new ArrayList<NameValuePair>();
			lp.add(new BasicNameValuePair("handle",handle));
			JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/trendingtags.php", "POST", lp);
			
			try {
				success = job.getInt("success");
				msg = job.getString("message");
				Log.d("Search people 329",job.toString());
				if(success==1)
				{
				 JSONArray ja = job.getJSONArray("trending");
				 for(int c=0;c<ja.length();c++)
				 {
				 JSONObject jb = ja.getJSONObject(c);
				 hashtaglist.add("#"+jb.getString("hashtag"));
				 }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String url)
		{
		  la.notifyDataSetChanged();
		}
	 }
 
 }

