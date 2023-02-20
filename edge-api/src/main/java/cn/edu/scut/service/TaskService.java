package cn.edu.scut.service;

import cn.edu.scut.bean.Task;
import cn.edu.scut.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    @Autowired
    private TaskMapper taskMapper;

    public float getSuccessRate(Map<String, Object> map) {
        Long successTasks = taskMapper.selectCount(new QueryWrapper<Task>().eq("status", "SUCCESS").gt("id", map.get("startIndex")));
        Long totalTasks = taskMapper.selectCount(new QueryWrapper<Task>().gt("id", map.get("startIndex")));
        return Float.valueOf(successTasks) / Float.valueOf(totalTasks);
    }

    public double getSuccessRate() {
        Long successTasks = taskMapper.selectCount(new QueryWrapper<Task>().eq("status", "SUCCESS"));
        Long totalTasks = taskMapper.selectCount(new QueryWrapper<Task>()) - taskMapper.selectCount(new QueryWrapper<Task>().eq("status", "EMPTY"));
        return Double.valueOf(successTasks) / Double.valueOf(totalTasks);
    }
}
