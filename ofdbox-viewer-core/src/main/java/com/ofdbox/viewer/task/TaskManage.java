package com.ofdbox.viewer.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class TaskManage {

    @Value("${task-clear-time}")
    private int taskClearTime;


    private Map<String, Task> tasks = Collections.synchronizedMap(new HashMap<>());
    private Map<String, TaskContext> contexts = Collections.synchronizedMap(new HashMap<>());
    private ExecutorService service = Executors.newFixedThreadPool(1);


    public void runTask(TaskContext context, File workdir) {
        Task task = new Task(context, workdir);
        contexts.put(context.getUid(), context);
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    contexts.remove(context.getUid());
                }

            }
        });
    }

    public TaskContext queryTask(String uid) {
        return contexts.get(uid);
    }

    public Task getTask(String id) {
        return tasks.get(id);
    }

//    @Scheduled(cron = "0 0 * * * ?")
//    private void clearTasks() {
//        log.info("开始清理任务");
//        List<Map.Entry<String, Task>> copy = new ArrayList<>(tasks.entrySet());
//        for (Map.Entry<String, Task> entry : copy) {
//            if (System.currentTimeMillis() - entry.getValue().getCreateTime() > taskClearTime * 3600 * 1000) {
//                tasks.remove(entry.getKey());
//                log.info("清理任务：" + entry.getKey());
//            }
//        }
//    }
}
