package com.ofdbox.viewer.web;

import com.ofdbox.viewer.task.Task;
import com.ofdbox.viewer.task.TaskManage;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
public class ImageController {

    @Value("${store-path}")
    private String storePath;

    @Autowired
    TaskManage taskManage;

    @GetMapping("/image/{id}/{page}")
    public void getImage(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String uid, @PathVariable("page") String page) {
        File file = null;
        try {
            file = new File(getWorkdir(uid), "images/" + page).getCanonicalFile();
            if (!file.getCanonicalPath().startsWith(new File(storePath).getCanonicalPath()) || !file.exists()) {
                response.setStatus(404);
                return;
            }
            if (page.toLowerCase().endsWith(".svg")) {
                response.setHeader("Content-Type", "application/xml");
            }
        } catch (IOException e) {
            response.setStatus(500);
            return;
        }

        response.setHeader("Content-Disposition", "attachment;Filename=" + page);
        try (OutputStream outputStream = response.getOutputStream();
             FileInputStream inputStream = new FileInputStream(file);) {
            IOUtils.copy(inputStream, outputStream);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(500);
        return;
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<Task> queryTask(@PathVariable("id") String uid) {
        Task task = taskManage.getTask(uid);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    public File getWorkdir(String uid) {
        return new File(storePath, uid.substring(0, 8) + "/" + uid);
    }

}
