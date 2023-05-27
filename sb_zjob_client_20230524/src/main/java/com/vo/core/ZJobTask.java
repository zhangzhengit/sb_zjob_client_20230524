package com.vo.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 此注解用@ZJobComponent对象的方法上，表示此方法是一个具体的zjob任务
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ZJobTask {

	/**
	 * job名称，多个task方法的name可以重复，可以用来执行不同的逻辑
	 *
	 * @return
	 *
	 */
	String name();

	/**
	 * job的唯一ID
	 *
	 * @return
	 *
	 */
	String id();

	/**
	 * 描述此job
	 *
	 * @return
	 *
	 */
	String description();
}
