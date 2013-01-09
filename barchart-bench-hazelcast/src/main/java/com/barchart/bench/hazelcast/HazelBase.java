package com.barchart.bench.hazelcast;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.barchart.bench.BenchBase;
import com.barchart.bench.BenchUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.LifecycleListener;

public class HazelBase extends BenchBase implements LifecycleListener {

	static {

		/** provide default hazel logging provider */
		System.setProperty("hazelcast.logging.type", "slf4j");

		/** needed for proper multicast discovery */
		System.setProperty("java.net.preferIPv4Stack", "true");

	}

	protected static List<String> mapList() {
		return BenchUtil.valueList("100000");
	}

	protected static List<String> keyList() {
		return BenchUtil.valueList("10,20,40");
	}

	protected static List<String> valueList() {
		return BenchUtil.valueList("100,200,400");
	}

	private final BlockingQueue<LifecycleEvent> //
	eventQueue = new LinkedBlockingQueue<LifecycleEvent>();

	protected Queue<LifecycleEvent> eventQueue() {
		return eventQueue;
	}

	protected long awaitStep() {
		return 100;
	}

	protected long awaitTotal() {
		return 10 * 1000;
	}

	/**
	 * block till hazel node reaches the state
	 */
	protected void await(final LifecycleState state) {
		long total = 0;
		final long step = awaitStep();
		while (true) {
			for (final LifecycleEvent event : eventQueue) {
				if (event.getState() == state) {
					return;
				}
			}
			try {
				Thread.sleep(step);
				total += step;
			} catch (final InterruptedException e) {
				return;
			}
			if (total > awaitTotal()) {
				throw new IllegalStateException("await is taking too much time");
			}
		}
	}

	@Override
	public void stateChanged(final LifecycleEvent event) {
		eventQueue.add(event);
		log.info("event : {}", event);
	}

	protected void listenOn(final HazelcastInstance hazel) {
		hazel.getLifecycleService().addLifecycleListener(this);
	}

	protected void listenOff(final HazelcastInstance hazel) {
		hazel.getLifecycleService().removeLifecycleListener(this);
	}

}
