package net.twisterrob.android.test;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.RequiresPermission;

/**
 * The below should be equivalent to using this class,
 * but it's better to achieve everything without requiring an activity.
 * <code><pre>
 * getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
 * getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
 * getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 * </pre></code>
 */
public class DeviceUnlocker {
	private final KeyguardManager keyguardManager;
	private final PowerManager powerManager;

	public DeviceUnlocker(Context context) {
		keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
		powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
	}

	@RequiresPermission(allOf = {
			Manifest.permission.DISABLE_KEYGUARD,
			Manifest.permission.WAKE_LOCK
	})
	@SuppressWarnings("deprecation")
	public void wakeUpWithDisabledKeyguard() {
		KeyguardManager.KeyguardLock kl = keyguardManager.newKeyguardLock("Keyguard off for Test");
		kl.disableKeyguard();
		try {
			wakeUp();
		} finally {
			kl.reenableKeyguard();
		}
	}

	@RequiresPermission(Manifest.permission.WAKE_LOCK)
	@SuppressWarnings("deprecation")
	public void wakeUp() {
		int flags = PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE;
		WakeLock wakeLock = powerManager.newWakeLock(flags, "Wake up for Test");
		try {
			wakeLock.acquire();
		} finally {
			wakeLock.release();
		}
	}
}
