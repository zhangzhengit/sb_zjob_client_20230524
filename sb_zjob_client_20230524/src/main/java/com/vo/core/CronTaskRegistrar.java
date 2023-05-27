package com.vo.core;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import com.vo.common.ScheduledTask;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Component
public class CronTaskRegistrar implements DisposableBean {

    private final Map<Runnable, ScheduledTask> scheduledTaskMap = new ConcurrentHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    public void addCronTask(final Runnable runnable, final String cronExpression) {
        this.addCronTask(new CronTask(runnable, cronExpression));
    }

    public void addCronTask(final CronTask cronTask) {
        if (cronTask != null) {
            final Runnable runnnable = cronTask.getRunnable();
            if (this.scheduledTaskMap.containsKey(runnnable)) {
                this.removeCronTask(runnnable);
            }
            this.scheduledTaskMap.put(runnnable, this.scheduleCronTask(cronTask));
        }
    }

    public void removeCronTask(final Runnable task) {
        final ScheduledTask scheduledTask = this.scheduledTaskMap.remove(task);
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }

    public ScheduledTask scheduleCronTask(final CronTask cronTask) {
        final ScheduledTask scheduledTask = new ScheduledTask();
        // 指定一个触发器执行定时任务，并返回执行结果
        scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        return scheduledTask;
    }

    public ScheduledTask executeImmediately(final Runnable runnable) {
    	final CronTask cronTask = new CronTask(runnable, "0/1 * * * * ?");
    	final ScheduledTask executeImmediately = this.executeImmediately(cronTask);
    	return executeImmediately;
    }

    public ScheduledTask executeImmediately(final CronTask cronTask) {
    	final ScheduledTask scheduledTask = new ScheduledTask();
    	scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), new Date());
    	return scheduledTask;
    }

    @Override
    public void destroy() throws Exception {
        for (final ScheduledTask task : this.scheduledTaskMap.values()) {
            task.cancel();
        }
        this.scheduledTaskMap.clear();
    }
}
