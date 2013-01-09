package com.barchart.bench.hazelcast;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.Join;

public class HazelUtil {

	private final static Logger log = LoggerFactory.getLogger(HazelUtil.class);

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

	private static String store;

	public static void warmup(final Map<String, String> map) {
		int index = 0;
		for (final String key : map.keySet()) {
			store = map.get(key);
			if (index % 1000 == 0) {
				log.info("warm up index : {}", index);
			}
			index++;
		}
	}

	public static Config serverConfig() {

		final Config config = new Config();

		config.setProperty("hazelcast.socket.bind.any", "false");

		final Join join = config.getNetworkConfig().getJoin();
		join.getAwsConfig().setEnabled(false);
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().setEnabled(true);
		join.getTcpIpConfig().addMember("hazel-01");
		join.getTcpIpConfig().addMember("hazel-02");

		config.getNetworkConfig().setPort(12345);
		config.getNetworkConfig().setPortAutoIncrement(false);

		return config;
	}

}
