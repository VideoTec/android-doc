package com.feinno.beside.chatroom.ui.dialog;

import com.feinno.beside.chatroom.logic.ChatRoomManager;
import com.feinno.beside.manager.BesideEngine;
import com.feinno.beside.ui.activity.personal.PersonalPageActivity;
import com.feinno.beside.utils.ToastUtils;
import com.feinno.beside.utils.fetion.Account;
import com.feinno.beside.utils.log.LogSystem;

import cn.com.fetion.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ChatRoomDialog extends AlertDialog implements android.view.View.OnClickListener {

	private static final String TAG = ChatRoomDialog.class.getSimpleName();
	private LinearLayout mLookInfoLl;
	private TextView mLookInfoTV;
	private LinearLayout mUnlikeTaLl;

	private android.view.View.OnClickListener mListener;
	private Context mContext;
	private long uid;
	private String nickname;
	private TextView mCancleTV;

	public ChatRoomDialog(Context context) {
		super(context);
		mContext = context;
	}

	public void setUserinfo(long uid, String nickname) {
		this.uid = uid;
		this.nickname = nickname;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beside_dialog_popmenu_chatroom_portrait);
		initView();
	}

	private void initView() {
		getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.dimAmount = 0.5f;
		getWindow().setAttributes(params);

		mLookInfoLl = (LinearLayout) findViewById(R.id.beside_dialog_popupmenu_chatroom_menu_lookinfo_ll);
		mLookInfoLl.setOnClickListener(this);
		mLookInfoTV = (TextView) findViewById(R.id.beside_dialog_popupmenu_chatroom_menu_lookinfo_id);
		mUnlikeTaLl = (LinearLayout) findViewById(R.id.beside_dialog_popupmenu_chatroom_menu_unlike_ll);
		mUnlikeTaLl.setOnClickListener(this);
		mCancleTV = (TextView) findViewById(R.id.broadcast_detail_popmenu_cancle_id);
		mCancleTV.setOnClickListener(this);
	
	}

	@Override
	public void onClick(View v) {
		ChatRoomManager manager = BesideEngine.getEngine(mContext).getManager(ChatRoomManager.class);
		if (v.getId() == R.id.beside_dialog_popupmenu_chatroom_menu_lookinfo_ll) { // 查看资料
			LogSystem.d(TAG, "uid --->" + uid );
			if (manager.minSendMessageNumber == 0) {
				Intent intent = new Intent();
				intent.putExtra(PersonalPageActivity.EXTRA_USERID, uid);
				intent.setClass(mContext, PersonalPageActivity.class);
				mContext.startActivity(intent);
			}else {
				ToastUtils.showShortToast(mContext, "再聊一会儿才可以查看Ta的资料");
			}
		} else if (v.getId() == R.id.beside_dialog_popupmenu_chatroom_menu_unlike_ll) { // 不欢迎
			LogSystem.d(TAG, "click unlike " + uid  + "isContainsMember: " +  manager.unlikeList.contains(String.valueOf(uid))+ "  mIsMemberChanges :" + manager.mIsMemberChanges);
			if ( manager.unlikeList.contains(String.valueOf(uid))) {
				if (manager.mIsMemberChanges) {
					ToastUtils.showShortToast(mContext, "你已经发起请 "+nickname +" 离开聊天室的请求，等待大家的投票");
					manager.unlikeMember(""+uid, nickname);
				}else {
					ToastUtils.showShortToast(mContext, "已发起，无需重复操作");
				}
			}else {
				ToastUtils.showShortToast(mContext, "你已经发起请 "+nickname +" 离开聊天室的请求，等待大家的投票");
				manager.unlikeMember(""+uid, nickname);
			}
		} 
		dismiss();
	}
}