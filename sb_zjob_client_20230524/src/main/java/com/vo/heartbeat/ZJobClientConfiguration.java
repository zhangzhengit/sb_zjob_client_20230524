package com.vo.heartbeat;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.checkerframework.checker.units.qual.min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ZJobClientConfiguration
 *
 * @author zhangzhen
 * @date 2023年5月27日
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@Validated
@ConfigurationProperties(prefix = "zjob.client")
public class ZJobClientConfiguration {

	@NotNull(message = "second 不能配置为空")
	@Min(value = 1, message = "second 最小配置为1")
	private Integer second = 3;

//	@NotEmpty(message = "zjob.client.cron 不能配置为空")
//	private String cron = "0/3 * * * * ?";

	private String name;

}
