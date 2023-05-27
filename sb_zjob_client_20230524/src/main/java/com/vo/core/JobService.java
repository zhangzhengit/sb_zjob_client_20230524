package com.vo.core;

import java.util.Objects;
import java.util.Set;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.vo.api.ExecuteImmediatelJobDTO;
import com.vo.api.JobCache;
import com.vo.api.PushJobTypeEnum;
import com.vo.enums.StatusEnum;
import com.votool.common.CR;

import cn.hutool.core.collection.CollUtil;

/**
 *
 * job 相关操作
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Service
public class JobService {

	private final static ZLog2 LOG = ZLog2.getInstance();

	@Autowired
	private CronTaskRegistrar cronTaskRegistrar;

	public CR<Object> updateJob(final JobDTO jobDTO) {

		LOG.info("开始更新job信息，jobDTO={}", jobDTO);
		final Set<ZScheduledTask> sts = JobCache.get(jobDTO.getName());
		if (CollUtil.isEmpty(sts)) {
			LOG.error("更新job信息，job不存在,name={},jobDTO={}", jobDTO.getName(), jobDTO);
			return CR.error("job=" + jobDTO.getName() + "不存在");
		}

		for (final ZScheduledTask scheduledTask : sts) {

			if (Objects.isNull(scheduledTask)) {
				LOG.error("更新job信息，job不存在,name={},jobDTO={}", jobDTO.getName(), jobDTO);
				return CR.error("job=" + jobDTO.getName() + "不存在");
			}

			final boolean validExpression = CronExpression.isValidExpression(jobDTO.getCron());
			if (!validExpression) {
				LOG.error("更新job信息，cron错误,name={},jobDTO={}", jobDTO.getName(), jobDTO);
				return CR.error("cron错误");
			}

			final StatusEnum statusEnum = StatusEnum.valueByStatus(jobDTO.getStatus());

			LOG.info("开始更新job信息，name={},status={},cron={}", jobDTO.getName(), statusEnum, jobDTO.getCron());
			switch (statusEnum) {
			case QI_YONG:
				this.cronTaskRegistrar.addCronTask(scheduledTask, jobDTO.getCron());
				break;

			case TING_YONG:
				this.cronTaskRegistrar.removeCronTask(scheduledTask);
				break;

			default:
				break;
			}

			LOG.info("更新job信息完成，name={},status={},cron={}", jobDTO.getName(), statusEnum, jobDTO.getCron());
		}

		return CR.ok();
	}

	public CR<Object> executeImmediatel(final ExecuteImmediatelJobDTO executeImmediatelJobDTO) {

		final JobDTO jobDTO = JSON.parseObject(executeImmediatelJobDTO.getContent(),JobDTO.class);
		final String name = jobDTO.getName();
		final Set<ZScheduledTask> sts = JobCache.get(name);
		if (CollUtil.isEmpty(sts)) {
			return CR.error("job不存在");
		}

		final PushJobTypeEnum pushJobTypeEnum = PushJobTypeEnum.valueByType(executeImmediatelJobDTO.getType());
		switch (pushJobTypeEnum) {
		case EXECUTE_IMMEDIATEL:
			LOG.info("开始立即执行job,name={}", jobDTO.getName());

			// FIXME 2023年5月27日 上午2:39:43 zhanghen: ZScheduledTask加入name，用于在client区分
			LOG.info("开始executeImmediately-job,job个数={}", sts.size());
			for (final ZScheduledTask zScheduledTask : sts) {
				LOG.info("开始executeImmediately-job,name={}", jobDTO.getName());
				this.cronTaskRegistrar.executeImmediately(zScheduledTask);
				LOG.info("executeImmediately-job完成,name={}", jobDTO.getName());
			}
			break;

		case UPDATE_CONFIGURATION:
			LOG.info("开始更新job信息,name={}", jobDTO.getName());
			// FIXME 2023年5月27日 上午2:39:43 zhanghen: ZScheduledTask加入name，用于在client区分
			LOG.info("开始更新job信息,job个数={}", sts.size());

			final StatusEnum statusEnum = StatusEnum.valueByStatus(jobDTO.getStatus());
			switch (statusEnum) {
			case QI_YONG:
				for (final ZScheduledTask zScheduledTask : sts) {
					LOG.info("开始更新job信息>启用job,name={},jobDTO={}", jobDTO.getName(), jobDTO);
					this.cronTaskRegistrar.addCronTask(zScheduledTask, jobDTO.getCron());
					LOG.info("开始更新job信息启用job完成,name={},jobDTO={}", jobDTO.getName(), jobDTO);
				}
				break;

			case TING_YONG:
				for (final ZScheduledTask zScheduledTask : sts) {
					LOG.info("开始更新job信息>停用job,name={},jobDTO={}", jobDTO.getName(), jobDTO);
					this.cronTaskRegistrar.removeCronTask(zScheduledTask);
					LOG.info("开始更新job信息>停用job完成,name={},jobDTO={}", jobDTO.getName(), jobDTO);
				}

				break;

			default:
				break;
			}

			break;

		default:
			break;
		}



		return CR.ok();
	}

}
