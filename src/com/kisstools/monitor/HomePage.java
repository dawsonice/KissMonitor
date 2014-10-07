package com.kisstools.monitor;

import java.util.ArrayList;
import java.util.List;

import me.dawson.kisstools.data.KVDataBase;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

	private ImageView btAdd;
	private ListView lvSelected;
	private List<AppSummary> summaryList;
	private InfoUpdater updater;
	private Handler handler;
	private KVDataBase database;

	private class AppSummary {
		String pkg;
		Drawable icon;
		String info;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		database = new KVDataBase(this, "selected_apps");

		summaryList = new ArrayList<AppSummary>();
		List<String> packageList = database.list();
		for (String pkg : packageList) {
			AppSummary summary = new AppSummary();
			summary.pkg = pkg;
			summaryList.add(summary);
		}
		handler = new Handler(Looper.getMainLooper());

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
			PackageManager pm = getPackageManager();
			for (AppSummary summary : summaryList) {
				try {
					String pkg = summary.pkg;
					if (summary.icon == null) {
						summary.icon = pm.getApplicationIcon(pkg);
					}
					summary.info = getMemInfo(summary);
					// summary.info += "\n" + getCpuInfo(summary);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			adapter.notifyDataSetChanged();
			handler.postDelayed(this, 1000);
		}
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.equals(btAdd)) {
				Intent starter = new Intent(v.getContext(), AboutPhone.class);
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

			TextView tvInfo = (TextView) convertView.findViewById(R.id.tv_info);
			tvInfo.setText(summary.info);

			ImageView ivIcon = (ImageView) convertView
					.findViewById(R.id.iv_icon);
			ivIcon.setImageDrawable(summary.icon);

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
		if (TextUtils.isEmpty(packageName)) {
			return;
		}

		for (AppSummary summary : summaryList) {
			if (packageName.equals(summary.pkg)) {
				return;
			}
		}

		database.set(packageName, packageName);
		AppSummary ap = new AppSummary();
		ap.pkg = packageName;
		summaryList.add(ap);
	}

	private String getMemInfo(AppSummary summary) {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> lrapi = am.getRunningAppProcesses();

		String pkgName = summary.pkg;
		int pid = -1;
		for (RunningAppProcessInfo rapi : lrapi) {
			if (pkgName.equals(rapi.processName)) {
				pid = rapi.pid;
				break;
			}
		}

		if (pid == -1) {
			return null;
		}

		int pids[] = new int[1];
		pids[0] = pid;
		MemoryInfo[] mia = am.getProcessMemoryInfo(pids);
		if (mia == null || mia.length <= 0) {
			return null;
		}

		MemoryInfo mi = mia[0];

		StringBuilder sb = new StringBuilder();
		sb.append("mem: " + mi.getTotalPss());
		return sb.toString();
	}

	// private String getCpuInfo(AppSummary summary) {
	// String cpuInfo = SystemUtil.runCmd("top -n 1 -s cpu | grep '"
	// + summary.pkg + "$' | awk '{print $3}';");
	// return "cpu: " + cpuInfo.trim();
	// }

}
