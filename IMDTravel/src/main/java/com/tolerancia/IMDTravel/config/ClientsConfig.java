package com.tolerancia.IMDTravel.config;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.tolerancia.IMDTravel.component.CorrelationAndLoggingFilter;

@Configuration
public class ClientsConfig {

    private ClientHttpRequestInterceptor correlationInterceptor() {
        return (request, body, execution) -> {
            String cid = MDC.get("cid");
            if (cid != null && !cid.isBlank()) {
                request.getHeaders().set(CorrelationAndLoggingFilter.HEADER, cid);
            }
            return execution.execute(request, body);
        };
    }

    @Bean
    RestClient airlinesClient(@Value("${services.airlines.url:http://airlineshub:8080}") String url) {
        return RestClient.builder().baseUrl(url).requestInterceptor(correlationInterceptor()).build();
    }

    @Bean
    RestClient exchangeClient(@Value("${services.exchange.url:http://exchange:8080}") String url) {
        return RestClient.builder().baseUrl(url).requestInterceptor(correlationInterceptor()).build();
    }

    @Bean
    RestClient fidelityClient(@Value("${services.fidelity.url:http://fidelity:8080}") String url) {
        return RestClient.builder().baseUrl(url).requestInterceptor(correlationInterceptor()).build();
    }
}
