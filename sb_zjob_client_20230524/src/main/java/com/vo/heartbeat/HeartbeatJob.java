package com.vo.heartbeat;


import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.vo.core.ClientRegDTO;
import com.vo.core.CronTaskRegistrar;
import com.vo.core.MQISA;
import com.vo.core.ServerConf;
import com.vo.core.TaskParam;
import com.vo.core.ZJobComponent;
import com.vo.core.ZLog2;
import com.vo.enums.APIEnum;

import cn.hutool.http.HttpRequest;

/**
 * zjob-client的心跳job
 *
 * @author zhangzhen
 * @date 2023年5月27日
 *
 */
@ZJobComponent
public class HeartbeatJob implements InitializingBean {

	private final static ZLog2 LOG = ZLog2.getInstance();

	@Autowired
	private ServerConf serverConf;
	@Autowired
	private MQISA mqisa;
	@Autowired
	private CronTaskRegistrar cronTaskRegistrar;
	@Autowired
	private ZJobClientConfiguration jobClientConfiguration;

//	@ZJobTask(name = "zjobClientHeartbeat", id = "zjobClientHeartbeat", description = "zjob-client的心跳job")
	public void zjobClientHeartbeat(final TaskParam taskParam) {
		this.heartbeat();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {

				HeartbeatJob.this.heartbeat();

			}


		}, this.jobClientConfiguration.getSecond(), this.jobClientConfiguration.getSecond(), TimeUnit.SECONDS);

	}

	private void heartbeat() {
//		LOG.debug("心跳");

		final APIEnum heartbeat = APIEnum.HEARTBEAT;
		final String url = HeartbeatJob.this.serverConf.getHost() + ":" + HeartbeatJob.this.serverConf.getPort() + heartbeat.getUrl();

		final ClientRegDTO clientRegDTO = HeartbeatJob.this.mqisa.getClientRegDTOReference().get();
		clientRegDTO.setHeartbeatTime(new Date());
		HttpRequest.post(url)
				.body(JSON.toJSONString(clientRegDTO)).execute().body();
	}

}
