package com.example.library.model;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class SubscriptionDTO {
    private String author;
    private String category;
}