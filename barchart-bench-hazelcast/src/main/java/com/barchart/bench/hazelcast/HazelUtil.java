package com.barchart.bench.hazelcast;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HazelUtil {

	public static Map<String, String> randomMap(final int mapSize,
			final int keySzie, final int valueSize) {
		final Map<String, String> map = new HashMap<String, String>();
		for (int k = 0; k < mapSize; k++) {
			final String key = randomString(keySzie);
			final String value = randomString(valueSize);
			map.put(key, value);
		}
		return map;
	}

	private final static Random random = new Random(0);

	public static int[] randomIndex(final int size) {
		final int[] array = new int[size];
		for (int k = 0; k < size; k++) {
			array[k] = random.nextInt(size);
		}
		return array;
	}

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	public static String randomString(final int size) {
		final byte[] array = new byte[size * 2];
		random.nextBytes(array);
		return new String(array, UTF_8);
	}

}
