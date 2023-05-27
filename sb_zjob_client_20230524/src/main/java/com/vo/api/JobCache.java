package com.vo.api;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.vo.core.ZScheduledTask;
import com.votool.common.CR;

/**
 * 暂存new 出来的Task对象
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
public class JobCache {

//	private static final ConcurrentMap<String, ZScheduledTask> map = Maps.newConcurrentMap();
	static HashMultimap<String, ZScheduledTask> map = HashMultimap.create();

	static {
	}

	public synchronized static void put(final String zjobTaskName, final ZScheduledTask scheduledTask) {
		map.put(zjobTaskName, scheduledTask);
	}

	public synchronized static Set<ZScheduledTask> get(final String zjobTaskName) {
		return map.get(zjobTaskName);
	}
}
