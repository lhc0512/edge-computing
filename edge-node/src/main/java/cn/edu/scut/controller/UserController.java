package cn.edu.scut.controller;

import cn.edu.scut.bean.Task;
import cn.edu.scut.service.EdgeNodeSystemService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/user", method = {RequestMethod.GET, RequestMethod.POST})
@CommonsLog
public class UserController {
    @Resource
    EdgeNodeSystemService edgeNodeSystemService;

    @PostMapping("/task")
    public String receiveUserTask(@RequestBody Task task) {
        log.info("receive task from user");
        task.setArrivalTime(LocalDateTime.now());
        edgeNodeSystemService.processUserTask(task);
        return "success";
    }
}
