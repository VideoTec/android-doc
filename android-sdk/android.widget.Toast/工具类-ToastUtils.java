package com.feinno.beside.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.fetion.R;

import com.feinno.beside.application.BesideInitUtil;

public class ToastUtils {

	private static Toast toast = null;

	private static Handler mHandler = null;

	public static int mDuration = Toast.LENGTH_LONG;

	public static String mMsg = null;
	
	public static View mView = null;
	
	@SuppressLint("ShowToast")
	private static void initToast() {
		if (toast == null) {
			mHandler = new Handler(Looper.getMainLooper());
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					toast = Toast.makeText(BesideInitUtil.getBesideInitUtilInstance().getApplication(), "", Toast.LENGTH_SHORT);
				}
			});
		}
	}

	private static void show(Context context, final int duration, final String msg) {
		initToast();
		mDuration = duration;
		mMsg = msg;
		mView = LayoutInflater.from(context).inflate(R.layout.beside_toast_help_msg, null);
		((TextView)mView.findViewById(R.id.help_toast_msg_id)).setText(mMsg);
		Runnable showRunnable = new Runnable() {

			@Override
			public void run() {
				if (toast != null) {
					toast.setDuration(mDuration);
					toast.setView(mView);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		};
		mHandler.removeCallbacks(showRunnable);
		mHandler.postDelayed(showRunnable, 100);
	}
	

	public static void showShortToast(Context context, int resID) {
		//增加判断，防止卸载SD卡时mApplication为空，程序崩溃
		if(BesideInitUtil.getBesideInitUtilInstance().getApplication() != null){
			try {
				initToast();
				show(context,Toast.LENGTH_SHORT,BesideInitUtil.getBesideInitUtilInstance().getString(resID));
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	public static void showShortToast(Context context, String msg) {
		initToast();
		show(context,Toast.LENGTH_SHORT, msg);
	}

	public static void showLongToast(Context context, int resID) {
		initToast();
		show(context,Toast.LENGTH_LONG, BesideInitUtil.getBesideInitUtilInstance().getString(resID));
	}
	
	public static void showLongToast(Context context, String msg) {
		initToast();
		show(context,Toast.LENGTH_LONG, msg);
	}

	private static void toastShow(final String content) {
		initToast();
		show(BesideInitUtil.getBesideInitUtilInstance().getApplication().getApplicationContext(),Toast.LENGTH_LONG, content);
	}

	public static void toast(String content) {
		toastShow(content);
	}

	public static void toast(int resId) {
		toastShow(BesideInitUtil.getBesideInitUtilInstance().getString(resId));
	}

}
