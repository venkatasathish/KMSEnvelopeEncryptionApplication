package com.mykmsenvelope.KMSEnvelopeEncryptionApplication.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

@Configuration
@EnableCaching
public class CacheConfig {

	  @Bean
	    public CacheManager cacheManager() {
	        return new ConcurrentMapCacheManager(){
	        	protected Cache createConcurrentMapCache(final String name) {
	        		return new ConcurrentMapCache(name,CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1000).build().asMap(),false);
	        	}
	        };
	    }
	
	
}
