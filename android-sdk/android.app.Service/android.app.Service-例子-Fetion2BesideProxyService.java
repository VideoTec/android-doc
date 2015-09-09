package com.feinno.beside.fetion;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.feinno.beside.BesideFetionChannel;
import com.feinno.beside.application.BesideInitUtil;
import com.feinno.beside.utils.log.LogSystem;

public class Fetion2BesideProxyService extends Service {
    public static final String ACTION_REPORT = "action_report";
	FetionBesideChannel mChannel = null;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(android.content.Context context, Intent intent) {
            try {
                long id = intent.getLongExtra("rpid", -1);
                if (id == -1) {
                    return;
                }
                LogSystem.d("report", Long.toString(id));
                if (BesideInitUtil.getUserBehaviorCounter() != null) {
                    BesideInitUtil.getUserBehaviorCounter().setUserBehavior(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
	};
	public void onCreate() {
	    super.onCreate();
	    IntentFilter filter = new IntentFilter(ACTION_REPORT);
        registerReceiver(mReceiver, filter);
	}
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    unregisterReceiver(mReceiver);
	}
	@Override
	public IBinder onBind(Intent intent) {
		LogSystem.d(FetionBesideChannel.TAG, "Fetion2BesideProxyService ,onBind,initBeside...");
		BesideInitUtil besideUtil = BesideInitUtil.getBesideInitUtilInstance();
		besideUtil.initBeside(getApplication(), true);
		mChannel = FetionBesideChannel.getChannelInstance();
		return new ServiceBinder();
	}

	public class ServiceBinder extends Fetion2BesideProxy.Stub {

		@Override
		public void changeTipVisible(int visibility) throws RemoteException {
		    LogSystem.d("changeTipVisibile", "ServiceBinder changeTipVisible =  " + visibility);
			mChannel.changeTipVisible(visibility);
		}

		@Override
		public List<NoticeUIState> getNoticeUIStates() throws RemoteException {
			return mChannel.getNoticeUIStates();
		}

		@Override
		public String getUserProties(String key) throws RemoteException {
			Object obj = BesideFetionChannel.getUserProties(key);
			String value = null;
			if (obj != null) {
				if (obj instanceof String) {
					value = (String)obj;
				} else if (obj instanceof Integer) {
					value  = (Integer)obj + "";
				} else {
					value = (String)obj;
				}
			}
			return value;
		}

		@Override
		public String getBesideNavUrl(String userId) throws RemoteException {
			return BesideFetionChannel.getBesideNavUrl(Fetion2BesideProxyService.this, userId);
		}

		@Override
		public void JionGroup(String uri, String name) throws RemoteException {
			BesideFetionChannel.JionGroup(Fetion2BesideProxyService.this,uri,name);
		}

		/***
		 * 分享动态到会话窗口接口
		 */
//		@Override
//		public void shareBroadcast(String groupUri,String broadcast) throws RemoteException {
//			// TODO Auto-generated method stub
////			BesideFetionChannel.shareBroadcast(groupUri,broadcast);
//		}

		/**
		 * 更新群会话窗口中群动态提醒标记接口
		 */
		@Override
		public void updateNewBroadcastCount(String groupUri, int count)
				throws RemoteException {
			// TODO Auto-generated method stub
			BesideFetionChannel.updateNewBroadcastCount(groupUri, count);
		}

		/**
		 * 进入游戏中心接口
		 */
//		@Override
//		public void showGameCenter() throws RemoteException {
//			// TODO Auto-generated method stub
////			BesideFetionChannel.showGameCenter();
//		}

		/**
		 * 查看动态中分享的游戏信息接口
		 */
//		@Override
//		public void showGameInformation(String information)
//				throws RemoteException {
//			// TODO Auto-generated method stub
////			BesideFetionChannel.showGameInformation(information);
//		}

	}
}
