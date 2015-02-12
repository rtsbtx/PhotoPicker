package com.example.photopicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;

public class BitmapUtil {

    public static Bitmap getResizeBitmap(Context context, String imgSysId, String filePath, int orientation, int itemWidth) throws Exception {

        if (imgSysId == null && filePath == null) {
            return null;
        }

        Options options = new Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //<<====================
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(filePath, options);
        if (options.outWidth > itemWidth || options.outHeight > itemWidth) {
            float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
            options.inSampleSize = Math.round(maxBitmapBorder / ((float) itemWidth));
        }
        options.inJustDecodeBounds = false;
        Bitmap bp = BitmapUtil.getBitmap(filePath, options);

        if (orientation > 0 && orientation < 360 && bp != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(orientation);
            bp = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, false);
        }
        //==================>>

        if (bp != null) {
            return bp;
        }

        //<<============
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                Long.valueOf(imgSysId), MediaStore.Images.Thumbnails.MINI_KIND, options);
        if (options.outWidth > itemWidth || options.outHeight > itemWidth) {
            float maxBitmapBorder = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
            options.inSampleSize = Math.round(maxBitmapBorder / ((float) itemWidth));
        }
        options.inJustDecodeBounds = false;
        int exceptionCount = 0;
        while (exceptionCount < 3) {
            try {
                bp = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                        Long.valueOf(imgSysId), MediaStore.Images.Thumbnails.MINI_KIND, options);
                break;
            } catch (Exception e) {
                exceptionCount++;
                options.inSampleSize++;
                System.gc();
            }
        }
        //==============>>

        if (orientation > 0 && orientation < 360 && bp != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(orientation);
            bp = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, false);
        }

        return bp;
    }

	public static Bitmap getBitmap(String filePath) {

		Options options = new Options();
		options.inSampleSize = 1;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		while (true) {
			try {
				return BitmapFactory.decodeFile(filePath, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 4) {
					return null;
				}
			}
		}
	}

	public static Bitmap getBitmap(String filePath, Options options) {
		while (true) {
			try {
				return BitmapFactory.decodeFile(filePath, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 4) {
					return null;
				}
			}
		}
	}
	
	public static Bitmap getBitmap(byte[] datas, Options options) {
		while (true) {
			try {
				return BitmapFactory.decodeByteArray(datas, 0, datas.length, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 4) {
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

	public static Bitmap getBitmap(byte[] data) {

		Options options = new Options();
		options.inSampleSize = 1;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		while (true) {
			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length, options);
			} catch (Exception e) {
				LogUtil.w("BitmapUtil", "fucking oom", e);
				options.inSampleSize++;
				System.gc();
				if (options.inSampleSize > 4) {
					return null;
				}
			}
		}
	}

}
