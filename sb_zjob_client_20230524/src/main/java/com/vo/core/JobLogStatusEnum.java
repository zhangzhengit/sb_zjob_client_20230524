package com.vo.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * job执行状态 1-正常 2-失败
 *
 * @author zhangzhen
 * @date 2023年5月27日
 *
 */
@Getter
@AllArgsConstructor
public enum JobLogStatusEnum {

	SUCCESS(1),

	EXCEPTION(2),;

	private Integer status;

}
