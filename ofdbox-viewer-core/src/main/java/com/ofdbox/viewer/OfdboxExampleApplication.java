package com.ofdbox.viewer;

import com.ofdbox.convertor.utils.FontUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@EnableScheduling
@Configuration
@SpringBootApplication
public class OfdboxExampleApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(OfdboxExampleApplication.class, args);
    }


    @Value("${font-dir}")
    private String fontDir;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File dir = new File(fontDir);
        FontUtils.scanFontDir(dir);
        FontUtils.addAliasMapping("null", "仿宋简体", "null", "方正仿宋简体");
        FontUtils.addAliasMapping("null", "小标宋体", "方正小标宋简体", "方正小标宋简体");
    }
}
