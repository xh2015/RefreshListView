package com.example.push.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.push.R;

public class RefreshListView extends ListView implements OnScrollListener {

	private View headerView;
	private ImageView iv_arrow;
	private ProgressBar pb_rotate;
	private TextView tv_state;
	private TextView tv_time;
	private int downY;
	private int headerViewHeight;

	private final int PULL_REFRESH = 0;// 下拉刷新的状态
	private final int RELEASE_REFRESH = 1;// 松开刷新的状态
	private final int REFRESHING = 2;// 正在刷新的状态
	private int currentState = PULL_REFRESH;

	private RotateAnimation upAnimation, downAnimation;

	public RefreshListView(Context context) {
		super(context);
		init();
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOnScrollListener(this);
		initHeaderView();
		initAnimation();
		initFooterView();
	}

	private void initHeaderView() {
		headerView = View.inflate(getContext(), R.layout.layout_header, null);
		iv_arrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
		pb_rotate = (ProgressBar) headerView.findViewById(R.id.pb_rotate);
		tv_state = (TextView) headerView.findViewById(R.id.tv_state);
		tv_time = (TextView) headerView.findViewById(R.id.tv_time);

		headerView.measure(0, 0);// 主动通知系统去测量view
		headerViewHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		addHeaderView(headerView);
	}

	/**
	 * 初始化旋转动画
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(300);
		upAnimation.setFillAfter(true);
		downAnimation = new RotateAnimation(-180, -360,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(300);
		downAnimation.setFillAfter(true);
	}

	private void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.layout_footer, null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		addFooterView(footerView);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (currentState == REFRESHING) {
				break;
			}
			int deltaY = (int) (ev.getY() - downY);
			int paddingTop = -headerViewHeight + deltaY;
			// paddingTop大于等于-headerview高度的时候才开始设置出现以及显示第一个的时候
			if (paddingTop > -headerViewHeight
					&& getFirstVisiblePosition() == 0) {
				headerView.setPadding(0, paddingTop, 0, 0);
				if (paddingTop >= 0 && currentState == PULL_REFRESH) {
					// 从下拉刷新进入松开刷新
					currentState = RELEASE_REFRESH;
					refreshHeaderView();
				} else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
					// 从松开刷新进入下拉刷新
					currentState = PULL_REFRESH;
					refreshHeaderView();
				}
				return true;// 拦截TouchMove，不让listview处理该次move事件,会造成listview无法滑动
			}

			break;
		case MotionEvent.ACTION_UP:
			if (currentState == PULL_REFRESH) {
				// 隐藏头部
				headerView.setPadding(0, -headerViewHeight, 0, 0);
			} else if (currentState == RELEASE_REFRESH) {
				headerView.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshHeaderView();

				// new Handler().postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				// completeRefresh();
				// }
				//
				// }, 3000);
				if (onRefreshListener != null) {
					onRefreshListener.onPullRefresh();

				}

			}

			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 根据currentstate更新ui
	 */
	private void refreshHeaderView() {

		switch (currentState) {
		case PULL_REFRESH:
			tv_state.setText("下拉刷新");
			iv_arrow.startAnimation(downAnimation);
			break;
		case RELEASE_REFRESH:
			tv_state.setText("松开刷新");
			iv_arrow.startAnimation(upAnimation);
			break;
		case REFRESHING:
			iv_arrow.clearAnimation();// 因为向上的旋转动画有可能没有执行完
			iv_arrow.setVisibility(View.INVISIBLE);
			pb_rotate.setVisibility(View.VISIBLE);
			tv_state.setText("正在刷新...");
			break;
		}
	}

	/**
	 * 完成刷新操作，重置状态,在你获取完数据并更新完adater之后，去在UI线程中调用该方法
	 */
	public void completeRefresh() {
		if (isLoadingMore) {
			isLoadingMore = false;
			footerView.setPadding(0, -footerViewHeight, 0, 0);
		} else {
			// 重置headerview状态
			currentState = PULL_REFRESH;
			headerView.setPadding(0, -headerViewHeight, 0, 0);
			pb_rotate.setVisibility(View.INVISIBLE);
			iv_arrow.setVisibility(View.VISIBLE);
			tv_state.setText("下拉刷新");
			tv_time.setText("最后刷新：" + getCurrentTime());
		}

	}

	/**
	 * 获取当前时间并格式化
	 * 
	 * @return
	 */

	private String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(new Date());
		return currentTime;
	}

	private OnRefreshListener onRefreshListener;
	private View footerView;
	private int footerViewHeight;
	private boolean isLoadingMore = false;

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	public interface OnRefreshListener {
		void onPullRefresh();

		void onLoadingMore();
	}

	/**
	 * SCROLL_STATE_IDLE:闲置状态，就是手指松开 SCROLL_STATE_TOUCH_SCROLL：手指触摸滑动，就是按着来滑动
	 * SCROLL_STATE_FLING：快速滑动后松开
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& getLastVisiblePosition() == (getCount() - 1)
				&& !isLoadingMore) {
			isLoadingMore = true;
			footerView.setPadding(0, 0, 0, 0);
			setSelection(getCount());
			if (onRefreshListener != null) {
				onRefreshListener.onLoadingMore();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}
}
