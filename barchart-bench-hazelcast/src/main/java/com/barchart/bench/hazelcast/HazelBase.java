package com.barchart.bench.hazelcast;

import java.util.List;

import com.barchart.bench.BenchBase;
import com.barchart.bench.BenchHelp;

public class HazelBase extends BenchBase {

	protected static List<String> mapList() {
		return BenchHelp.valueList("1000,100000");
	}

	protected static List<String> keyList() {
		return BenchHelp.valueList("10,20,40");
	}

	protected static List<String> valueList() {
		return BenchHelp.valueList("125,250,500,1000,2000");
	}

}
