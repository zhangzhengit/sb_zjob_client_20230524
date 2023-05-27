package com.vo.core;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vo.api.AddJobLogDTO;
import com.vo.api.JobCache;
import com.vo.enums.APIEnum;
import com.vo.enums.StatusEnum;
import com.votool.common.CR;

import cn.hutool.http.HttpRequest;
import lombok.Getter;

/**
 * 查找 @ZJobComponent 的bean的 @ZJobTask 方法，然后拉取配置信息，注册为一个task
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class MQISA implements ApplicationContextAware {

	public static final String SUCCESS = "SUCCESS";

	private final static ZLog2 LOG = ZLog2.getInstance();

	@Getter
	private final AtomicReference<ClientRegDTO> clientRegDTOReference = new AtomicReference<>();

	@Autowired
	private ServerConf serverConf;
	@Autowired
	private CronTaskRegistrar cronTaskRegistrar;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		LOG.info("执行");
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				LOG.info("执行");
				MQISA.this.f1(applicationContext);
			}
		});

		thread.start();
	}

	@Value("${server.port}")
	private Integer port;

	private static String getLostHost()  {
		try {
			InetAddress localHost;
			localHost = InetAddress.getLocalHost();
			return localHost.getHostAddress();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void f1(final ApplicationContext applicationContext) {
		LOG.info("执行");
		LOG.info("开始查找带有@{}注解的bean", ZJobComponent.class.getName());

		final String[] beanNameArray = applicationContext.getBeanDefinitionNames();
		final List<ZJobTask> jobTaskList = this.pull(applicationContext, beanNameArray);

		LOG.info("开始注册client信息到server,server={}", this.serverConf);

		final ClientRegDTO clientRegDTO = new ClientRegDTO();
		clientRegDTO.setHost(getLostHost());
		clientRegDTO.setPort(this.port);
		final Set<String> jobNameSet = jobTaskList.stream().map(a -> a.name()).collect(Collectors.toSet());
		clientRegDTO.setJobNameList(Lists.newArrayList(jobNameSet));
		this.clientRegDTOReference.set(clientRegDTO);

		LOG.info("开始注册client信息到server,client={}", clientRegDTO);

		final String urlR = this.serverConf.getHost() + ":" + this.serverConf.getPort() + APIEnum.REGISTER.getUrl();
		final String regCR = HttpRequest.post(urlR).body(JSON.toJSONString(clientRegDTO)).execute().body();

		LOG.info("注册client结果={}", regCR);

	}

	private List<ZJobTask> pull(final ApplicationContext applicationContext, final String[] beanNameArray) {

		final List<ZJobTask> list = Lists.newArrayList();

		final Set<String> jobIdSet = Sets.newHashSet();

		for (final String beanName : beanNameArray) {
			final Object bean = applicationContext.getBean(beanName);
			final ZJobComponent zJobComponent = bean.getClass().getDeclaredAnnotation(ZJobComponent.class);
			if (zJobComponent == null) {
				continue;
			}

			LOG.info("找到一个zJobComponent 对象 beanName={}", beanName);
			final Method[] methodArray = bean.getClass().getDeclaredMethods();
			for (final Method method : methodArray) {

				final ZJobTask zJobTask = AnnotationUtils.findAnnotation(method, ZJobTask.class);
				if (zJobTask == null) {
					continue;
				}

				LOG.info("找到一个zJobTask 对象 methodName={}", method.getName());
				list.add(zJobTask);

				final boolean add = jobIdSet.add(zJobTask.id());
				if (!add) {
					throw new IllegalArgumentException("@ZJobTask.id()重复,@ZJobTask=" + zJobTask);
				}

				MQISA.checkTaskParam(method);

				final String jobName = zJobTask.name();
				final String urlX = String.format(APIEnum.PULL_JOB_INFO.getUrl(), jobName);
				final String url = this.serverConf.getHost() + ":" + this.serverConf.getPort() + urlX;

				LOG.info("开始拉取job配置信息,job-name={},url={}", jobName, url);


				final String body = this.pullJobInfo(url);
				final CR<JobDTO> cr = JSON.parseObject(body,new TypeReference<CR<JobDTO>>(){});
				if (cr.getCode() != CR.CODE_OK) {
					LOG.error("拉取job配置信息出错，job-name={},cr={}", jobName, cr);
					continue;
				}

				final JobDTO jobDTO = cr.getData();
				if (Objects.isNull(jobDTO)) {
					LOG.error("拉取job配置信息-无job信息，job-name={},cr={}", jobName, cr);
					continue;
				}

				// @ZJobTask 注册为一个task
				final String cron = jobDTO.getCron();

				final ZScheduledTask scheduledTask = this.buildZST(bean, method, zJobTask);
				JobCache.put(jobName, scheduledTask);

				if (StatusEnum.QI_YONG.getStatus().equals(jobDTO.getStatus())) {
					LOG.info("job开始注册，job-name={},cron={}", jobName, jobDTO.getCron());
					this.cronTaskRegistrar.addCronTask(scheduledTask, cron);
					LOG.info("job注册成功，job-name={},cron={}", jobName, jobDTO.getCron());
				} else {
					LOG.warn("job未启用，不执行.job-name={}", jobName);
				}

			}
		}

		return list;
	}

	private String pullJobInfo(final String url) {
		try {

			final String body = HttpRequest.get(url).execute().body();
			return body;
		} catch (final Exception e) {
			e.printStackTrace();
			LOG.error("连接zjob-server 失败,server={}", this.serverConf.getHost() + ":" + this.serverConf.getPort());
			System.exit(0);
		}

		return null;
	}

	private  ZScheduledTask buildZST(final Object bean, final Method method, final ZJobTask zJobTask) {

		return new ZScheduledTask() {

			@Override
			public void run() {
				Date executionTime =null;
				Date endTime =null;
				try {
					final Object[] args = { null };
					final String as = Arrays.toString(args);

					LOG.info("job开始执行,name={},args={}", zJobTask.name(), as);

					executionTime = new Date();
					method.invoke(bean, args);
					endTime = new Date();

					LOG.info("job执行结束,name={},args={}", zJobTask.name(), as);

					final String urlX = APIEnum.RECEIVE_LOG.getUrl();
					final String url = MQISA.this.serverConf.getHost() + ":" + MQISA.this.serverConf.getPort() + urlX;
					final AddJobLogDTO addJobLogDTO = MQISA.this.newAddJobLogDTO(zJobTask, SUCCESS, JobLogStatusEnum.SUCCESS, executionTime, endTime, null);
					final String json = JSON.toJSONString(addJobLogDTO);
					HttpRequest.post(url).body(json).execute().body();

				} catch (final Exception e) {

					final Date exceptionTime = new Date();
					endTime = exceptionTime;

					final StringWriter stringWriter = new StringWriter();
					e.printStackTrace(new PrintWriter(stringWriter, true));
					final String eMessage = stringWriter.toString();

					LOG.error("job执行异常,name={},e.message{}", zJobTask.name(), eMessage);

					final String urlX = APIEnum.RECEIVE_LOG.getUrl();
					final String url = MQISA.this.serverConf.getHost() + ":" + MQISA.this.serverConf.getPort() + urlX;

					final AddJobLogDTO addJobLogDTO = MQISA.this.newAddJobLogDTO(zJobTask, eMessage, JobLogStatusEnum.EXCEPTION, executionTime, endTime, exceptionTime);
					final String json = JSON.toJSONString(addJobLogDTO);
					HttpRequest.post(url).body(json).execute().body();

				}
			}

		};
	}

	private AddJobLogDTO newAddJobLogDTO(final ZJobTask zJobTask, final String jobLog, final JobLogStatusEnum jobLogStatusEnum, final Date executionTime, final Date endTime, final Date exceptionTime) {
		final AddJobLogDTO addJobLogDTO = new AddJobLogDTO();
		addJobLogDTO.setHost(MQISA.this.serverConf.getHost());
		addJobLogDTO.setPort(String.valueOf(MQISA.this.serverConf.getPort()));
		addJobLogDTO.setJobId(zJobTask.id());
		addJobLogDTO.setDescription(zJobTask.description());
		addJobLogDTO.setJobName(zJobTask.name());
		addJobLogDTO.setExecutionTime(executionTime);
		addJobLogDTO.setEndTime(endTime);
		addJobLogDTO.setJobLog(jobLog);
		addJobLogDTO.setExceptionTime(exceptionTime);
		addJobLogDTO.setStatus(jobLogStatusEnum.getStatus());

		return addJobLogDTO;
	}

	private static boolean checkTaskParam(final Method method) {
		final int parameterCount = method.getParameterCount();
		if (parameterCount != 1) {
			throw new IllegalArgumentException(
					"@ZJobTask方法 [" + method.getName() + "] 必须声明为(TaskParam taskParam)");
		}

		if (!method.getParameters()[0].getParameterizedType().getTypeName().equals(TaskParam.class.getCanonicalName())) {
			throw new IllegalArgumentException(
					"@ZJobTask方法 [" + method.getName() + "] 必须声明为(TaskParam taskParam)");
		}
		return true;
	}

	static final ObjectMapper mapper = new ObjectMapper();

	private static CR<JobDTO> parse(final String v) {
		CR<JobDTO> cr = null;
		try {
			cr = mapper.readValue(v, new com.fasterxml.jackson.core.type.TypeReference<CR<JobDTO>>() {
			});
		} catch (final JsonProcessingException e) {
			e.printStackTrace();
		}

		return cr;
	}

}
