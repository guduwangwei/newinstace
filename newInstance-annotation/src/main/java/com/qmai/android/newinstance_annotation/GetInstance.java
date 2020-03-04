package com.qmai.android.newinstance_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by wangwei   ON 3/2/20  email:wangwei_5521@163.com
 *
 * @version 1.1.1
 * @Description
 **/
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface GetInstance {

    String path();

}
