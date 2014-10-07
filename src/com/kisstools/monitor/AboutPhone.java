package com.kisstools.monitor;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.kisstools.monitor.data.DisplayInfo;

public class AboutPhone extends Activity {

	public static final String TAG = "AboutPhone";

	private TextView tvInfo;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_about_phone);

		tvInfo = (TextView) findViewById(R.id.tv_info);
		StringBuilder builder = new StringBuilder();

		builder.append("============ display ============\n");
		DisplayInfo di = new DisplayInfo(this);
		builder.append("width    " + di.getWidth()).append("\n");
		builder.append("height   " + di.getHeight()).append("\n");
		builder.append("density  " + di.getDensity()).append("\n");
		builder.append("inch     " + di.getInch()).append("\n\n");

		tvInfo.setText(builder.toString());
	}

}
