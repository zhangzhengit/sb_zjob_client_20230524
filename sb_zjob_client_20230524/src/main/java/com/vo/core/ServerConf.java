package com.vo.core;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * server 配置
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@Validated
@ConfigurationProperties(prefix = "zjob.server")
public class ServerConf {

	@NotEmpty(message = "zjob.server.host不能配置为空")
	private String host;

	@NotNull(message = "zjob.server.port不能配置为空")
	private Integer port;
}
