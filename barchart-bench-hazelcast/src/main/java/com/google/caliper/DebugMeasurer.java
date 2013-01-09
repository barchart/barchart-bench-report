/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.caliper;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Supplier;

/**
 * Measure's the benchmark's per-trial execution time.
 */
class DebugMeasurer extends Measurer {

	private final int reps;

	DebugMeasurer(final int reps) {
		checkArgument(reps > 0);
		this.reps = reps;
	}

	@Override
	public MeasurementSet run(final Supplier<ConfiguredBenchmark> testSupplier)
			throws Exception {

		System.out.println("### reps=" + reps);

		final ConfiguredBenchmark benchmark = testSupplier.get();
		benchmark.run(reps);
		benchmark.close();

		return null;

	}

}
