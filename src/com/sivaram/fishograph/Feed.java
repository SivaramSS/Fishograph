package com.sivaram.fishograph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikhaellopez.circularimageview.CircularImageView;


@SuppressLint("UseValueOf") public class Feed extends Fragment implements OnScrollListener
{ 
 String handle;
 ListView lvposts;
 Jsparser jp;
 int width,height;
 int maxMemory;
 int currentFirstVisibleItem ;
 int currentVisibleItemCount;
 PostAdapter pa;
 ArrayList<eachpost> posts;
 int value = 1;
 boolean isLoading = false;
 int photoid;
 private List<String> myData;
 Boolean tapped = false, Loading= false;
 SharedPreferences spf;
 ArrayList<String> likes;
 public Feed()
 {
   super();
 }
  Feed(String handle)
  {
   super();
   photoid = 99999999;
   this.handle = handle;
   
  }
  
   @Override
   public void onCreate(Bundle b)
   {
	super.onCreate(b);
	maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	final int cacheSize = maxMemory / 8;
	spf = getActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE);
    likes = new ArrayList<String>();
   }
  @Override
  public View onCreateView(LayoutInflater inflater,ViewGroup vg, Bundle b)
  {
     
   View v = inflater.inflate(R.layout.allposts, vg, false);
   ActionBar ab = ((ActionBarActivity) getActivity()).getSupportActionBar();
   ab.setBackgroundDrawable(new ColorDrawable(Color.RED));
   ab.hide();
   lvposts = (ListView) v.findViewById(R.id.lvposts);
   jp = new Jsparser();
   Display d = getActivity().getWindowManager().getDefaultDisplay();
   width = d.getWidth();
   height = d.getHeight();
   lvposts.setOnScrollListener(this);
   posts = new ArrayList<eachpost>();
   pa = new PostAdapter(getActivity(),R.layout.postcontent,posts,inflater);
   Loading = true;
   lvposts.setAdapter(pa);
   lvposts.setScrollY(height);
   new GetData(photoid).execute(); 
   return v;
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
	    }
		@Override
		protected String doInBackground(String... params) {
			List<NameValuePair> lp = new ArrayList<NameValuePair>();
			lp.add(new BasicNameValuePair("handle",handle));
			lp.add(new BasicNameValuePair("photoid",limit.toString()));
			JSONObject job = jp.makeHttpRequest("http://fishograph.com/scripts/getfeed.php", "POST", lp);
			
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
				  jb.getString("uldatetime"), jb.getInt("likescount"), jb.getInt("comcount"), jb.getString("dpurl"), jb.getInt("isliked"),jb.getInt("photoid") ));
			    }
			  }
			  else
			  {
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
		 Loading = false;
		 if(success==1)
		  {    
		    //gd = new GestureDetector(getActivity(),new GestureListener(posts));
		    photoid = posts.get(posts.size()-1).getPhotoid();
			Log.d("last id",photoid+"");
			Log.d("Length of posts",""+posts.size());
			pa.notifyDataSetChanged();
			//lvposts.invalidateViews();
		  }
		}
		  
	  }
	
	class PostAdapter extends ArrayAdapter<eachpost> 
	{
      ViewHolder vholder;
      String root = Environment.getExternalStorageDirectory().toString();
	  File dir = new File (root + "/fishograph/.feed");
	  Map<Integer,View> myViews;
		public PostAdapter(Context context, int resource, ArrayList<eachpost> list, LayoutInflater li) {
			super(context, R.layout.postcontent, list); 
			myViews = new HashMap<Integer,View>();
			//final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			//final int cacheSize = maxMemory / 8;
		   // p = new Picasso.Builder(context)
		   // .memoryCache((Cache) new LruCache(24000))
		   // .build();
		}
		
		@Override
		public View getView(final int pos,View v,ViewGroup vg)
		{	
			 final eachpost post = posts.get(pos);
			  final String imgurl = post.getPhotoUrl();
			  String dpurl = post.getDpurl();
			  int isliked = post.getIsliked();
			  View row = myViews.get(pos);
			  if(row == null)
			  { 
			   row = getActivity().getLayoutInflater().inflate(R.layout.postcontent,vg,false); 
			   row.setMinimumHeight(height);
			   vholder = new ViewHolder();
			   vholder.handle = ((TextView) row.findViewById(R.id.handle));
			   vholder.caption = ((TextView) row.findViewById(R.id.caption));
			   vholder.likesnum = ((TextView) row.findViewById(R.id.likesnum));
			   vholder.comnum = ((TextView) row.findViewById(R.id.comnum));
			   vholder.uploadtime = ((TextView) row.findViewById(R.id.uploadtime));
			   vholder.photo = (ImageView) row.findViewById(R.id.imgpost);
			   vholder.feeddp = (CircularImageView) row.findViewById(R.id.realdp);
			   vholder.like = (FlipImageView) row.findViewById(R.id.like);
			   LayoutParams lp = vholder.photo.getLayoutParams();
			   lp.width = width;
			   lp.height = width;
			   vholder.handle.setText(post.getHandle());
			   vholder.caption.setText(post.getCaption());
			   vholder.likesnum.setText(post.getLikes()+"");
			   vholder.comnum.setText(post.getComments()+"");
			   vholder.uploadtime.setText(post.getUl());
			   row.setTag(vholder); //changed here setTag()
			 }else{
			     vholder=(ViewHolder)row.getTag(); //changed here getTag()
			 }

			 Drawable d = getResources().getDrawable(R.drawable.hook_like);
			 vholder.like.setFlippedDrawable(d);
			 Glide.with(getActivity()).load("http://fishograph.com/scripts/"+imgurl).into(vholder.photo);

			 if(dpurl.contains("http"))
			     Glide.with(getActivity()).load(dpurl).into(vholder.feeddp);
			 else
			     Glide.with(getActivity()).load("http://fishograph.com/scripts/"+dpurl).into(vholder.feeddp);
			 Log.d("image loading", dpurl + "-" + imgurl);
			 if(isliked==1)
			      {
			        vholder.like.setFlipped(true,false);
			        likes.add(imgurl);
			      }

			 vholder.like.setOnClickListener(new View.OnClickListener() {
			     @Override
			     public void onClick(View v) {
			         View temp = myViews.get(pos);
			         final FlipImageView like = (FlipImageView) temp.findViewById(R.id.like);
			         final TextView likesnum = (TextView) temp.findViewById(R.id.likesnum);
			         like.toggleFlip();

			         if(!likes.contains(imgurl))
			            {
			             posts.get(pos).incrementLikes(handle);
			             likes.add(posts.get(pos).getPhotoUrl());
			             likesnum.setText(posts.get(pos).getLikes()+"");
			           }
			         else
			           {
			             likes.remove(posts.get(pos).getPhotoUrl());
			             posts.get(pos).decrementLikes(handle);
			             likesnum.setText(posts.get(pos).getLikes()+"");
			           }
			     }
			 });
			 myViews.put(pos, row);
			 
			 return row;
		}
		
		@Override
		public boolean isEnabled(int position)
		{
		    return true;
		}
	}
  static class ViewHolder {
		    TextView handle;
			TextView caption;
			TextView likesnum;
			TextView comnum;
			TextView uploadtime;
			ImageView photo;
			CircularImageView feeddp;
			FlipImageView like;
	}

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
	 if (this.currentVisibleItemCount > 0 && scrollState == SCROLL_STATE_FLING) {
        /*** In this way I detect if there's been a scroll which has completed ***/
        /*** do the work for load more date! ***/
        
        	if(currentFirstVisibleItem > (currentVisibleItemCount - 2) && Loading!=true)
            {
                new GetData(photoid).execute();
            }       
    }
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem,
	 	int visibleItemCount, int totalItemCount) {
    	this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        
  }
  
}

