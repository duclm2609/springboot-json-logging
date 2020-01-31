package com.example.demojsonlogging.configuration;

import com.example.demojsonlogging.filter.DemoLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingFilterConfiguration {

    @Bean
    public DemoLoggingFilter requestLogFilter() {
        DemoLoggingFilter loggingFilter = new DemoLoggingFilter();
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(2048);
        loggingFilter.setShouldLogBefore(true);
        loggingFilter.setHeaderPredicate(
                t -> t.equals("user-agent") || t.equals("host") || t.equals("accept-encoding")
        );
        return loggingFilter;
    }
}
