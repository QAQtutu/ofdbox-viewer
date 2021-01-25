package com.ofdbox.viewer.web;

import com.ofdbox.viewer.task.Task;
import com.ofdbox.viewer.task.TaskContext;
import com.ofdbox.viewer.task.TaskManage;
import com.ofdbox.viewer.task.Type;
import com.ofdbox.viewer.utils.Utils;
import com.ofdbox.viewer.utils.Wapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class ParseController {

    @Value("${store-path}")
    private String storePath;

    @Autowired
    TaskManage taskManage;

    @PostMapping("/upload")
    public ResponseEntity upload(MultipartFile file, @Valid TaskContext context, HttpServletRequest request) {
        System.out.println(context);

        //校验参数
        if (context.getType() == Type.FILE) {
            if (file == null || !file.getOriginalFilename().endsWith(".ofd"))
                return ResponseEntity.badRequest().build();
        } else if (context.getType() == Type.URL) {
            if (context.getUrl() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        //创建工作目录
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uid = date + Utils.uuid();
        File workdir = new File(new File(storePath), date + "/" + uid);
        try {
            FileUtils.forceMkdir(workdir);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }

        //将文件拷贝到工作目录
        if (context.getType() == Type.FILE) {
            File ofdFile = new File(workdir, file.getOriginalFilename());
            try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(ofdFile);) {
                IOUtils.copy(in, out);
                context.setFilename(file.getOriginalFilename());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }


        context.setUid(uid);
        context.setCreateTime(System.currentTimeMillis());

        //启动任务
        taskManage.runTask(context, workdir);

        return ResponseEntity.ok(context);
    }
}
