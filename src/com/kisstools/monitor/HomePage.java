package com.kisstools.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomePage extends Activity {

	public static final String TAG = "HomePage";

	public static final int REQUEST_CHOOSE_APP = 0;

	private TextView tvInfo;
	private ImageView btAdd;
	private List<String> packageList;
	private ListView lvSelected;
	private List<AppSummary> summaryList;
	private InfoUpdater updater;
	private Handler handler;

	private class AppSummary {
		String name;
		String info;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		packageList = new ArrayList<String>();
		handler = new Handler(Looper.getMainLooper());
		summaryList = new ArrayList<AppSummary>();

		btAdd = (ImageView) findViewById(R.id.bt_add);
		btAdd.setOnClickListener(clickListener);

		lvSelected = (ListView) findViewById(R.id.lv_selected);
		lvSelected.setAdapter(adapter);

		updater = new InfoUpdater();
	}

	protected void onResume() {
		super.onResume();
		handler.post(updater);
	}

	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(updater);
	}

	private class InfoUpdater implements Runnable {

		@Override
		public void run() {
			summaryList.clear();

			for (String pkg : packageList) {
				AppSummary summary = new AppSummary();
				summary.name = pkg;
				summary.info = "xx" + System.currentTimeMillis();
				summaryList.add(summary);
			}
			adapter.notifyDataSetChanged();
			handler.postDelayed(this, 1000);
		}
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.equals(btAdd)) {
				Intent starter = new Intent(v.getContext(), AppChooser.class);
				startActivityForResult(starter, REQUEST_CHOOSE_APP);
			}
		}
	};

	private BaseAdapter adapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.application_summary, parent, false);
			}

			AppSummary summary = summaryList.get(position);

			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvName.setText(summary.name);

			TextView tvInfo = (TextView) convertView.findViewById(R.id.tv_info);
			tvInfo.setText(summary.info);

			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			if (summaryList == null || summaryList.isEmpty()) {
				return 0;
			} else {
				return summaryList.size();
			}
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_CHOOSE_APP || resultCode != RESULT_OK) {
			return;
		}
		String packageName = data.getStringExtra(AppChooser.PACKAGE_NAME);
		Log.d(TAG, "choose add new package " + packageName);
		if (!packageList.contains(packageName)) {
			packageList.add(packageName);
		}
	}

	private void getMemInfo(String processName) {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> lrapi = am.getRunningAppProcesses();

		int pid = -1;
		for (RunningAppProcessInfo rapi : lrapi) {
			if (processName.equals(rapi.processName)) {
				pid = rapi.pid;
				break;
			}
		}

		if (pid == -1) {
			return;
		}

		int pids[] = new int[1];
		pids[0] = pid;
		MemoryInfo[] mia = am.getProcessMemoryInfo(pids);
		if (mia == null || mia.length <= 0) {
			return;
		}

		MemoryInfo mi = mia[0];

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("** MEMINFO in pid %d [%s] **\n", pid,
				processName));
		sb.append("totalPrivateDirty " + mi.getTotalPrivateDirty() + "\n");
		sb.append("totalPss " + mi.getTotalPss() + "\n");
		sb.append("totalSharedDirty " + mi.getTotalSharedDirty() + "\n");
		tvInfo.setText(sb.toString());
	}

}
