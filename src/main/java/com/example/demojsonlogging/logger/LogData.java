package com.example.demojsonlogging.logger;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogData {
    private Tag message_tag;
    private Object data;
}
