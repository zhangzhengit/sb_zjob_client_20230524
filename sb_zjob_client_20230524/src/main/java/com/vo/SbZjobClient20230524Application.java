package com.vo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vo.core.ZLog2;

import cn.hutool.log.Log;

/**
 *	zjob-client
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@SpringBootApplication
public class SbZjobClient20230524Application {

	private final static ZLog2 LOG = ZLog2.getInstance();


	public static void main(final String[] args) {
		SpringApplication.run(SbZjobClient20230524Application.class, args);

		LOG.info("zjob_client 启动成功", args);
	}

}
