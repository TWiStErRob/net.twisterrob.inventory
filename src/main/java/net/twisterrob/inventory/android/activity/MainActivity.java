package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;

import net.twisterrob.inventory.R;

public class MainActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);

		Button properties = (Button)findViewById(R.id.btn_ok);
		properties.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), RoomEditActivity.class));
			}
		});
		properties.performClick();
	}
}
