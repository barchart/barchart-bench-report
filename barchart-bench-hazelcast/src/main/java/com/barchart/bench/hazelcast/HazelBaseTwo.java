package com.barchart.bench.hazelcast;

import java.util.List;
import java.util.Map;

import com.barchart.bench.BenchBase;
import com.barchart.bench.BenchUtil;
import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelBaseTwo extends BenchBase {

	static {

		/** provide default hazel logging provider */
		System.setProperty("hazelcast.logging.type", "slf4j");

		/** needed for proper multicast discovery */
		System.setProperty("java.net.preferIPv4Stack", "true");

	}

	protected static List<String> delayList() {
		return BenchUtil.valueList("0,10");
	}

	protected static HazelcastInstance hazelOne;
	protected static HazelcastInstance hazelTwo;

	protected static IMap<String, String> hazelMapOne;
	protected static IMap<String, String> hazelMapTwo;

	static final int mapSize = 100 * 1000;
	static final int keySize = 20;
	static final int valueSize = 200;

	static int[] randomIndex;
	static private String[] keySet;

	protected static String randomKey(final int rep) {
		final int k = rep % mapSize;
		final int index = randomIndex[k];
		final String key = keySet[index];
		return key;
	}

	protected static Config config() {
		final Config config = new Config();
		//
		config.setProperty("hazelcast.socket.bind.any", "false");
		//
		final Join join = config.getNetworkConfig().getJoin();
		join.getAwsConfig();
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().setEnabled(true);
		join.getTcpIpConfig().addMember("hazel-01");
		join.getTcpIpConfig().addMember("hazel-02");
		//
		config.getNetworkConfig().setPort(12345);
		config.getNetworkConfig().setPortAutoIncrement(true);
		//
		return config;
	}

	protected static void init() throws Exception {

		{
			final Config config = config();
			config.setProperty("hazelcast.local.localAddress", "hazel-01");
			hazelOne = Hazelcast.newHazelcastInstance(config);
			hazelMapOne = hazelOne.getMap("default");
		}

		{
			final Config config = config();
			config.setProperty("hazelcast.local.localAddress", "hazel-02");
			hazelTwo = Hazelcast.newHazelcastInstance(config);
			hazelMapTwo = hazelTwo.getMap("default");
		}

		{

			final Map<String, String> randomMap = //
			HazelUtil.randomMap(mapSize, keySize, valueSize);

			keySet = randomMap.keySet().toArray(new String[0]);

			randomIndex = HazelUtil.randomIndex(mapSize);

			int side = 0;
			for (final String key : keySet) {
				final String value = randomMap.get(key);
				if (side++ % 2 == 0) {
					hazelMapOne.put(key, value);
				} else {
					hazelMapTwo.put(key, value);
				}
			}

		}

		{
			HazelUtil.warmup(hazelMapOne);
			HazelUtil.warmup(hazelMapTwo);
		}

	}

	protected static void done() {
		Hazelcast.shutdownAll();
	}

	static {
		try {
			init();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
