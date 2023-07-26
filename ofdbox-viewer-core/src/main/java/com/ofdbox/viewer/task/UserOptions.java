package com.ofdbox.viewer.task;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserInput {
    /**
     * 任务类型 文件/URL
     */
    private Type type;

    /**
     * 图像类型
     */
    private ImageType imageType = ImageType.SVG;

    /**
     * 下载链接
     */
    private String url;


    /**
     * 每毫米像素值
     */
    @Min(5)
    @Max(15)
    private int ppm = 8;

    /**
     * 是否绘制元素边框（用于测试）
     */
    @NotNull
    private boolean drawBoundary;
}
