package com.example.demojsonlogging.service;

import com.example.demojsonlogging.dto.FakeNews;
import com.example.demojsonlogging.logger.LogData;
import com.example.demojsonlogging.logger.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.fields;

@Service
@Slf4j
public class FakeNewsService {

    @Autowired
    AnotherNewsService anotherNewsService;

    public FakeNews getFakeNews() {
        FakeNews fakeNews = new FakeNews(1, "U23 Việt Nam vô địch!");
        log.info("Return news: {}", fakeNews, fields(
                LogData.builder()
                        .message_tag(Tag.GATEWAY)
                        .data(fakeNews)
                        .build()));
        return fakeNews;
    }

    @Async
    public void someAsyncJob(int id) {
        log.info("Doing in async job id={}", id);
        if (id == 1) {
            this.anotherNewsService.subTask();
        }
    }
}
