package com.feinno.beside.ui.activity.group;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.fetion.R;

import com.feinno.beside.model.BroadCastNews;
import com.feinno.beside.model.Topic;
import com.feinno.beside.ui.activity.BaseActivity;
import com.feinno.beside.ui.activity.broadcast.SendBroadcastActivity;
import com.feinno.beside.ui.dialog.BroadcastMediaAttachShareDialog;
import com.feinno.beside.ui.dialog.MediaAttachShareDialog;
import com.feinno.beside.ui.dialog.MediaAttachShareDialog.AttachShareDialogListener;
import com.feinno.beside.ui.dialog.TopicMediaAttachShareDialog;
import com.feinno.beside.utils.Config;
import com.feinno.beside.utils.HttpTaskPool;
import com.feinno.beside.utils.NavConfig;
import com.feinno.beside.utils.ToastUtils;
import com.feinno.beside.utils.cache.ImageFetcher;
import com.feinno.beside.utils.fetion.Account;
import com.feinno.beside.utils.log.LogSystem;
import com.feinno.beside.utils.network.GetData;
import com.feinno.beside.utils.network.HttpParam;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class GroupListActivity extends BaseActivity {
    public static final String SHARE_BROADCAST = "share_broadcast";
    public static final String FROMTOPIC = "fromTopic";
    public static final String SHARE_TOPIC = "share_topic";
    private boolean isShareTopic = false;
    private Topic mTopic;
    private TopicMediaAttachShareDialog mShareDialog;
    private ListView mListView;
    private GroupListAdapter mAdapter;
    private BroadCastNews mBroadCastNews;
    private MediaAttachShareDialog mDialog;
    private String mGroupUri;
    private String TAG = GroupListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beside_activity_group_list);
        mContext = getApplicationContext();
        mListView = (ListView) findViewById(R.id.group_list);
        Cursor c = getGroupListCursor();
        mAdapter = new GroupListAdapter(this, c, true);
        mListView.setAdapter(mAdapter);
        setTitle(R.string.beside_select_group_title);
        Intent intent = getIntent();
        isShareTopic = intent.getBooleanExtra(FROMTOPIC, false);
        if (isShareTopic) {
            mTopic = (Topic) intent.getSerializableExtra(SHARE_TOPIC);
            if (mTopic == null) {
                finish();
            }
        } else {
            mBroadCastNews = (BroadCastNews) intent.getSerializableExtra(SHARE_BROADCAST);
            if (mBroadCastNews == null) {
                finish();
            }
        }

        if (isShareTopic) {
            // 分享话题
            mShareDialog = new TopicMediaAttachShareDialog(GroupListActivity.this, mTopic);
            mShareDialog.setAttachShareDialogListener(new AttachShareDialogListener() {
                @Override
                public void shareOrNotOnClick(int id, String in) {
                    if (id == R.id.share_cancle) {
                        LogSystem.i(TAG, "shareOrNotOnClick cancel.");
                        mShareDialog.dismiss();
                        finish();
                    } else if (id == R.id.share_ok) {
                        sendShareTopic(in);
                        LogSystem.i(TAG, "shareOrNotOnClick ok.");
                        mShareDialog.dismiss();
                        finish();
                    } else if (id == R.id.share_permission) {

                    }
                }
            });
        } else {
            // TODO 分享动态
            mBroadCastNews.range = 1;// 默认设置成1,分享到群动态,1:公开权限
            
            mDialog = new BroadcastMediaAttachShareDialog(GroupListActivity.this, mBroadCastNews,
                    MediaAttachShareDialog.SHARETOGROUP,R.style.beside_share_dynamic_dialog_style);
            mDialog.setAttachShareDialogListener(new AttachShareDialogListener() {
                @Override
                public void shareOrNotOnClick(int id, String desc) {
                    if (id == R.id.share_cancle) {
                        LogSystem.i(TAG, "shareOrNotOnClick cancel.");
                        mDialog.dismiss();
                        finish();
                    } else if (id == R.id.share_ok) {
                        sendShareBroadcast(desc, mBroadCastNews.range);
                        LogSystem.i(TAG, "shareOrNotOnClick ok.");
                        mDialog.dismiss();
                        finish();
                    } else if (id == R.id.share_permission) {
                        if (mBroadCastNews.range == 0) {
                            mBroadCastNews.range = 1;
                        } else if (mBroadCastNews.range == 1) {
                            mBroadCastNews.range = 0;
                        }
                    }
                }
            });
        }

        mTitleRightView.setVisibility(View.INVISIBLE);
    }

    private Cursor getGroupListCursor() {
        Cursor cursor = null;
        //read fetion.db
        try {
            cursor = getApplicationContext().getContentResolver().query(
                    Uri.parse(Config.FETION_PROVIDER_URL + "/pg_group"),
                    new String[] {"_id","name","get_group_portrait_hds","portrait_crc","uri"},
                    "ower_id=? and identity in (1,2,3)",
                    new String[] {"" + Account.getUserId()}, null);
        } catch (RuntimeException ex) {
            LogSystem.e("cn.com.fetion.provider", "query pg_group exception: " + ex.getMessage());
        } finally {
            if (cursor != null) {
                LogSystem.d("cn.com.fetion.provider", "query pg_group return: " + cursor);
            } else {
                LogSystem.d("cn.com.fetion.provider", "query pg_group return: null");
            }
        }
        return cursor;
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null && mAdapter.getCursor() != null) {
            mAdapter.getCursor().close();
        }
        super.onDestroy();
    }

    private HttpTaskPool mTaskPool = HttpTaskPool.getTaskPool();

    private void sendShareBroadcast(String desc, int shareTarget) {

        long bid = mBroadCastNews.broadid;
        // 每次分享都要获取当前动态的bid,不取card的id.
        // if (mBroadCastNews.cards != null && mBroadCastNews.cards.size() > 0)
        // {
        // bid = mBroadCastNews.cards.get(0).id;
        // }
        RequestParams params = new RequestParams(HttpParam.GROUP_URI, mGroupUri, HttpParam.B_ID, bid);
        params.put(HttpParam.RANGE, String.valueOf(shareTarget));
        if (!TextUtils.isEmpty(desc)) {
            params.put(HttpParam.BROADCAST_CONTENT, desc);
        }
        mTaskPool.executeRequest(Config.getShareDynamic2Group(), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String content) {
                super.onSuccess(statusCode, headers, content);
                ToastUtils.showShortToast(GroupListActivity.this, R.string.notice_share_success);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mDialog.dismiss();
            }
        });
    }

    private void sendShareTopic(String content) {
        if (null != mTopic) {
            new GetData(this).shareTopicToGroup(Config.getShareTopic2Group(), mTopic.id, mGroupUri, content,
                    mTopic.type, new AsyncHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, String content) {
                            super.onSuccess(statusCode, headers, content);
                            ToastUtils.showShortToast(GroupListActivity.this, R.string.notice_share_success);
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            mShareDialog.dismiss();
                        }
                    });
        }
    }

    private class GroupListAdapter extends CursorAdapter {

        private String baseUrl;

        public GroupListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.photo_view);
            TextView textView = (TextView) view.findViewById(R.id.group_name);
            final String groupName = cursor.getString(cursor.getColumnIndex("name"));
            if (TextUtils.isEmpty(baseUrl)) {
                baseUrl = NavConfig.getValue(context, "get-group-portrait", null);
            }
            String crc = cursor.getString(cursor.getColumnIndex("portrait_crc"));
            final String uri = cursor.getString(cursor.getColumnIndex("uri"));
            String url = null;
            try {
                url = baseUrl + "?&c={0}&portraitcrc=" + crc + "&size=64&uri=" + URLEncoder.encode(uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ImageFetcher fetcher = ImageFetcher.getFetcherInstance(GroupListActivity.this);
            DisplayImageOptions options = fetcher.getRoundedOptions(R.drawable.beside_group_default);
            fetcher.loadImage(url, imageView, options, null);
            textView.setText(groupName);
            view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, SendBroadcastActivity.class);
	    			intent.putExtra(SendBroadcastActivity.IS_SHARE2GROUP, true);
	    			intent.putExtra(SendBroadcastActivity.SHAREDCONTENT, mBroadCastNews);
	    			intent.putExtra(SendBroadcastActivity.SHAREDGROUPURI, uri);
	    			intent.putExtra(SendBroadcastActivity.SHAREDGROUPNAME, groupName);
	    			mContext.startActivity(intent);
				}
			});
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = getLayoutInflater().inflate(R.layout.beside_group_list_item, null);
            return view;
        }
    }
}
