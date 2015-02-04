package com.example.photopicker;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class LargePreviewActivity extends ActionBarActivity {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}else if (id == R.id.action_choice) {
			setResult(RESULT_OK, new Intent().putExtra("filePath", filePath));
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private String filePath;
    private boolean isJustPreview = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.large_preview_layout);
		
		filePath = getIntent().getStringExtra("filePath");
        String orientation = getIntent().getStringExtra("orientation");
        isJustPreview = getIntent().getBooleanExtra("isJustPreview", true);

		if(TextUtils.isEmpty(filePath) || TextUtils.isEmpty(orientation)){
			Log.e("LargePreviewActivity", "file path is " + filePath);
			finish();
			return;
		}
		
//		final View viewTitleBar = findViewById(R.id.title_bar);
		
//		setTitle(ImagePickerPlusActivity.getSizeStr(new File(filePath).length()));
		
		getSupportActionBar().setTitle(ImagePickerPlusActivity.getSizeStr(new File(filePath).length()));
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setShowHideAnimationEnabled(false);
		
//		setTopLeftImageButtonBackAction(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				setResult(RESULT_CANCELED);
//				finish();
//			}
//		});
		
//		final Button btTopRight = (Button)findViewById(R.id.bt_top_right);
//		btTopRight.setVisibility(View.VISIBLE);
//		btTopRight.setText("选择");
//		btTopRight.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				setResult(RESULT_OK, new Intent().putExtra("filePath", filePath));
//				finish();
//			}
//		});
		
		Bitmap bp = BitmapUtil.getBitmap(filePath);
        int orientationInt = Integer.parseInt(orientation);
		if(bp != null){
            if(orientationInt > 0 && orientationInt < 360){
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.reset();
                matrix.setRotate(orientationInt);
                bp = Bitmap.createBitmap(bp,0,0,bp.getWidth(),bp.getHeight(),matrix,false);
            }
			ImageView ivt = (ImageView)findViewById(R.id.zivp);
			ivt.setScaleType(ScaleType.FIT_CENTER);
			ivt.setImageBitmap(bp);
			ivt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(getSupportActionBar().isShowing()){
						getSupportActionBar().hide();
					}else{
						getSupportActionBar().show();
					}
				}
			});
		}else{
			LogUtil.e("LargePreviewActivity", "bitmap is null");
		}
				
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
        if(isJustPreview){
            return super.onCreateOptionsMenu(menu);
        }else{
            getMenuInflater().inflate(R.menu.main2, menu);
            return true;
        }
	}
	
}
