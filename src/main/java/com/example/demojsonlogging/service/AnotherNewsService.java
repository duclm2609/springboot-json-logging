package com.example.demojsonlogging.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AnotherNewsService {

    @Async
    public void subTask() {
        log.info("Executing sub task of fake news");
    }
}
