package com.example.photopicker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImagePickerPlusActivity extends ActionBarActivity {

    public static final String EXTRA_PICK_PHOTO_COUNT = "extra_pick_photo_count";
    public static final String EXTRA_DISK_CACHE_PATH = "extra_disk_cache_path";

    public static final String EXTRA_PICK_RETURN_DATA_PATHS = "photoPaths";
    public static final String EXTRA_PICK_RETURN_DATA_IDS = "photoIds";
    public static final String EXTRA_PICK_RETURN_DATA_ORIENTATIONS = "photoOrientations";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isAlbumMode) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
            isAlbumMode = !isAlbumMode;
            adapter.notifyDataSetChanged();
            if (!isAlbumMode) {
                lv.setSelection(0);
            }
            return true;
        } else if (id == R.id.action_finish) {
            setResult(RESULT_OK, new Intent()
                    .putStringArrayListExtra(EXTRA_PICK_RETURN_DATA_PATHS, choicePhotoPaths)
                    .putStringArrayListExtra(EXTRA_PICK_RETURN_DATA_IDS, choicePhotoIds)
                    .putStringArrayListExtra(EXTRA_PICK_RETURN_DATA_ORIENTATIONS, choicePhotoOrientations));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private MenuItem menuItem;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.findItem(R.id.action_finish);
        menuItem.setTitle("完成(0/" + CAN_CHECK_COUNT + ")");
        menuItem.setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }

    private static final String TAG = "ImagePickerPlusActivity";
    private boolean isAlbumMode = true;
    private BaseAdapter adapter;
    private boolean flag = true;
    private Handler mHandler;

    private ArrayList<String> choicePhotoPaths = new ArrayList<>();
    private ArrayList<String> choicePhotoIds = new ArrayList<>();
    private ArrayList<String> choicePhotoOrientations = new ArrayList<>();

    private int CAN_CHECK_COUNT;
    private LongSparseArray<AlbumInfo> itemAlbumDatas;
    private ListView lv;
    private int imgViewWidthAndHeight;

    private long clickAlbumId;
    private View clickItemView;

    private String diskCachePath;

    @Override
    protected void onDestroy() {
        flag = false;
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isAlbumMode) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
            isAlbumMode = !isAlbumMode;
            adapter.notifyDataSetChanged();
            if (!isAlbumMode) {
                lv.setSelection(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker_plus_layout);

        diskCachePath = getIntent().getStringExtra(EXTRA_DISK_CACHE_PATH);
        if(!TextUtils.isEmpty(diskCachePath)){
            new File(diskCachePath).mkdirs();
        }
        CAN_CHECK_COUNT = getIntent().getIntExtra(EXTRA_PICK_PHOTO_COUNT, 0);
        if (CAN_CHECK_COUNT <= 0) {
            LogUtil.e(TAG, "pick_count from intent is 0");
            finish();
            return;
        }

        getSupportActionBar().setTitle("选择图片");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setShowHideAnimationEnabled(false);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

        imgViewWidthAndHeight = outMetrics.widthPixels / 3 - 4;

        itemAlbumDatas = getAdapterDatas();

        lv = (ListView) findViewById(R.id.list_view);

        final LoadPhonePhotoThread t = new LoadPhonePhotoThread();
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                ImageView imgView = (ImageView) msg.obj;
                Bitmap b = msg.getData().getParcelable("bitmap");
                Long msgId = msg.getData().getLong("imgId");
                Long nowMsgId = (Long) imgView.getTag();
                if (msgId.longValue() == nowMsgId.longValue()) {
                    if(null != b){
                        imgView.setImageBitmap(b);
                    }else{
                        imgView.setImageDrawable(null);
                    }
                } else {
                    LogUtil.w(TAG, "last tag imgId != now tag imgId, 重新排序");
                    imgView.setImageDrawable(null);
                    t.addTask(null, imgView, null, null);
                }
            }
        };

        adapter = new BaseAdapter() {

            private int size = itemAlbumDatas.size();
            private LayoutInflater layoutInfalter = getLayoutInflater();

            private void setLayoutHeight(ImageView imageView) {
                ViewGroup.LayoutParams vl = imageView.getLayoutParams();
                if (vl == null) {
                    vl = new ViewGroup.LayoutParams(imgViewWidthAndHeight, imgViewWidthAndHeight);
                }
                vl.width = imgViewWidthAndHeight;
                vl.height = imgViewWidthAndHeight;
                imageView.setLayoutParams(vl);
                imageView.setImageBitmap(null);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = layoutInfalter.inflate(R.layout.list_grid_item, parent, false);
                }

                ImageView imageView = (ImageView) view.findViewById(R.id.image_item);
                ImageView imageView2 = (ImageView) view.findViewById(R.id.image_item_2);
                ImageView imageView3 = (ImageView) view.findViewById(R.id.image_item_3);

                setLayoutHeight(imageView);
                setLayoutHeight(imageView2);
                setLayoutHeight(imageView3);

                TextView tv = (TextView) view.findViewById(R.id.tv_info);
                TextView tv2 = (TextView) view.findViewById(R.id.tv_info2);
                TextView tv_2 = (TextView) view.findViewById(R.id.tv_info_2);
                TextView tv2_2 = (TextView) view.findViewById(R.id.tv_info2_2);
                TextView tv_3 = (TextView) view.findViewById(R.id.tv_info_3);
                TextView tv2_3 = (TextView) view.findViewById(R.id.tv_info2_3);

                View bottomCt = view.findViewById(R.id.bottom_ct);
                View bottomCt2 = view.findViewById(R.id.bottom_ct_2);
                View bottomCt3 = view.findViewById(R.id.bottom_ct_3);

                View allCt = view.findViewById(R.id.all_ct);
                View allCt2 = view.findViewById(R.id.all_ct2);
                View allCt3 = view.findViewById(R.id.all_ct3);

                allCt.setOnClickListener(onItemClick);
                allCt2.setOnClickListener(onItemClick);
                allCt3.setOnClickListener(onItemClick);

                CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox);
                CheckBox cb2 = (CheckBox) view.findViewById(R.id.checkbox_2);
                CheckBox cb3 = (CheckBox) view.findViewById(R.id.checkbox_3);

                TextView blackBg = (TextView) view.findViewById(R.id.tv_checked_bg);
                TextView blackBg2 = (TextView) view.findViewById(R.id.tv_checked_bg2);
                TextView blackBg3 = (TextView) view.findViewById(R.id.tv_checked_bg3);

                cb.setTag(R.string.view_tag_key, blackBg);
                cb2.setTag(R.string.view_tag_key, blackBg2);
                cb3.setTag(R.string.view_tag_key, blackBg3);

                allCt.setTag(R.string.view_tag_key, R.id.checkbox);
                allCt2.setTag(R.string.view_tag_key, R.id.checkbox_2);
                allCt3.setTag(R.string.view_tag_key, R.id.checkbox_3);

                if (isAlbumMode) {

                    cb.setVisibility(View.GONE);
                    cb2.setVisibility(View.GONE);
                    cb3.setVisibility(View.GONE);

                    blackBg.setVisibility(View.GONE);
                    blackBg2.setVisibility(View.GONE);
                    blackBg3.setVisibility(View.GONE);

                    bottomCt.setVisibility(View.VISIBLE);
                    bottomCt2.setVisibility(View.VISIBLE);
                    bottomCt3.setVisibility(View.VISIBLE);

                    AlbumInfo albumInfo;
                    ItemImageInfo imgInfo;
                    int dataSize = itemAlbumDatas.size();

                    if (position * 3 < dataSize) {
                        albumInfo = itemAlbumDatas.valueAt(position * 3);
                        imgInfo = albumInfo.imageInfos.get(0);
                        t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                        bottomCt.setVisibility(View.VISIBLE);
                        tv.setText(albumInfo.albumName);
                        tv2.setText("(" + albumInfo.photoCount + ")");
                        if (albumInfo.choiceCount > 0) {
                            blackBg.setText("已选" + albumInfo.choiceCount + "张");
                            blackBg.setVisibility(View.VISIBLE);
                        }
                        allCt.setTag(albumInfo.albumId);
                        allCt.setVisibility(View.VISIBLE);
                    } else {
                        allCt.setVisibility(View.INVISIBLE);
                    }

                    if (position * 3 + 1 < dataSize) {
                        albumInfo = itemAlbumDatas.valueAt(position * 3 + 1);
                        imgInfo = albumInfo.imageInfos.get(0);
                        t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                        bottomCt2.setVisibility(View.VISIBLE);
                        tv_2.setText(albumInfo.albumName);
                        tv2_2.setText("(" + albumInfo.photoCount + ")");
                        if (albumInfo.choiceCount > 0) {
                            blackBg2.setText("已选" + albumInfo.choiceCount + "张");
                            blackBg2.setVisibility(View.VISIBLE);
                        }
                        allCt2.setTag(albumInfo.albumId);
                        allCt2.setVisibility(View.VISIBLE);
                    } else {
                        allCt2.setVisibility(View.INVISIBLE);
                    }

                    if (position * 3 + 2 < dataSize) {
                        albumInfo = itemAlbumDatas.valueAt(position * 3 + 2);
                        imgInfo = albumInfo.imageInfos.get(0);
                        t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                        bottomCt3.setVisibility(View.VISIBLE);
                        tv_3.setText(albumInfo.albumName);
                        tv2_3.setText("(" + albumInfo.photoCount + ")");
                        if (albumInfo.choiceCount > 0) {
                            blackBg3.setText("已选" + albumInfo.choiceCount + "张");
                            blackBg3.setVisibility(View.VISIBLE);
                        }
                        allCt3.setTag(albumInfo.albumId);
                        allCt3.setVisibility(View.VISIBLE);
                    } else {
                        allCt3.setVisibility(View.INVISIBLE);
                    }

                } else {

                    cb.setVisibility(View.VISIBLE);
                    cb2.setVisibility(View.VISIBLE);
                    cb3.setVisibility(View.VISIBLE);

                    cb.setOnCheckedChangeListener(null);
                    cb2.setOnCheckedChangeListener(null);
                    cb3.setOnCheckedChangeListener(null);

                    bottomCt.setVisibility(View.GONE);
                    bottomCt2.setVisibility(View.GONE);
                    bottomCt3.setVisibility(View.GONE);

                    AlbumInfo albumInfo = itemAlbumDatas.get(clickAlbumId);
                    ItemImageInfo imgInfo;
                    int dataSize = albumInfo.imageInfos.size();

                    if (position * 3 < dataSize) {
                        imgInfo = albumInfo.imageInfos.get(position * 3);
                        t.addTask(imgInfo.filePath, imageView, imgInfo.imageId, imgInfo.orientation);
                        allCt.setVisibility(View.VISIBLE);
                        allCt.setTag(imgInfo.filePath);
                        allCt.setTag(R.string.view_tag_key2, imgInfo.orientation);
                        cb.setTag(position * 3);
                        if (imgInfo.isChecked) {
                            cb.setChecked(true);
                            blackBg.setVisibility(View.VISIBLE);
                            blackBg.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                        } else {
                            cb.setChecked(false);
                            blackBg.setVisibility(View.GONE);
                        }
                    } else {
                        allCt.setVisibility(View.INVISIBLE);
                    }

                    if (position * 3 + 1 < dataSize) {
                        imgInfo = albumInfo.imageInfos.get(position * 3 + 1);
                        t.addTask(imgInfo.filePath, imageView2, imgInfo.imageId, imgInfo.orientation);
                        allCt2.setVisibility(View.VISIBLE);
                        allCt2.setTag(imgInfo.filePath);
                        allCt2.setTag(R.string.view_tag_key2, imgInfo.orientation);
                        cb2.setTag(position * 3 + 1);
                        if (imgInfo.isChecked) {
                            cb2.setChecked(true);
                            blackBg2.setVisibility(View.VISIBLE);
                            blackBg2.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                        } else {
                            cb2.setChecked(false);
                            blackBg2.setVisibility(View.GONE);
                        }
                    } else {
                        allCt2.setVisibility(View.INVISIBLE);
                    }

                    if (position * 3 + 2 < dataSize) {
                        imgInfo = albumInfo.imageInfos.get(position * 3 + 2);
                        t.addTask(imgInfo.filePath, imageView3, imgInfo.imageId, imgInfo.orientation);
                        allCt3.setVisibility(View.VISIBLE);
                        allCt3.setTag(imgInfo.filePath);
                        allCt3.setTag(R.string.view_tag_key2, imgInfo.orientation);
                        cb3.setTag(position * 3 + 2);
                        if (imgInfo.isChecked) {
                            cb3.setChecked(true);
                            blackBg3.setVisibility(View.VISIBLE);
                            blackBg3.setText(getSizeStr(imgInfo.size, imgInfo.filePath));
                        } else {
                            cb3.setChecked(false);
                            blackBg3.setVisibility(View.GONE);
                        }
                    } else {
                        allCt3.setVisibility(View.INVISIBLE);
                    }

                    cb.setOnCheckedChangeListener(onCheck);
                    cb2.setOnCheckedChangeListener(onCheck);
                    cb3.setOnCheckedChangeListener(onCheck);

                }

                return view;
            }

            private View.OnClickListener onItemClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object o = v.getTag();
                    if (o instanceof String) {
                        clickItemView = v;
                        String path = (String) o;
                        Intent intent = new Intent(ImagePickerPlusActivity.this, LargePreviewActivity.class);
                        intent.putExtra("filePath", path);
                        String orientation = (String) v.getTag(R.string.view_tag_key2);
                        intent.putExtra("orientation", orientation);
                        intent.putExtra("isJustPreview", false);
                        startActivityForResult(intent, CODE_LARGE_PREVIEW);
                    } else if (o instanceof Long) {
                        clickAlbumId = (Long) o;
                        isAlbumMode = !isAlbumMode;
                        adapter.notifyDataSetChanged();
                        if (!isAlbumMode) {
                            lv.setSelection(0);
                        }
                    }
                }
            };

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public void notifyDataSetChanged() {
                t.clearTaskAndCache();
                if (isAlbumMode) {
                    size = itemAlbumDatas.size();
                } else {
                    size = itemAlbumDatas.get(clickAlbumId).imageInfos.size();
                }
                super.notifyDataSetChanged();
            }

            @Override
            public int getCount() {
                if (size % 3 == 0) {
                    return size / 3;
                } else {
                    return size / 3 + 1;
                }
            }

        };
        lv.setAdapter(adapter);

    }

    public static String getSizeStr(long size) {
        if (size > 0) {
            String imgSize;
            double temp = Double.valueOf(size);
            temp = temp / 1024.0d; //KB
            if (temp > 1024.0d) { // > 1MB
                temp = temp / 1024.0d; //MB
                String sizeStr = String.valueOf(temp);
                imgSize = sizeStr.substring(0, sizeStr.indexOf(".") + 2) + " MB";
            } else {
                long temp2 = Math.round(temp);
                imgSize = (temp2 > 0 ? temp2 : 1) + " KB";
            }
            return imgSize;
        } else {
            LogUtil.e(TAG, "param size <= 0 , size : " + size);
            return "";
        }
    }

    public static String getSizeStr(long size, String filePath){
        String value = getSizeStr(size);
        if(TextUtils.isEmpty(value) && !TextUtils.isEmpty(filePath)){
            value = getSizeStr(new File(filePath).length());
        }
        return value;
    }

    private CompoundButton.OnCheckedChangeListener onCheck = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (choicePhotoPaths.size() >= CAN_CHECK_COUNT && isChecked) {
                buttonView.setOnCheckedChangeListener(null);
                buttonView.setChecked(false);
                buttonView.setOnCheckedChangeListener(onCheck);
                Toast.makeText(getApplicationContext(), "最多选择" + CAN_CHECK_COUNT + "张", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer position = (Integer) buttonView.getTag();
            if (null != position) {
                AlbumInfo albumInfo = itemAlbumDatas.get(clickAlbumId);
                ItemImageInfo imgItemInfo = albumInfo.imageInfos.get(position);
                TextView tvBg = (TextView) buttonView.getTag(R.string.view_tag_key);
                if (isChecked) {
                    tvBg.setVisibility(View.VISIBLE);
                    tvBg.setText(getSizeStr(imgItemInfo.size, imgItemInfo.filePath));
                    choicePhotoPaths.add(imgItemInfo.filePath);
                    choicePhotoIds.add(String.valueOf(imgItemInfo.imageId));
                    choicePhotoOrientations.add(imgItemInfo.orientation);
                    albumInfo.choiceCount++;
                } else {
                    tvBg.setVisibility(View.GONE);
                    choicePhotoPaths.remove(imgItemInfo.filePath);
                    choicePhotoIds.remove(String.valueOf(imgItemInfo.imageId));
                    choicePhotoOrientations.remove(imgItemInfo.orientation);
                    albumInfo.choiceCount--;
                }
                menuItem.setTitle("完成 (" + choicePhotoPaths.size() + "/" + CAN_CHECK_COUNT + ")");
                long choicePhotoSize;
                if ((choicePhotoSize = choicePhotoPaths.size()) > 0) {
                    menuItem.setEnabled(true);
                } else {
                    menuItem.setEnabled(false);
                }
                imgItemInfo.isChecked = isChecked;

                long allFileSize = 0;
                for (int i = 0; i < choicePhotoSize; i++) {
                    allFileSize = allFileSize + new File(choicePhotoPaths.get(i)).length();
                }
                String title = getSizeStr(allFileSize);
                if (!TextUtils.isEmpty(title)) {
                    getSupportActionBar().setTitle("选择图片 (" + title + ")");
                } else {
                    getSupportActionBar().setTitle("选择图片");
                }
            }
        }
    };

    public static final int CODE_LARGE_PREVIEW = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_LARGE_PREVIEW:
                if (resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra("filePath");
                    if (choicePhotoPaths.contains(filePath)) {
                        return;
                    }
                    Integer cbResId = (Integer) clickItemView.getTag(R.string.view_tag_key);
                    if (null != cbResId) {
                        CheckBox checkBox = (CheckBox) clickItemView.findViewById(cbResId);
                        checkBox.setChecked(true);
                    }
                }
                break;
        }
    }

    class LoadPhonePhotoThread extends Thread {

        private ConcurrentLinkedQueue<ImageView> imgViews = new ConcurrentLinkedQueue<>();
        private LongSparseArray<String> thumbnailsMap = new LongSparseArray<>();
        private Options options = new Options();
        private android.graphics.Matrix matrix = new android.graphics.Matrix();

        public LoadPhonePhotoThread() {
            super();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        public void clearTaskAndCache() {
            imgViews.clear();
        }

        public void addTask(String filePath, ImageView imgView, Long imgId, String orientation) {
            if (imgView != null) {
                synchronized (imgView) {
                    if(null != filePath && null != imgId && null != orientation){
                        imgView.setTag(imgId);
                        imgView.setTag(R.string.view_tag_key, filePath);
                        imgView.setTag(R.string.view_tag_key2, orientation);
                    }
                }
                if (imgViews.contains(imgView)) {
                    imgViews.remove(imgView);
                }
                imgViews.add(imgView);
            }
        }

        @Override
        public void run() {
            String[] projection = {Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA};
            Cursor cursor = getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
                    Thumbnails.KIND + "=?", new String[]{String.valueOf(Thumbnails.MINI_KIND)}, null);
            while (cursor.moveToNext()) {
                thumbnailsMap.put(cursor.getLong(1), cursor.getString(2));
            }
            cursor.close();
            while (flag) {
                if (!imgViews.isEmpty()) {
                    ImageView imgView = imgViews.poll();
                    if (imgView == null) {
                        continue;
                    }
                    Long imgId;
                    String tagFilePath;
                    String orientation;
                    synchronized (imgView) {
                        imgId = (Long) imgView.getTag();
                        tagFilePath = (String) imgView.getTag(R.string.view_tag_key);
                        orientation = (String) imgView.getTag(R.string.view_tag_key2);
                    }
                    Bitmap b = null;
                    if (b == null) { //get mini from system diskcache
                        b = getSystemMiniFromSystemDiskCache(imgId);
                    }
                    if(b == null){ //my mini from my diskcache
                        b = getMyMiniFromMyDiskCache(tagFilePath);
                    }
                    //my mini from system ori and save to my diskcache
                    if (b == null) {
                        b = getMyMiniFromSystemOri(tagFilePath);
                    }
                    //gen system mini by system and save to system diskcache、db.
                    if (b == null) {
                        //性能很差
                        b = getSystemMiniFromSystem(imgId);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("bitmap", b);
                    bundle.putLong("imgId", imgId);
                    if (b != null) {
                        try {
                            int o = Integer.parseInt(orientation);
                            if (o > 0 && o < 360) {
                                matrix.reset();
                                matrix.setRotate(o);
                                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
                            }
                            bundle.putParcelable("bitmap", b);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LogUtil.e(TAG, "get small bitmap fail ! " + b);
                    }
                    Message msg = mHandler.obtainMessage(0, imgView);
                    msg.setData(bundle);
                    msg.sendToTarget();
                }
            }
            clearTaskAndCache();
        }

        private Bitmap getSystemMiniFromSystemDiskCache(long imgId){
            String thumbnailFilePath = thumbnailsMap.get(imgId);
            if (!TextUtils.isEmpty(thumbnailFilePath)) {
                File miniFile = new File(thumbnailFilePath);
                if(miniFile.exists() && miniFile.length() > 0){
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(thumbnailFilePath, options);
                    if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                        final float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                        options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
                    }
                    options.inJustDecodeBounds = false;
                    return BitmapUtil.getBitmap(thumbnailFilePath, options);
                }else{
                    LogUtil.w(TAG, "1.mini file is not exists");
                }
            } else {
                LogUtil.w(TAG, "1.mini filePath is null");
            }
            return null;
        }

        private Bitmap getMyMiniFromMyDiskCache(String oriFilePath){
            if(!TextUtils.isEmpty(diskCachePath) && !TextUtils.isEmpty(oriFilePath)){
                Bitmap b = BitmapUtil.getBitmap(new File(diskCachePath, new File(oriFilePath).getName().split("\\.")[0]).getAbsolutePath());
                if(b == null){
                    LogUtil.w(TAG, "2.get my mini from my diskcache fail.");
                }else{
                    LogUtil.i(TAG, "2.get my mini from my diskcache succ.");
                }
                return b;
            }
            return null;
        }

        private Bitmap getSystemMiniFromSystem(long imgId){
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            Thumbnails.getThumbnail(getContentResolver(), imgId,
                    Thumbnails.MINI_KIND, options);
            if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                final float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
            }
            options.inJustDecodeBounds = false;
            Bitmap bm = Thumbnails.getThumbnail(getContentResolver(), imgId, Thumbnails.MINI_KIND, options);
            if(bm != null && TextUtils.isEmpty(thumbnailsMap.get(imgId))){
                LogUtil.i(TAG, "4.gen mini and save path.");
                Cursor c = Thumbnails.queryMiniThumbnail(getContentResolver(), imgId, Thumbnails.MINI_KIND, new String[]{Thumbnails._ID, Thumbnails.DATA});
                if(null != c){
                    if(c.moveToFirst()){
                        thumbnailsMap.put(imgId, c.getString(1));
                    }else{
                        LogUtil.e(TAG, "4.query mini path fail !");
                    }
                    c.close();
                }else{
                    LogUtil.e(TAG, "4.query mini cursor is null");
                }
            }
            return bm;
        }

        private Bitmap getMyMiniFromSystemOri(String filePath) {
            if (!TextUtils.isEmpty(filePath)) {
                options.inJustDecodeBounds = true;
                options.inSampleSize = 1;
                BitmapFactory.decodeFile(filePath, options);
                if(options.outWidth*options.outHeight >= 1600 * 1200){
                    if(TextUtils.isEmpty(diskCachePath)){
                        return null;
                    }
                }
                if (options.outWidth > imgViewWidthAndHeight || options.outHeight > imgViewWidthAndHeight) {
                    float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                    options.inSampleSize = Math.round(maxBitmapBorder / ((float) imgViewWidthAndHeight));
                }
                options.inJustDecodeBounds = false;
                Bitmap bm = BitmapUtil.getBitmap(filePath, options);
                if(null != bm){
                    FileOutputStream fos = null;
                    try {
                        LogUtil.i(TAG, "mini from ori saving to diskcache");
                        fos = new FileOutputStream(new File(diskCachePath, new File(filePath).getName().split("\\.")[0]));
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if(null != fos){
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if(bm == null){
                    LogUtil.w(TAG, "3.ori file maybe too big");
                }
                return bm;
            } else {
                LogUtil.e(TAG, "3.ori filePath is null");
                return null;
            }
        }

    }

    private LongSparseArray<AlbumInfo> getAdapterDatas() {
        LongSparseArray<AlbumInfo> itemAlbumDatas = new LongSparseArray<AlbumInfo>();
        String[] projection2 = {Media._ID, Media.SIZE, Media.DATA, Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME,
                Media.DISPLAY_NAME, Media.ORIENTATION};
        Cursor cursor2 = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection2, null,
                null, Media.DATE_TAKEN + " DESC, " + Media._ID + " DESC");
        while (cursor2.moveToNext()) {
            long Media_BUCKET_ID = cursor2.getLong(3);
            AlbumInfo albumInfo = itemAlbumDatas.get(Media_BUCKET_ID);
            if (albumInfo == null) {
                albumInfo = new AlbumInfo();
                albumInfo.albumId = Media_BUCKET_ID;
                albumInfo.albumName = cursor2.getString(4);
                itemAlbumDatas.put(Media_BUCKET_ID, albumInfo);
            }
            ItemImageInfo imageInfo = new ItemImageInfo();
            imageInfo.imageId = cursor2.getLong(0);
            imageInfo.size = cursor2.getLong(1);
            imageInfo.filePath = cursor2.getString(2);
            imageInfo.orientation = new String(String.valueOf(cursor2.getInt(6)));

            albumInfo.photoCount++;
            albumInfo.imageInfos.add(imageInfo);

        }
        cursor2.close();
        return itemAlbumDatas;
    }

}
