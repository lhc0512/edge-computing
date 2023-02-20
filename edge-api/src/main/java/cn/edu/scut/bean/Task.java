package cn.edu.scut.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private Integer timeSlot;
    private String source;
    private String destination;
    private TaskStatus status;
    // KB
    private Long taskSize;
    private Long taskComplexity;
    // cycle
    private Long cpuCycle;  // cpuCycle = taskSize * taskComplexity
    // s
    private Long deadline;
    private Long transmissionTime;
    private Long executionTime;
    private Long transmissionWaitingTime;
    private Long executionWaitingTime;

    @TableField(exist = false)
    private LocalDateTime arrivalTime;
    @TableField(exist = false)
    private LocalDateTime beginTransmissionTime;
    @TableField(exist = false)
    private LocalDateTime endTransmissionTime;
    @TableField(exist = false)
    private LocalDateTime beginExecutionTime;
    @TableField(exist = false)
    private LocalDateTime endExecutionTime;
    @TableField(exist = false)
    private Double transmissionFailureRate;
    @TableField(exist = false)
    private Double executionFailureRate;

    private String availAction;
}
