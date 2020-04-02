package net.twisterrob.java.collections;

import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.junit.*;

public class RestoreStructureAlgoTest {
	@Test public void testLevels() {
		Map<String, Integer> data = new LinkedHashMap<>();
		data.put("1", 1);
		data.put("1.1", 2);
		data.put("1.2", 2);
		data.put("1.2.1", 3);
		data.put("1.2.2", 3);
		data.put("1.3", 2);
		data.put("1.3.1", 3);
		data.put("2", 1);
		data.put("2.1", 2);

		StringBuilder sb = new StringBuilder();

		int lastLevel = 0;
		for (Entry<String, Integer> entry : data.entrySet()) {
			int level = entry.getValue();
			String name = entry.getKey();

			if (level < lastLevel) {
				for (int i = lastLevel; level < i; --i) {
					sb.append("}(").append(i).append(")\n");
				}
			}
			if (lastLevel < level) {
				for (int i = lastLevel + 1; i <= level; ++i) {
					sb.append("{(").append(i).append(")\n");
				}
			}

			sb.append(level).append(":").append(name).append("\n");

			lastLevel = level;
		}
		if (0 < lastLevel) {
			for (int i = lastLevel; 0 < i; --i) {
				sb.append("}(").append(i).append(")\n");
			}
		}

		Assert.assertEquals(
				"{(1)\n1:1\n{(2)\n2:1.1\n2:1.2\n{(3)\n3:1.2.1\n3:1.2.2\n}(3)\n2:1.3\n{(3)\n3:1.3.1\n}(3)\n}(2)\n1:2\n{(2)\n2:2.1\n}(2)\n}(1)\n",
				sb.toString());
	}

	@Test public void testLevelsAlgo() {
		Map<String, Integer> data = new LinkedHashMap<>();
		data.put("1", 1);
		data.put("1.1", 2);
		data.put("1.2", 2);
		data.put("1.2.1", 3);
		data.put("1.2.2", 3);
		data.put("1.3", 2);
		data.put("1.3.1", 3);
		data.put("2", 1);
		data.put("2.1", 2);

		String result = new RestoreStructureAlgo<Map<String, Integer>, Entry<String, Integer>, String>() {
			private StringBuilder sb;

			@Override protected @Nonnull Iterator<Entry<String, Integer>> start(@Nonnull Map<String, Integer> data) {
				sb = new StringBuilder();
				return data.entrySet().iterator();
			}

			@Override protected int getLevel(Entry<String, Integer> entry) {
				return entry == null? 0 : entry.getValue();
			}

			@Override protected void onIncrementLevel(int level, Entry<String, Integer> item) {
				sb.append("{(").append(level).append(")\n");
			}

			@Override protected void onEntity(int level, @Nonnull Entry<String, Integer> data) {
				String name = data.getKey();
				sb.append(level).append(":").append(name).append("\n");
			}

			@Override protected void onDecrementLevel(int level) {
				sb.append("}(").append(level).append(")\n");
			}

			@Override protected String finish() {
				return sb.toString();
			}
		}.run(data);
		Assert.assertEquals(
				"{(1)\n1:1\n{(2)\n2:1.1\n2:1.2\n{(3)\n3:1.2.1\n3:1.2.2\n}(3)\n2:1.3\n{(3)\n3:1.3.1\n}(3)\n}(2)\n1:2\n{(2)\n2:2.1\n}(2)\n}(1)\n",
				result);
	}
}

