package cn.edu.scut.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {
    public static void recursiveDelete(File file) {
        if (!file.exists())
            return;

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                // 调用递归
                recursiveDelete(f);
            }
        }
        file.delete();
        log.info("delete file/folder: {}", file.getAbsolutePath());
    }

    public static void writeObject(Object data, Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream f = new FileOutputStream(path.toFile());
             ObjectOutput s = new ObjectOutputStream(f)) {
            s.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object readObject(Path path){
        try (FileInputStream in = new FileInputStream(path.toFile());
             ObjectInputStream s = new ObjectInputStream(in)) {
            return s.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
