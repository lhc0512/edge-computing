package cn.edu.scut.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class EdgeNode {
    private Integer id;
    private String name;
    private Long cpuNum;
    @TableField(exist = false)
    private Long capacity; //  capacity = cpuNum * cpuCapacity
    private Double taskRate;
    private Double executionFailureRate;
}