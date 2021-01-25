package com.ofdbox.viewer.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ofdbox.viewer.task.Task;
import com.ofdbox.viewer.task.TaskContext;
import com.ofdbox.viewer.task.TaskManage;
import com.ofdbox.viewer.utils.Wapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@RestController
public class ImageController {

    @Value("${store-path}")
    private String storePath;

    @Autowired
    TaskManage taskManage;

    @GetMapping("/image/{id}/{page}")
    public void getImage(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String uid, @PathVariable("page") Integer page) {

        File file = new File(getWorkdir(uid), "images/" + page + ".png");

        if (!file.exists()) {
            response.setStatus(404);
            return;
        }

        response.setHeader("Content-Disposition", "attachment;Filename=" + page + ".png");
        try {
            OutputStream outputStream = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(500);
        return;
    }

    @GetMapping("/task/{id}")
    public ResponseEntity queryTask(@PathVariable("id") String uid) {
        TaskContext context = taskManage.queryTask(uid);
        if (context != null) {
            return ResponseEntity.ok(context);
        }

        File workdir = getWorkdir(uid);
        File allover = new File(workdir, "allover");
        File jsonFile = new File(workdir, "context.json");
        if (!allover.exists() || !jsonFile.exists())
            return ResponseEntity.notFound().build();

        String json = readString(jsonFile);
        JSONObject object = JSON.parseObject(json);

        return ResponseEntity.ok(object);
    }

    private String readString(File file) {
        String res = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                res += str;
            }
            return res;
        } catch (IOException e) {
            return null;
        }
    }

    public File getWorkdir(String uid) {
        return new File(storePath, uid.substring(0, 8) + "/" + uid);
    }

}
