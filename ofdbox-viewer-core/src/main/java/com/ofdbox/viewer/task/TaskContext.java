package com.ofdbox.viewer.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ofdbox.viewer.task.State;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskContext {

    /*
     * 任务类型 文件/URL
     * */
    private Type type;
    private String url;

    @Min(5)
    @Max(15)
    private int dpi = 8;
    /*
     * 是否绘制元素边框
     * */
    @NotNull
    private boolean drawBoundary;
    /*
     * 任务唯一标识
     * */
    private String uid;

    private State state = State.WAITING;
    private long createTime;
    private String filename;
    private int currentPage;
    private List<Integer> pages = new ArrayList<>();
    private String stackTrace;

}
