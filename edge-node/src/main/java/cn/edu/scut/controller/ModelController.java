package cn.edu.scut.controller;

import cn.edu.scut.agent.IMAAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;

@RestController
@Slf4j
public class ModelController {

    // heuristic
    @Autowired(required = false)
    private IMAAgent agent;

    @GetMapping("/updateParam/{flag}")
    public String updateParam(@PathVariable("flag") String flag) {
        agent.loadHdfsModel(flag);
        return "success";
    }

    @PostMapping("/updateModel")
    public String updateStreamParam(HttpServletRequest request) throws IOException, ServletException {
        for (Part part : request.getParts()) {
            log.info("content type: {}", part.getContentType());  //text/plain
            log.info("parameter name: {}", part.getName()); // actor
            log.info("file name: {}", part.getSubmittedFileName()); // ctor.param
            log.info("file size: {}", part.getSize());
            agent.loadSteamModel(part.getInputStream(), part.getSubmittedFileName());
            log.info("update model completed!");
        }
        return "success";
    }

    @GetMapping("/loadModel/{flag}")
    public String loadLocalModel(@PathVariable("flag") String flag) {
        agent.loadModel(flag);
        return "success";
    }
}