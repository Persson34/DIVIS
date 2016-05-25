package com.example.m.divis;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Typeface;

import android.hardware.Camera;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
   
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "DIVISMainActivity";
	private static final int MY_PERMISSIONS_REQUEST_CAMERA = 42;

	// custom font Calibri.ttf
	public static Typeface typeface;// = Typeface.createFromAsset(getContext().getAssets(), "fonts/Calibri.ttf");

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	public SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public MyViewPager mViewPager;

	// Native camera.
	public Camera mCamera;

	// size of image captured by camera, set to largest available
	public Camera.Size mCaptureSize;
	// adjusted by device orientation
	public int mCaptureWidth;
	public int mCaptureHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// for hiding titlebar
		if (Build.VERSION.SDK_INT < 16) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
			ActionBar actionBar = getActionBar();
			if(actionBar != null)
				actionBar.hide();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// for hiding titlebar
		View root = findViewById(R.id.main_content);
		root.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		typeface = Typeface.createFromAsset(getAssets(), "fonts/Calibri.ttf");

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (MyViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(1, false);

		// prevent destruction of other fragments
		mViewPager.setOffscreenPageLimit(2);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		/*
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
		  }
	  });
	  */
		setupCamera();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_main, menu);
//		return true;
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	boolean setupCamera()
	{
		boolean opened = false;
		int permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.CAMERA);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			opened = safeCameraOpen();
			if(!opened) {
				Log.d(TAG, "Error, Camera failed to open");
			}
		} else {
//			// Should we show an explanation?
//			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//					Manifest.permission.CAMERA)) {
//
//				// Show an expanation to the user *asynchronously* -- don't block
//				// this thread waiting for the user's response! After the user
//				// sees the explanation, try again to request the permission.
//
//			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						MY_PERMISSIONS_REQUEST_CAMERA);

				// MY_PERMISSIONS_REQUEST_CAMERA is an
				// app-defined int constant. The callback method gets the
				// result of the request.
//			}
		}
		return opened;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
			String permissions[], int[] grantResults) {
		switch (requestCode) {
		case MY_PERMISSIONS_REQUEST_CAMERA: {
			Log.d(TAG, "MY_PERMISSIONS_REQUEST_CAMERA");
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				Log.d(TAG, "MY_PERMISSIONS_REQUEST_CAMERA 2");
				if(safeCameraOpen()) {
					/*
//					FragmentCalibrate fc = (FragmentCalibrate)getSupportFragmentManager().findFragmentByTag("calibrate");
					FragmentCalibrate fc = (FragmentCalibrate)mSectionsPagerAdapter.getItem(1);
					if(fc != null) {
						fc.onCameraReady(this);
					}
					*/
					Log.d(TAG, "=== PERMISSION GRANTED, SETTING UP UI!");
					recreate();
				} else {
					Log.d(TAG, "Error, failed to open Camera");
				}
			} else {
				// permission denied, boo! Disable the
				// functionality that depends on this permission.
			}
			return;
		}
		}
	}

	private boolean safeCameraOpen() {
		boolean qOpened = false;
//		releaseCameraAndPreview();
		mCamera = getCameraInstance();
		qOpened = (mCamera != null);

		Log.d(TAG, "INFO, qOpened==" + qOpened);

		if(qOpened == true) {
			// disable shutter sound
			Camera.CameraInfo info = new Camera.CameraInfo();
			// TODO: camera index setting
			Camera.getCameraInfo(0, info);
			if (info.canDisableShutterSound) {
				mCamera.enableShutterSound(false);
			}

			// determine largest capture size available
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> capture_sizes = params.getSupportedPictureSizes();
			//mCaptureSize = capture_sizes.get(capture_sizes.size()-1);
			int idx_of_largest = 0;
			for(int i=0; i<capture_sizes.size(); i++) {
				Log.d(TAG, "Capture Size: " + capture_sizes.get(i).width + "x" + capture_sizes.get(i).height);
				if(capture_sizes.get(idx_of_largest).height < capture_sizes.get(i).height)
					idx_of_largest = i;
			}
			mCaptureSize = capture_sizes.get(idx_of_largest);

			// adjust by orientation
			Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			switch (display.getRotation()) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				mCaptureWidth = mCaptureSize.height;
				mCaptureHeight = mCaptureSize.width;
				break;
			default:
				mCaptureWidth = mCaptureSize.width;
				mCaptureHeight = mCaptureSize.height;
				break;
			}

			params.setPictureSize(mCaptureWidth, mCaptureHeight);

			Log.d(TAG, "Capture Size Set To " + mCaptureSize.width + "x" + mCaptureSize.height);
			Log.d(TAG, "Capture Size (rot): " + mCaptureWidth + "x" + mCaptureHeight);

			// capture settings
			// Configure image format. RGB_565 is the most common format.
			List<Integer> formats = params.getSupportedPictureFormats();
//			if (formats.contains(PixelFormat.RGB_565)) {
//				Log.d(TAG, "Picture format: RGB_565");
//				params.setPictureFormat(PixelFormat.RGB_565);
//			} else {
//				Log.d(TAG, "Picture format: JPEG");
				params.setPictureFormat(PixelFormat.JPEG);
//			}
			params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(params);
		}
		return qOpened;
	}

	/**
	 * Safe method for getting a camera instance.
	 * @return
	 */
	public static Camera getCameraInstance(){
		Camera c = null;
		Log.d(TAG, "INFO, <getCameraInstance>");
		try {
			int n = Camera.getNumberOfCameras();
			// TODO: setting to select camera
			if(n > 0)
				c = Camera.open(0); // attempt to get a Camera instance
		} catch (Exception e){
			e.printStackTrace();
		}
		Log.d(TAG, "INFO, </getCameraInstance> " + c);
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position) {
			case 0:
				return new FragmentSettings();
			case 1:
				return new FragmentCalibrate();
			case 2:
				return new FragmentData();
			}

			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_settings).toUpperCase(l);
			case 1:
				return getString(R.string.title_calibrate).toUpperCase(l);
			case 2:
				return getString(R.string.title_data).toUpperCase(l);
			}
			return null;
		}
	}
}
