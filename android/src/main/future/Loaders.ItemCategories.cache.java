		private String[] columnNames;
		private Object[][] matrix;
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			if(matrix == null) {
				Cursor cursor = App.db().listItemCategories();
				columnNames = cursor.getColumnNames();
				Object[][] matrix = new Object[cursor.getCount()][cursor.getColumnCount()];
				int row = 0;
				while(cursor.moveToNext()) {
					matrix[row][0] = cursor.getLong(0); // _id
					matrix[row][1] = cursor.getString(1); // name
					matrix[row][2] = cursor.getLong(2); // parent
					matrix[row][3] = cursor.getShort(3); // level
					matrix[row][4] = cursor.getString(4); // typeImage
					row++;
				}
				this.matrix = matrix;

				cursor.moveToFirst(); // this time use this one
				return cursor;
			}
			return buildMatrix();
		}
		private Cursor buildMatrix() {
			MatrixCursor cursor = new MatrixCursor(columnNames);
			for (Object[] row : matrix) {
				cursor.addRow(row);
			}
			return cursor;
		}