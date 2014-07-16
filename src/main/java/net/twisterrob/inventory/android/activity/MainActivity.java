package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.contract.Extras;

public class MainActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		Button properties = (Button)findViewById(R.id.btn_ok);
		properties.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = createIntent(PropertiesActivity.class);
				startActivity(intent);
			}
		});
		new Handler().post(new Runnable() {
			public void run() {
				Intent intent = createIntent(ItemsActivity.class);
				intent.putExtra(Extras.PARENT_ID, 4L);
				startActivity(intent);
			}
		});
	}
}
