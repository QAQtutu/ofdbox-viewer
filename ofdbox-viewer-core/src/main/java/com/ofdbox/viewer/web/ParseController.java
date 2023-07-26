package com.ofdbox.viewer.web;

import com.ofdbox.viewer.task.Task;
import com.ofdbox.viewer.task.TaskManage;
import com.ofdbox.viewer.task.UserOptions;
import com.ofdbox.viewer.task.constant.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;

@RestController
public class ParseController {

    @Value("${store-path}")
    private String storePath;

    @Autowired
    TaskManage taskManage;

    @PostMapping("/upload")
    public ResponseEntity<Task> upload(MultipartFile file, @Valid UserOptions userOptions, HttpServletRequest request) {
        //校验参数
        if (userOptions.getType() == Type.FILE) {
            if (file == null || !file.getOriginalFilename().endsWith(".ofd"))
                return ResponseEntity.badRequest().build();
        } else if (userOptions.getType() == Type.URL) {
            if (userOptions.getUrl() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        Task task = null;
        //将文件拷贝到工作目录
        try {
            if (userOptions.getType() == Type.FILE) {
                task = Task.fromFile(userOptions, file, new File(storePath));
            } else {
                task = Task.fromUrl(userOptions, new File(storePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }

        //启动任务
        taskManage.runTask(task);

        return ResponseEntity.ok(task);
    }
}
