package com.barchart.bench.hazelcast;

import java.util.List;
import java.util.Map;

import com.google.caliper.Param;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
	private int[] keyIndex;
	private String[] keySet;

	@Override
	protected void setUp() throws Exception {

		final Config hazelConf = new Config();

		hazel = Hazelcast.newHazelcastInstance(hazelConf);

		hazelMap = hazel.getMap("default");

		final Map<String, String> bootMap = HazelUtil.randomMap(mapSize, keySize,
				valueSize);

		keyIndex = HazelUtil.randomIndex(mapSize);

		keySet = bootMap.keySet().toArray(new String[0]);

		keyIndex = HazelUtil.randomIndex(mapSize);

		hazelMap.putAll(bootMap);

	}

	@Override
	protected void tearDown() throws Exception {

		hazel.getLifecycleService().shutdown();

	}

	private String store;

	public void timeGet(final int reps) throws Exception {
		for (int r = 0; r < reps; r++) {
			final int k = r % mapSize;
			final int index = keyIndex[k];
			final String key = keySet[index];
			final String value = hazelMap.get(key);
			store = value;
		}
	}

	public static void main(final String... args) throws Exception {
		new HazelNodeOne().execute();
	}

}
