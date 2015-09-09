package com.feinno.beside.fetion;

import java.util.List;

import com.feinno.beside.application.BesideInitUtil;
import com.feinno.beside.model.Marker;
import com.feinno.beside.model.ShareMap;
import com.feinno.beside.utils.log.LogSystem;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * 飞信调用身边，身边提供的代理服务类
 * @author yuanshuqi
 *
 */
public class Beside2FetionProxyService extends Service {
	private ServiceBinder msb = new ServiceBinder();
	private BesideInterface bi = new BesideInterfaceImple();
	
	@Override
	public IBinder onBind(Intent intent) {
		LogSystem.d(FetionBesideChannel.TAG, "Beside2FetionProxyService ,onBind,initBeside...");
		BesideInitUtil besideUtil = BesideInitUtil.getBesideInitUtilInstance();
		besideUtil.initBeside(getApplication(), true);
		return msb;
	}
	
	 public class ServiceBinder extends com.feinno.beside.model.Beside2FetionProxy.Stub {

		@Override
		public List<String> getPersonalNewestImg(int userId) throws RemoteException {
			return bi.getPersonalNewestImg(userId, Beside2FetionProxyService.this);
		}

		@Override
		public List<String> getGroupNewestImg(String uri) throws RemoteException {
			return bi.getGroupNewestImg(uri, Beside2FetionProxyService.this);
		}

		@Override
		public int getGroupVisual(String uri) throws RemoteException {
			return bi.getGroupVisual(uri, Beside2FetionProxyService.this);
		}

		@Override
		public int setGroupVisual(String uri, int visual) throws RemoteException {
			return bi.setGroupVisual(uri, visual, Beside2FetionProxyService.this);
		}

		@Override
		public Marker getGroupMarker(String uri) throws RemoteException {
			return bi.getGroupMarker(uri, Beside2FetionProxyService.this);
		}

		@Override
		public int bindGroupMarker(String groupuri, long markId, double longt,double lat) throws RemoteException {
			return bi.bindGroupMarker(Beside2FetionProxyService.this, groupuri, markId, longt, lat);
		}

		@Override
		public int informBeside(String type, String xml) throws RemoteException {
			return bi.informBeside(Beside2FetionProxyService.this, type, xml);
		}

		/**
		 * 分享动态到会话窗口接口
		 */
		@Override
		public void showDetailBroadcast(String broadcast)
				throws RemoteException {
			bi.showDetailBroadcast(Beside2FetionProxyService.this,broadcast);
		}

		/**
		 * 更新游戏中心未读消息数接口
		 */
		@Override
		public void updateGameCenterUnreadNoticeCount(int count)
				throws RemoteException {
			// TODO Auto-generated method stub
			bi.updateGameCenterUnreadNoticeCount(Beside2FetionProxyService.this, count);
		}

		/**
		 * 分享游戏中心内容到朋友圈接口
		 */
		@Override
		public void shareGameToBroadcast(ShareMap information)
				throws RemoteException {
			// TODO Auto-generated method stub
			bi.shareGameToBroadcast(Beside2FetionProxyService.this,information);
		}

		@Override
		public void shareToBroadcast(ShareMap information)
				throws RemoteException {
			bi.shareToBroadcast(getApplicationContext(),information);
		}
	 }

}
