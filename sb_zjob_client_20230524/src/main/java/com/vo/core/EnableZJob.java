package com.vo.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.vo.api.API;
import com.vo.heartbeat.HeartbeatJob;
import com.vo.heartbeat.ZJobClientConfiguration;

/**
 *
 * 启用zjob
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Import(value = { MQISA.class, ServerConf.class, CronTaskRegistrar.class, TaskSchedulingAutoConfiguration.class,
		SchedulingConfig.class, API.class,
		JobService.class,
		HeartbeatJob.class,ZJobClientConfiguration.class

})
public @interface EnableZJob {

}
