package com.example.photopicker;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPickClick(View view) {
        Intent intent = new Intent(this, ImagePickerPlusActivity.class);
        intent.putExtra(ImagePickerPlusActivity.EXTRA_PICK_PHOTO_COUNT, 10); //10张
        intent.putExtra(ImagePickerPlusActivity.EXTRA_DISK_CACHE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> photoIds = data.getStringArrayListExtra(ImagePickerPlusActivity.EXTRA_PICK_RETURN_DATA_IDS);
                    ArrayList<String> photoFilePaths = data.getStringArrayListExtra(ImagePickerPlusActivity.EXTRA_PICK_RETURN_DATA_PATHS);
                    ArrayList<String> photoOrientations = data.getStringArrayListExtra(ImagePickerPlusActivity.EXTRA_PICK_RETURN_DATA_ORIENTATIONS);
                }
                break;
        }
    }

}
