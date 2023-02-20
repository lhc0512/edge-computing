package cn.edu.scut.controller;

import cn.edu.scut.service.EdgeNodeSystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/cmd", method = {RequestMethod.GET, RequestMethod.POST})
public class CmdController {

    @Resource
    EdgeNodeSystemService edgeNodeSystemService;

    @GetMapping("/init")
    public String init(){
        edgeNodeSystemService.init();
        return "success";
    }
}
