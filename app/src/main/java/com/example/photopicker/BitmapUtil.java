package com.example.photopicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;

public class BitmapUtil {

	public static Bitmap getBitmap(String filePath) {
        return getBitmap(filePath, null);
	}

	public static Bitmap getBitmap(String filePath, Options options) {
		while (true) {
			try {
				return BitmapFactory.decodeFile(filePath, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 5) {
					return null;
				}
			}
		}
	}

    public static Bitmap getBitmap(byte[] data) {
        return getBitmap(data, null);
    }
	
	public static Bitmap getBitmap(byte[] datas, Options options) {
		while (true) {
			try {
				return BitmapFactory.decodeByteArray(datas, 0, datas.length, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 5) {
					return null;
				}
			}
		}
	}

    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

}
