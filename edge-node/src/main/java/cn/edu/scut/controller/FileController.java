package cn.edu.scut.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@Slf4j
public class FileController {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String upload(HttpServletRequest request) throws IOException, ServletException {
        for (Part part : request.getParts()) {
            log.info("content type: {}", part.getContentType());  //text/plain
            log.info("parameter name: {}", part.getName()); // actor
            log.info("file name: {}", part.getSubmittedFileName()); // actor.param
            log.info("file size: {}", part.getSize());
            log.info("save file");
            InputStream inputStream = part.getInputStream();
            var path = Paths.get("results/model/edge-node-1/"+ part.getSubmittedFileName());
            Files.createDirectories(path.getParent());
            Files.write(path, StreamUtils.copyToByteArray(inputStream));
        }
        return "success";
    }
}
