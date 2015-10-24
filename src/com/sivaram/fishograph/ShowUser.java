package com.sivaram.fishograph;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

public class ShowUser extends ActionBarActivity{
	
	@Override
	protected void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.activity_showuser);
		String han = getIntent().getExtras().getString("handle");
		boolean own = getIntent().getExtras().getBoolean("own");
		Fragment f = new Profile(han,own, getSupportFragmentManager());
		getSupportFragmentManager().beginTransaction().replace(R.id.show, f).commit();
		
	}

}
