package org.springframework.samples.petclinic.featureflag.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
public class FeatureFlagConfig {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("vets", "featureFlags");
	}
}
