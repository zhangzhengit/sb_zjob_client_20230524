package com.vo.common;

import java.util.concurrent.ScheduledFuture;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
public class ScheduledTask {

	// 使用volatile同步机制，处理定时任务
	public volatile ScheduledFuture future;

	/**
	 * @return void
	 * @method cancel
	 * @description 取消定时任务
	 */
	public void cancel() {
		final ScheduledFuture future = this.future;
		if (future != null) {
			future.cancel(true);
		}
	}
}
