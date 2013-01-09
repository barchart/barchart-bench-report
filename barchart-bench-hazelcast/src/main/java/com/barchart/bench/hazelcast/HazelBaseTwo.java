package com.barchart.bench.hazelcast;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.bench.BenchBase;
import com.barchart.bench.BenchUtil;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HazelBaseTwo extends BenchBase {

	private final static Logger log = LoggerFactory
			.getLogger(HazelNodeTwo.class);

	static {

		/** provide default hazel logging provider */
		System.setProperty("hazelcast.logging.type", "slf4j");

		/** needed for proper multicast discovery */
		System.setProperty("java.net.preferIPv4Stack", "true");

	}

	protected static List<String> delayList() {
		return BenchUtil.valueList("0,10");
	}

	protected static volatile HazelcastInstance hazelOne;
	protected static volatile HazelcastInstance hazelTwo;

	protected static volatile IMap<String, String> hazelMapOne;
	protected static IMap<String, String> hazelMapTwo;

	protected static final int mapSize = 10 * 1000;
	protected static final int keySize = 20;
	protected static final int valueSize = 200;

	private static volatile int[] randomIndex;
	private static volatile String[] keySet;

	protected static String randomKey(final int rep) {
		final int k = rep % mapSize;
		final int index = randomIndex[k];
		final String key = keySet[index];
		return key;
	}

	protected static void showtime() throws Exception {

		{
			final Config config = HazelUtil.serverConfig();
			config.setProperty("hazelcast.local.localAddress", "hazel-01");
			hazelOne = Hazelcast.newHazelcastInstance(config);
			hazelMapOne = hazelOne.getMap("default");
		}

		log.info("@@@ started one");

		{
			final Config config = HazelUtil.serverConfig();
			config.setProperty("hazelcast.local.localAddress", "hazel-02");
			hazelTwo = Hazelcast.newHazelcastInstance(config);
			hazelMapTwo = hazelTwo.getMap("default");
		}

		log.info("@@@ started two");

		{

			final Map<String, String> randomMap = //
			HazelUtil.randomMap(mapSize, keySize, valueSize);

			keySet = randomMap.keySet().toArray(new String[0]);

			randomIndex = HazelUtil.randomIndex(mapSize);

			int index = 0;
			for (final String key : keySet) {
				if (index % 1000 == 0) {
					log.info("popultate index : {}", index);
				}
				final String value = randomMap.get(key);
				if (index++ % 2 == 0) {
					hazelMapOne.put(key, value);
				} else {
					hazelMapTwo.put(key, value);
				}
			}

		}

		log.info("@@@ populated hazel map");

		{
			HazelUtil.warmup(hazelMapOne);
			HazelUtil.warmup(hazelMapTwo);
		}

		log.info("@@@ finished warm up");

	}

	protected static void shutdown() throws Exception {
		Hazelcast.shutdownAll();
	}

}
