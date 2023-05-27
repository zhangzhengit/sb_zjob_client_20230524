package com.vo.tet;

import java.io.IOException;

import org.springframework.scheduling.annotation.Scheduled;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年5月24日
 *
 */
public class JavassistTest1 {

	public static void main(final String[] args) throws Exception {
		test_1();
	}

	public static void test_1() throws NotFoundException, CannotCompileException, IOException, InstantiationException, IllegalAccessException {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "JavassistTest1.test_1()");

//		new B().hello();

		final ClassPool classPool = ClassPool.getDefault();
		final CtClass b = classPool.get("com.vo.tet.B");
		System.out.println(b);

		final ClassFile classFile = b.getClassFile();
		final ConstPool constPool = classFile.getConstPool();

		final CtMethod helloMethod = b.getDeclaredMethod("hello");

		System.out.println("helllomethod  = " + helloMethod);


		final MethodInfo methodInfo = helloMethod.getMethodInfo();
		System.out.println("methodInfo = " + methodInfo);

		  final AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        //创建要添加的注解
        final Annotation jsonFileAnnotation = new Annotation(Scheduled.class.getCanonicalName(), constPool);
        //设置注解中的属性和值
		jsonFileAnnotation.addMemberValue("cron", new StringMemberValue("from-javassist", constPool));
        //把这个注解放到一个AnnotationsAttribute对象里面
        annotationsAttribute.addAnnotation(jsonFileAnnotation);
        //把这个对象怼到要打上这个注解的字段/类上面
        helloMethod.getMethodInfo().addAttribute(annotationsAttribute);


		helloMethod.setBody("System.out.println(\"这是javassist动态修改了源码以后输出的内容\");");

		b.writeFile();

		final B b2 = (B) b.toClass().newInstance();

		System.out.println("------------hello开始执行---------------");
		b2.hello();

	}

}
