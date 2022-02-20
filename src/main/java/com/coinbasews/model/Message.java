package com.coinbasews.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Message {
    String type;
    List<String> product_ids;
    List<String> channels;

}
