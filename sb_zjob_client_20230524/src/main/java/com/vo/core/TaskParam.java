package com.vo.core;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用在@ZJobTask 标记的方法上，表示此方法的参数，接收server传来的参数
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskParam {

	private List<Object> paramList;
}
