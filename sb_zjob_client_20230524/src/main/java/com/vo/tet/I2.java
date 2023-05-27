package com.vo.tet;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
public class I2 implements I{

	@Override
	public void task() {
		// TODO Auto-generated method stub

	}

	public static void main(final String[] args) {
		final ServiceLoader<I> ss = ServiceLoader.load(I.class);
		System.out.println("ss = " + ss);
		final Iterator<I> iterator = ss.iterator();
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}
}
