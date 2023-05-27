package com.vo.core;

import java.util.Date;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  注册client信息到server
 *
 * @author zhangzhen
 * @date 2023年5月27日
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ClientRegDTO {

	private Integer port;

	private String host;

	private java.util.List<String> jobNameList;

	private Date heartbeatTime;

}
