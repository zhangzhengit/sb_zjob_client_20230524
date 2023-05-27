package com.vo.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 *
 * Scheduling 配置
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Configuration
public class SchedulingConfig {
	@Bean
	public TaskScheduler taskScheduler() {
		// 创建任务调度线程池
		final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		// 初始化线程池数量
		taskScheduler.setPoolSize(4);
		// 是否将取消后的任务，从队列中删除
		taskScheduler.setRemoveOnCancelPolicy(true);
		// 设置线程名前缀
		taskScheduler.setThreadNamePrefix("Scheduling-ThreadPool-");
		return taskScheduler;
	}
}
