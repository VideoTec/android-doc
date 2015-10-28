package com.feinno.beside.ui.view;

import com.feinno.beside.ui.view.expression.FetionExpressionViewPager;
import com.feinno.beside.utils.ToastUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.fetion.R;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CopyTextView extends TextView {
	
	private boolean mNeedLongClickFlag = false; //纯文本进详情长按复制失效
	
	public CopyTextView(Context context) {
		super(context);
	}
	public CopyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public CopyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	public PopupWindow initPopupWindow(final Activity activity){
		View view = activity.getLayoutInflater().inflate(R.layout.beside_detail_copy_menu_pop, null);
		TextView copyMenuItem = (TextView) view.findViewById(R.id.menu_copy_item);
		
		final PopupWindow copyPopupWindow = new PopupWindow(view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		copyPopupWindow.setContentView(view);  
		copyPopupWindow.setBackgroundDrawable(new PaintDrawable(android.R.color.transparent));
        // 设置PopupWindow外部区域是否可触摸  
		copyPopupWindow.setFocusable(true); //设置PopupWindow可获得焦点  
		copyPopupWindow.setTouchable(true); //设置PopupWindow可触摸  
		copyPopupWindow.setOutsideTouchable(true); //设置非PopupWindow区域可触摸  
		
		copyPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				//在popwindow消失时，文字背景还原
//				CopyTextView.this.setBackgroundResource(android.R.color.white);
				setBackgroundDrawable(mDrawable);
			}
		});
		
		copyMenuItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//将动态内容放入剪切板
				String copyText = CopyTextView.this.getText().toString();
				ClipboardManager clipboardManager = (ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
				//设置剪切板的内容
				//api 11以下不适用
				//clipboardManager.setPrimaryClip(ClipData.newPlainText("lable", copyText));
				//恢复一下原来背景，否则当手指在控件外响应up事件时，up事件恢复背景不执行
				setBackgroundDrawable(mDrawable);
				clipboardManager.setText(copyText);
				ToastUtils.showShortToast(activity, "["+copyText+"]已复制！");
				copyPopupWindow.dismiss();
			}
		});
		return copyPopupWindow;
	}
	private Drawable mDrawable;
	public void setIsCopyable(Activity activity,boolean isCopyable,final FetionExpressionViewPager mViewPager){
		if (isCopyable) {
			mNeedLongClickFlag = true;
			final PopupWindow copyPopupWindow = initPopupWindow(activity);
			setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
				    if (null != mViewPager) {
				        mViewPager.setVisibility(View.GONE);
				    }
					if (getText() == null || getText().length() == 0) {
						return false;
					}
					mDrawable = getBackground();
					setBackgroundResource(android.R.color.darker_gray);
					WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
					int height = wm.getDefaultDisplay().getHeight();
					copyPopupWindow.showAtLocation(CopyTextView.this,Gravity.NO_GRAVITY, 5, height / 5);
					return false;
				}
			});
		}
	}
	
	/**
	 * 解决ListView的onItemClick和Item布局中TextView超链接点击事件冲突问题
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean flag = false;
		MovementMethod mm = getMovementMethod();
		if (!mNeedLongClickFlag && mm != null && mm instanceof LinkMovementMethod
				&& !TextUtils.isEmpty(getText())
				&& getText() instanceof Spannable) {
			Spannable buffer = (Spannable)getText();
			int action = event.getAction();
			if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_DOWN) {
				int x = (int) event.getX();
				int y = (int) event.getY();
				x -= getTotalPaddingLeft();
				y -= getTotalPaddingTop();
				x += getScrollX();
				y += getScrollY();
				Layout layout = getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);
				ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
				int linkLength = link.length;
				if (linkLength > 0) {
					flag = true;
				} else {
					flag = false;
				}
			}
		}
		if (mNeedLongClickFlag || flag) {
			return super.onTouchEvent(event);
		}
		return flag;
	}
}
