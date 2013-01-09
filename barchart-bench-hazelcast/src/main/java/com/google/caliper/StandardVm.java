package com.google.caliper;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * replace google class
 */
final class StandardVm extends Vm {

	@Override
	public List<String> getVmSpecificOptions(final MeasurementType type,
			final Arguments arguments) {

		final List<String> options = Lists.newArrayList( //
				"-Xms2048m", //
				"-Xmx2048m",//
				"-verbose:gc", //
				"-XX:+UseSerialGC", //
				"-Xbatch", //
				"-XX:+PrintCompilation", //
				"-XX:+TieredCompilation", //
				"-server" //
		);

		return options;
	}

	/** java executable name */
	public static String defaultVmName() {
		return "java";
	}

}
