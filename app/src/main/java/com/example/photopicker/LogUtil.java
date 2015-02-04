package com.example.photopicker;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {
	
	public static final class DZQConfig{
		public static final boolean isPrintLog = true;
	}
	
    /**
     * 普通信息
     * 
     * @param tag
     * @param msg
     * @param e
     */
    public static void v(String tag, String msg, Throwable e) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg) && e == null) {
                Log.v(tag, msg);
            } else if (!TextUtils.isEmpty(msg) && e != null) {
                Log.v(tag, msg, e);
            }
        }
    }

    /**
     * 调试信息
     * 
     * @param tag
     * @param msg
     * @param e
     */
    public static void d(String tag, String msg, Throwable e) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg) && e == null) {
                Log.d(tag, msg);
            } else if (!TextUtils.isEmpty(msg) && e != null) {
                Log.d(tag, msg, e);
            }
        }
    }

    /**
     * 重要信息
     * 
     * @param tag
     * @param msg
     * @param e
     */
    public static void i(String tag, String msg, Throwable e) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg) && e == null) {
                Log.i(tag, msg);
            } else if (!TextUtils.isEmpty(msg) && e != null) {
                Log.i(tag, msg, e);
            }
        }
    }

    /**
     * 意料之中的较严重异常
     * 
     * @param tag
     * @param msg
     * @param e
     */
    public static void w(String tag, String msg, Throwable e) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg) && e == null) {
                Log.w(tag, msg);
            } else if (!TextUtils.isEmpty(msg) && e != null) {
                Log.w(tag, msg, e);
            }
        }
    }

    /**
     * 意料之外的严重异常
     * 
     * @param tag
     * @param msg
     * @param e
     */
    public static void e(String tag, String msg, Throwable e) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg) && e == null) {
                Log.e(tag, msg);
            } else if (!TextUtils.isEmpty(msg) && e != null) {
                Log.e(tag, msg, e);
            }
        }
    }

    /**
     * 普通信息
     * 
     * @param tag
     * @param msg
     */
    public static void v(String tag, String msg) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg)) {
                Log.v(tag, msg);
            }
        }
    }

    /**
     * 调试信息
     * 
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg)) {
                Log.d(tag, msg);
            }
        }
    }

    /**
     * 重要信息
     * 
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg)) {
                Log.i(tag, msg);
            }
        }
    }

    /**
     * 意料之中的较严重事件
     * 
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg)) {
                Log.w(tag, msg);
            }
        }
    }

    /**
     * 意料之外的严重事件
     * 
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if (DZQConfig.isPrintLog && !TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(msg)) {
                Log.e(tag, msg);
            }
        }
    }

}
