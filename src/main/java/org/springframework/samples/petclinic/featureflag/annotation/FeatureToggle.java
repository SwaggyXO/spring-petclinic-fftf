package org.springframework.samples.petclinic.featureflag.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureToggle {
	String flagKey();
	String fallbackMessage() default "This feature is currently disabled";
	boolean throwException() default false;
}
