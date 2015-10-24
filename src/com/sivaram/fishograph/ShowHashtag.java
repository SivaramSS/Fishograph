package com.sivaram.fishograph;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ShowHashtag extends ActionBarActivity implements OnScrollListener{
    boolean loading = false;
    ArrayList<String> urls;
    GridAdapter ga;
    String hashtag;
    int width;
    GridView gvphotos;
    Jsparser jp;
	@Override
	protected void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.userphotos);
		ActionBar ab = getSupportActionBar();
		ab.setBackgroundDrawable(new ColorDrawable(Color.RED));
		hashtag = getIntent().getExtras().getString("hashtag");
		ab.setTitle(hashtag);
		hashtag = hashtag.replace("#", "");
		Log.d("showhashtag ", hashtag);
		hashtag = hashtag.trim();
		gvphotos = (GridView) findViewById(R.id.photogrid);
		Display d = getWindowManager().getDefaultDisplay();
		width = d.getWidth();
		urls = new ArrayList<String>();
	    jp = new Jsparser();
		ga = new GridAdapter();
		gvphotos.setAdapter(ga);
	    new GetPhotos().execute();
		
	}

	
	 class GetPhotos extends AsyncTask<String,String,String> {
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
				lp.add(new BasicNameValuePair("search",hashtag));
				JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/searchbyhashtag.php", "POST", lp);
				try {
					success = job.getInt("success");
					msg = job.getString("message");
					Log.d("json Object hashtag",job.toString());
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
					super(getApplicationContext(), R.layout.photocontent,urls);
				}
				
				@Override
				public View getView(final int pos,View row,ViewGroup vg)
				{
				   if(row == null)
				   {
					 vh = new ViewHolder();
					 row = getLayoutInflater().inflate(R.layout.photocontent, vg,false);
					 vh.photo = (ImageView) row.findViewById(R.id.userphoto);
					 LayoutParams lp = vh.photo.getLayoutParams();
					 lp.height = width / 3;
					 lp.width = width / 3;
					 row.setTag(vh);
				   }
				   else
				     vh = (ViewHolder) row.getTag();
				   Log.d("getview", urls.get(pos));
				   Glide.with(getApplicationContext()).load("http://fishograph.com/scripts/"+urls.get(pos)).into(vh.photo);
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
		                 new GetPhotos().execute();
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
