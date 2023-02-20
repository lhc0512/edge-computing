package cn.edu.scut.config;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class HadoopConfig {

    @Value("${hadoop.hdfs.url}")
    private String hdfsUrl;

    @Bean
    public FileSystem fileSystem() {
        URI uri;
        try {
            uri = new URI(hdfsUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        var configuration = new org.apache.hadoop.conf.Configuration();
        String user = "hongcai";
        FileSystem fileSystem;
        try {
            fileSystem = FileSystem.get(uri, configuration, user);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return fileSystem;
    }
}
