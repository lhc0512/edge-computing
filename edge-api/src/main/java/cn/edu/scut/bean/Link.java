package cn.edu.scut.bean;

import lombok.Data;

@Data
public class Link {
    private Integer id;
    private String source;
    private String destination;
    private Double transmissionRate;
    private Double transmissionFailureRate;
}
