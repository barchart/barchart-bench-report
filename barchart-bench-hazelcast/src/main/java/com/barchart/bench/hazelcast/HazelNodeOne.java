package com.barchart.bench.hazelcast;

import java.util.List;
import java.util.Map;

import com.google.caliper.Param;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent.LifecycleState;

public class HazelNodeOne extends HazelBase {

	@Param
	private int mapSize;

	protected static List<String> mapSizeValues() {
		return HazelBase.mapList();
	}

	@Param
	private int keySize;

	protected static List<String> keySizeValues() {
		return HazelBase.keyList();
	}

	@Param
	private int valueSize;

	protected static List<String> valueSizeValues() {
		return HazelBase.valueList();
	}

	private HazelcastInstance hazel;
	private IMap<String, String> hazelMap;
	private int[] randomIndex;
	private String[] keySet;

	@Override
	protected void setUp() throws Exception {

		log.info("init");

		final Config config = new Config();

		config.addListenerConfig(new ListenerConfig(this));

		hazel = Hazelcast.newHazelcastInstance(config);

		await(LifecycleState.STARTED);

		hazelMap = hazel.getMap("default");

		randomIndex = HazelUtil.randomIndex(mapSize);

		final Map<String, String> randomMap = HazelUtil.randomMap(mapSize,
				keySize, valueSize);

		keySet = randomMap.keySet().toArray(new String[0]);

		hazelMap.putAll(randomMap);

		HazelUtil.warmup(hazelMap);

	}

	@Override
	protected void tearDown() throws Exception {

		hazel.getLifecycleService().shutdown();

		await(LifecycleState.SHUTDOWN);

		listenOff(hazel);

		log.info("done");

	}

	private String store;

	private String randomKey(final int rep) {
		final int k = rep % mapSize;
		final int index = randomIndex[k];
		final String key = keySet[index];
		return key;
	}

	public void timeGet(final int reps) throws Exception {
		for (int r = 0; r < reps; r++) {
			final String key = randomKey(r);
			final String value = hazelMap.get(key);
			store = value;
		}
	}

	public void timePut(final int reps) throws Exception {
		final String value = HazelUtil.randomString(valueSize);
		for (int r = 0; r < reps; r++) {
			final String key = randomKey(r);
			hazelMap.put(key, value);
		}
	}

	public static void main(final String... args) throws Exception {
		new HazelNodeOne().execute( //
				// "--debug" //
				);
	}

}
