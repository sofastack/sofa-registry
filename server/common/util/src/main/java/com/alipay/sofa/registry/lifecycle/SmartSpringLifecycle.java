package com.alipay.sofa.registry.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhuchen
 * @date Nov 24, 2020, 4:24:34 PM
 *
 * Annotation for whom install, an auto lifecycle start/end process
 * will triggered through spring PostConstruct and PreDestroy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SmartSpringLifecycle {
}
