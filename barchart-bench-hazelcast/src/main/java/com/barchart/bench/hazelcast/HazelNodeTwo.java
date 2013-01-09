package com.barchart.bench.hazelcast;

import java.util.List;

import com.barchart.bench.TrafficControl;
import com.google.caliper.Param;

public class HazelNodeTwo extends HazelBaseTwo {

	@Param
	private int latency;

	protected static List<String> latencyValues() {
		return HazelBaseOne.delayList();
	}

	protected String store;

	public void timeGetOne(final int reps) {
		for (int r = 0; r < reps; r++) {
			final String key = randomKey(r);
			final String value = hazelMapOne.get(key);
			store = value;
		}
	}

	public void timeGetTwo(final int reps) {
		for (int r = 0; r < reps; r++) {
			final String key = randomKey(r);
			final String value = hazelMapTwo.get(key);
			store = value;
		}
	}

	@Override
	protected void setUp() throws Exception {
		TrafficControl.delay(latency);
	}

	@Override
	protected void tearDown() throws Exception {
		TrafficControl.delay(0);
	}

	public static void main(final String... args) throws Exception {
		new HazelNodeTwo().execute( //
				"--debug" //
				);
		HazelNodeTwo.done();
	}

}
