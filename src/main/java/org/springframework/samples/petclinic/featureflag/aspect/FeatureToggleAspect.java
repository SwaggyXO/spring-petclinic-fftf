package org.springframework.samples.petclinic.featureflag.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.featureflag.annotation.FeatureToggle;
import org.springframework.samples.petclinic.featureflag.exception.FeatureDisabledException;
import org.springframework.samples.petclinic.featureflag.model.FlagContext;
import org.springframework.samples.petclinic.featureflag.service.FeatureFlagEvaluator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
@Slf4j
public class FeatureToggleAspect {

	@Autowired
	private FeatureFlagEvaluator evaluator;

	@Around("@annotation(featureToggle)")
	public Object checkFeatureFlag(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {
		String flagKey = featureToggle.flagKey();

		// Build context from request
		FlagContext context = buildContextFromRequest();

		boolean isEnabled = evaluator.isEnabled(flagKey, context);

		if (!isEnabled) {
			log.warn("Feature {} is disabled, blocking execution", flagKey);
			if (featureToggle.throwException()) {
				throw new FeatureDisabledException(featureToggle.fallbackMessage());
			}
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", featureToggle.fallbackMessage()));
		}

		return joinPoint.proceed();
	}

	private FlagContext buildContextFromRequest() {
		// Extract from HttpServletRequest, SecurityContext, etc.
		ServletRequestAttributes attrs = (ServletRequestAttributes)
			RequestContextHolder.getRequestAttributes();

		if (attrs != null) {
			HttpServletRequest request = attrs.getRequest();
			return FlagContext.builder()
				.userId(request.getHeader("X-User-Id"))
				.sessionId(request.getSession().getId())
				.ipAddress(request.getRemoteAddr())
				.environment(System.getProperty("spring.profiles.active", "development"))
				.build();
		}
		return FlagContext.builder().build();
	}
}

