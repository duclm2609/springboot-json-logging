package com.example.demojsonlogging.controller;

import com.example.demojsonlogging.dto.FakeNews;
import com.example.demojsonlogging.dto.FakeNewsReqDTO;
import com.example.demojsonlogging.exception.BusinessRuntimeException;
import com.example.demojsonlogging.service.FakeNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
public class FakeNewsController {

    private final FakeNewsService service;

    @GetMapping
    public FakeNews getNews() {
        return service.getFakeNews();
    }

    @GetMapping("/exception")
    public void getNewsButException() {
        try {
            throw new IllegalArgumentException("Incorrect news id");
        } catch (Exception e) {
            log.error("Error getting news", new RuntimeException("I dont know", new BusinessRuntimeException("Internal server error", e)));
        }
    }

    @PostMapping
    public FakeNewsReqDTO createFakeNews(@RequestBody FakeNewsReqDTO reqDTO) {
        log.info("Create new fake news");
        return reqDTO;
    }

    @GetMapping("/async")
    public void fakeAsyncTracing() {
        for (int i = 0; i < 2; i++) {
            this.service.someAsyncJob(i);
        }
    }

    @GetMapping("/async-controller")
    public Callable<String> testAsyncController() {
        return () ->
        {
            log.info("Async endpoint: started!");
            Thread.sleep(ThreadLocalRandom.current().nextInt(5000));
            log.info("Async endpoint: completed!");
            return "Hello World !!";
        };
    }
}
