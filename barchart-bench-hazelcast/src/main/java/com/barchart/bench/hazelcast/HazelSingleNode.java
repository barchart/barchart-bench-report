package com.barchart.bench.hazelcast;

import com.google.caliper.Param;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelSingleNode extends BenchBase {

	@Param("1000")
	private int instanceCount;

	private HazelcastInstance server;

	@Override
	protected void setUp() throws Exception {

		final Config hazelConf = new Config();

		server = Hazelcast.newHazelcastInstance(hazelConf);

	}

	@Override
	protected void tearDown() throws Exception {

	}

	public void timeMain(final int reps) throws Exception {
		for (int k = 0; k < reps; k++) {
			instanceCount = k * k;
		}
	}

	public static void main(final String... args) throws Exception {
		new HazelSingleNode().execute();
	}

}
