				create("Drive Chooser", new Runnable() {
					public void run() {
						Intent intent = new Intent(App.getAppContext(), PickDriveFileActivity.class);
						startActivity(intent);
					}
				}),
				create("Drive Test", new Runnable() {
					public void run() {
						Intent intent = new Intent(App.getAppContext(), DeveloperDriveActivity.class);
						startActivity(intent);
					}
				}),