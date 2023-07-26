package com.ofdbox.viewer.task;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ofdbox.viewer.task.constant.TaskConstant.TASK_INFO_FILE_NAME;

@Component
@Slf4j
public class TaskManage {

    @Value("${task-clear-time}")
    private int taskClearTime;

    @Value("${store-path}")
    private String storePath;


    private Map<String, Task> tasks = new ConcurrentHashMap<>();
    private Map<String, Task> contexts = new ConcurrentHashMap<>();
    private ExecutorService service = Executors.newFixedThreadPool(1);


    public void runTask(Task task) {
        contexts.put(task.getUid(), task);
        service.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                contexts.remove(task.getUid());
            }
        });
    }

    public Task getTask(String uid) {
        Task task = contexts.get(uid);
        if (task != null) {
            return task;
        }
        File file = new File(Task.getWorkdirFromUid(storePath, uid), TASK_INFO_FILE_NAME);
        try {
            String json = FileUtils.
                    readFileToString(file, "UTF-8");
            return JSON.parseObject(json, Task.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
