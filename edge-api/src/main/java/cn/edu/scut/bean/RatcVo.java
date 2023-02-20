package cn.edu.scut.bean;

import lombok.Data;

@Data
public class RatcVo {
    private String edgeId;
    private Double executionFailureRate;
    private Long capacity;
    private Long waitingTime;
    private Long totalTime;
}
