package com.example.photopicker;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
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
        final String orientation = getIntent().getStringExtra("orientation");
        isJustPreview = getIntent().getBooleanExtra("isJustPreview", true);

		if(TextUtils.isEmpty(filePath) || TextUtils.isEmpty(orientation)){
			Log.e("LargePreviewActivity", "file path is " + filePath);
			finish();
			return;
		}
		
		getSupportActionBar().setTitle(ImagePickerPlusActivity.getSizeStr(new File(filePath).length()));
		getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setShowHideAnimationEnabled(false);

        final ImageView ivt = (ImageView)findViewById(R.id.zivp);
        ivt.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ivt.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Bitmap bp = BitmapUtil.getBitmap(filePath);
                if (bp != null) {
                    try {
                        int orientationInt = Integer.parseInt(orientation);
                        ivt.setImageBitmap(bp);
                        android.graphics.Matrix matrix = new android.graphics.Matrix();
                        matrix.postTranslate(ivt.getWidth()/2 - bp.getWidth()/2, ivt.getHeight()/2 - bp.getHeight()/2);
                        rotate(orientationInt, matrix, ivt);
                        if (orientationInt == 90 || orientationInt == 270) {
                            minZoom(bp.getHeight(), bp.getWidth(), matrix, ivt);
                        } else {
                            minZoom(bp.getWidth(), bp.getHeight(), matrix, ivt);
                        }
                        ivt.setImageMatrix(matrix);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.e("LargePreviewActivity", "bitmap is null");
                }
            }
        });

	}

    private boolean rotate(int orientationInt, android.graphics.Matrix matrix, ImageView iv){
        if(orientationInt > 0 && orientationInt < 360){
            matrix.postRotate(orientationInt, iv.getWidth()/2, iv.getHeight()/2);
            return true;
        }
        return false;
    }

    private void minZoom(int w, int h, android.graphics.Matrix matrix, ImageView iv) {
        float minScaleR = Math.min(
                ((float) iv.getWidth()) / ((float) w),
                ((float) iv.getHeight()) / ((float) h));
        if (minScaleR < 1.0) {
            matrix.postScale(minScaleR, minScaleR, iv.getWidth()/2, iv.getHeight()/2);
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
