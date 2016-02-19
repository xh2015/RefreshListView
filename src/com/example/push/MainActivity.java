package com.example.push;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.push.view.RefreshListView;
import com.example.push.view.RefreshListView.OnRefreshListener;

public class MainActivity extends Activity {
	private RefreshListView refreshListView;
	private ArrayList<String> lists = new ArrayList<String>();
	private MyAdapter adapter;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 更新ui
			adapter.notifyDataSetChanged();
			refreshListView.completeRefresh();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		refreshListView = (RefreshListView) findViewById(R.id.refresh_listview);
	}

	private void initData() {
		for (int i = 0; i < 25; i++) {
			lists.add("listview原来的数据-" + i);
		}

		final View headerView = View
				.inflate(this, R.layout.layout_header, null);
		// 测量，布局，绘制
		// 在oncreate中View.getWidth和View.getHeight无法获得一个view的高度和宽度，这是因为View组件布局要在onResume回调后完成
		// 第一种方法
		// headerView.getViewTreeObserver().addOnGlobalLayoutListener(new
		// OnGlobalLayoutListener() {
		//
		// @Override
		// public void onGlobalLayout() {
		// headerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		// int headerViewHeight=headerView.getHeight();
		// headerView.setPadding(0, -headerViewHeight, 0, 0);
		// refreshListView.addHeaderView(headerView);
		// }
		// });
		// //第二种方法
		// headerView.measure(0, 0);//主动通知系统去测量
		// int headerViewHeight=headerView.getMeasuredHeight();
		// headerView.setPadding(0, -headerViewHeight, 0, 0);
		// refreshListView.addHeaderView(headerView);

		adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);
		refreshListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onPullRefresh() {
				// Log.e("lalal","进入");
				requestDataFromServer(false);
			}

			@Override
			public void onLoadingMore() {
				requestDataFromServer(true);
			}

		});
	}

	/**
	 * 模拟向服务器请求数据
	 */
	private void requestDataFromServer(final boolean isLoadingMore) {
		new Thread() {
			public void run() {
				SystemClock.sleep(3000);
				if(isLoadingMore){
					lists.add("加载更多的数据-1");
					lists.add("加载更多的数据-2");
					lists.add("加载更多的数据-3");
				}else{
					lists.add(0, "下拉刷新的数据");
				}
				
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return lists.size();
		}

		@Override
		public Object getItem(int position) {
			return lists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(MainActivity.this);
			textView.setPadding(20, 20, 20, 20);
			textView.setTextSize(18);
			textView.setText(lists.get(position));
			return textView;
		}

	}
}
