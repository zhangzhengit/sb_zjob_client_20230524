package com.vo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *	server API enum
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Getter
@AllArgsConstructor
public enum APIEnum {

	HEARTBEAT("/heartbeat","心跳接口"),

	REGISTER("/register","注册client信息到server"),

	RECEIVE_LOG("/receivelog","接收client的job执行的日志"),

	PULL_JOB_INFO("/job?name=%s","根据名称获取job的配置信息"),

	;

	private String url;
	private String description;

}
