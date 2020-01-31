package com.example.demojsonlogging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FakeNewsReqDTO {
    private String category;
    private String type;
}
