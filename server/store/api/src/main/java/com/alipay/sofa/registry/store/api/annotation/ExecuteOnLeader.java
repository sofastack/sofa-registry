package com.alipay.sofa.registry.store.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author chen.zhu
 * <p>
 * Dec 13, 2020
 */

@Target({ ElementType.METHOD })
@Retention(RUNTIME)
public @interface ExecuteOnLeader {
}
