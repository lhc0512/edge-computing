package cn.edu.scut.util;

import org.springframework.context.ApplicationContext;

public class SpringBeanUtils {
    public static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringBeanUtils.applicationContext = applicationContext;
    }
}
