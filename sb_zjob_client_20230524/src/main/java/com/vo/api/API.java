package com.vo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vo.core.JobDTO;
import com.vo.core.JobService;
import com.vo.core.ZLog2;
import com.votool.common.CR;

import io.netty.util.concurrent.FailedFuture;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@RestController
public class API {

	private final static ZLog2 LOG = ZLog2.getInstance();

	@Autowired
	private JobService jobService;

	@GetMapping
	public String index() {
		return "zjob_client_index";
	}

	@PostMapping(value = "/receive")
	public CR<Object> receive(@RequestBody @Validated final JobDTO jobDTO) {
		LOG.info("收到推送的job配置，jobDTO={}", jobDTO);

		final CR<Object> cr = this.jobService.updateJob(jobDTO);
		LOG.info("推送的job配置更新完成，name={},jobDTO={}", jobDTO.getName(), jobDTO);
		return cr;
	}

	/**
	 * 立即执行job/立即更新job最新信息
	 *
	 * @param executeImmediatelDTO
	 * @return
	 *
	 */
	@PostMapping(value = "/executeImmediatel")
	public CR executeImmediatel(
			@RequestBody @Validated final ExecuteImmediatelJobDTO executeImmediatelDTO) {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "API.executeImmediatel()");

		final CR<Object> cr = this.jobService.executeImmediatel(executeImmediatelDTO);
		return cr;
	}

}
