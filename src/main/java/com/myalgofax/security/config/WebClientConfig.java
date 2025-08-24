package com.myalgofax.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;


@Configuration
public class WebClientConfig {
     
	@Bean
	 WebClient webClient() {
	    int sizeInBytes = 50 * 1024 * 1024; // 50MB buffer

	    ExchangeStrategies strategies = ExchangeStrategies.builder()
	        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(sizeInBytes))
	        .build();

	    return WebClient.builder()
	        .exchangeStrategies(strategies)
	        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
	        .build();
	}
}
