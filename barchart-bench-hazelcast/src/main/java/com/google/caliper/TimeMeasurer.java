package com.google.caliper;

import static com.google.common.base.Preconditions.*;

import com.google.caliper.UserException.DoesNotScaleLinearlyException;
import com.google.caliper.UserException.RuntimeOutOfRangeException;
import com.google.common.base.Supplier;

/**
 * replace google class
 */
class TimeMeasurer extends Measurer {

	private final long warmupNanos;
	private final long runNanos;

	/**
	 * If the standard deviation of our measurements is within this tolerance,
	 * we won't bother to perform additional measurements.
	 */
	private static final double SHORT_CIRCUIT_TOLERANCE = 0.10; // XXX

	private static final int MAX_TRIALS = 3; // XXX

	TimeMeasurer(final long warmupMillis, final long runMillis) {
		checkArgument(warmupMillis > 50);
		checkArgument(runMillis > 50);

		this.warmupNanos = warmupMillis * 1000000;
		this.runNanos = runMillis * 1000000;
	}

	private double warmUp(final Supplier<ConfiguredBenchmark> testSupplier)
			throws Exception {

		long elapsedNanos = 0;
		long netReps = 0;
		int reps = 1;
		boolean definitelyScalesLinearly = false;

		/*
		 * Run progressively more reps at a time until we cross our warmup
		 * threshold. This way any just-in-time compiler will be comfortable
		 * running multiple iterations of our measurement method.
		 */
		log("[starting warmup]");
		while (elapsedNanos < warmupNanos) {

			final long nanos = measureReps(testSupplier.get(), reps);

			elapsedNanos += nanos;

			netReps += reps;

			reps *= 2;

			// if reps overflowed, that's suspicious! Check that it time scales
			// with reps
			if (reps <= 0) {
				if (!definitelyScalesLinearly) {
					checkScalesLinearly(testSupplier);
					definitelyScalesLinearly = true;
				}
				reps = Integer.MAX_VALUE;
			}
		}
		log("[ending warmup]");

		final double nanosPerExecution = (double) elapsedNanos / netReps;
		final double lowerBound = 0.1;
		final double upperBound = 10000000000.0;
		if (!(lowerBound <= nanosPerExecution && nanosPerExecution <= upperBound)) {
			throw new RuntimeOutOfRangeException(nanosPerExecution, lowerBound,
					upperBound);
		}

		return nanosPerExecution;
	}

	/**
	 * Doing half as much work shouldn't take much more than half as much time.
	 * If it does we have a broken benchmark!
	 */
	private void checkScalesLinearly(
			final Supplier<ConfiguredBenchmark> testSupplier) throws Exception {
		final double half = measureReps(testSupplier.get(),
				Integer.MAX_VALUE / 2);
		final double one = measureReps(testSupplier.get(), Integer.MAX_VALUE);
		if (half / one > 0.75) {
			throw new DoesNotScaleLinearlyException();
		}
	}

	/**
	 * Measure the nanos per rep for the given test. This code uses an
	 * interesting strategy to measure the runtime to minimize execution time
	 * when execution time is consistent.
	 * <ol>
	 * <li>1.0x {@code runMillis} trial is run.
	 * <li>0.5x {@code runMillis} trial is run.
	 * <li>1.5x {@code runMillis} trial is run.
	 * <li>At this point, the standard deviation of these trials is computed. If
	 * it is within the threshold, the result is returned.
	 * <li>Otherwise trials continue to be executed until either the threshold
	 * is satisfied or the maximum number of runs have been executed.
	 * </ol>
	 * 
	 * @param testSupplier
	 *            provides instances of the code under test. A new test is
	 *            created for each iteration because some benchmarks'
	 *            performance depends on which memory was allocated. See
	 *            SetContainsBenchmark for an example.
	 */
	@Override
	public MeasurementSet run(final Supplier<ConfiguredBenchmark> testSupplier)
			throws Exception {

		final double estimatedNanosPerRep = warmUp(testSupplier);

		log("[measuring nanos per rep with scale 1.00]");
		final Measurement measurement100 = measure(testSupplier, 1.00,
				estimatedNanosPerRep);

		log("[measuring nanos per rep with scale 0.50]");
		final Measurement measurement050 = measure(testSupplier, 0.50,
				measurement100.getRaw());

		log("[measuring nanos per rep with scale 1.50]");
		final Measurement measurement150 = measure(testSupplier, 1.50,
				measurement100.getRaw());

		final MeasurementSet measurementSet = new MeasurementSet(
				measurement100, measurement050, measurement150);

		// for (int i = 3; i < MAX_TRIALS; i++) {
		// final double threshold = SHORT_CIRCUIT_TOLERANCE
		// * measurementSet.meanRaw();
		// if (measurementSet.standardDeviationRaw() < threshold) {
		// return measurementSet;
		// }
		//
		// log("[performing additional measurement with scale 1.00]");
		// final Measurement measurement = measure(testSupplier, 1.00,
		// measurement100.getRaw());
		// measurementSet = measurementSet.plusMeasurement(measurement);
		// }

		return measurementSet;
	}

	/**
	 * Runs the test method for approximately {@code runNanos * durationScale}
	 * nanos and returns a Measurement of the nanos per rep and units per rep.
	 */
	private Measurement measure(
			final Supplier<ConfiguredBenchmark> testSupplier,
			final double durationScale, final double estimatedNanosPerRep)
			throws Exception {

		// int reps = (int) (durationScale * runNanos / estimatedNanosPerRep);
		// // XXX
		int reps = 1000;

		if (reps == 0) {
			reps = 1;
		}

		log("[running trial with " + reps + " reps]");
		final ConfiguredBenchmark benchmark = testSupplier.get();
		final long elapsedTime = measureReps(benchmark, reps);
		final double nanosPerRep = elapsedTime / (double) reps;
		log(String.format("[took %.2f nanoseconds per rep]", nanosPerRep));
		return new Measurement(benchmark.timeUnitNames(), nanosPerRep,
				benchmark.nanosToUnits(nanosPerRep));
	}

	/**
	 * Returns the total nanos to run {@code reps}.
	 */
	private long measureReps(final ConfiguredBenchmark benchmark, final int reps)
			throws Exception {
		prepareForTest();
		log(LogConstants.MEASURED_SECTION_STARTING);
		final long startNanos = System.nanoTime();
		benchmark.run(reps);
		final long endNanos = System.nanoTime();
		log(LogConstants.MEASURED_SECTION_DONE);
		benchmark.close();
		return endNanos - startNanos;
	}
}
