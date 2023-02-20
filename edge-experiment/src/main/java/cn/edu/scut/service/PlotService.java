package cn.edu.scut.service;

import cn.edu.scut.util.ArrayUtils;
import cn.edu.scut.util.PlotUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.tablesaw.plotly.Plot;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Service
@Slf4j
public class PlotService {

    @Autowired
    private FileSystem fileSystem;

    @Value("${hadoop.hdfs.url}")
    private String hdfsUrl;

    public void plot(ArrayList<Double> episodes, ArrayList<Double> successRates, String flag) {
        var episodes_ = ArrayUtils.toDoubleArray(episodes);
        var successRates_ = ArrayUtils.toDoubleArray(successRates);
        var figure = PlotUtils.plot(new double[][]{episodes_}, new double[][]{successRates_}, new String[]{"rl"}, "episode", "success rate");
        var path = Paths.get("results", "figure", flag + ".html");
        try {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException var2) {
                throw new UncheckedIOException(var2);
            }
            var file = path.toFile();
            Plot.show(figure, file);
        } catch (Exception e) {
            log.info("browser not support!");
        }
        try {
            fileSystem.copyFromLocalFile(true, true, new Path(path.toString()), new Path(hdfsUrl + "/" + path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
