package com.ofdbox.viewer.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ofdbox.viewer.task.constant.ImageType;
import com.ofdbox.viewer.task.constant.State;
import com.ofdbox.viewer.task.constant.Type;
import com.ofdbox.viewer.utils.Utils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ofdrw.converter.ImageMaker;
import org.ofdrw.converter.SVGMaker;
import org.ofdrw.reader.OFDReader;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.ofdbox.viewer.task.constant.TaskConstant.TASK_INFO_FILE_NAME;

@Data
public class Task implements Runnable {
    /**
     * 用户选项
     */
    private UserOptions userOptions;

    /**
     * 任务ID
     */
    private String uid;

    /**
     * 任务状态
     */
    private State state = State.WAITING;

    /**
     * 真实文件名
     */
    private String filename;

    /**
     * 已渲染完成页数
     */
    private int completedPage;

    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 错误信息
     */
    private String stackTrace;

    /**
     * 工作目录
     */
    @JSONField(serialize = false)
    @JsonIgnore
    private transient File workdir;

    /**
     * ofd文件
     */
    @JSONField(serialize = false)
    @JsonIgnore
    private transient File ofdFile;

    private Task(File storePath) throws IOException {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.uid = date + Utils.uuid();
        this.workdir = new File(storePath, date + "/" + uid);
        getImageDir().mkdirs();
        FileUtils.forceMkdir(this.workdir);
    }

    private Task() {
    }

    public static Task fromFile(UserOptions userOptions, MultipartFile file, File storePath) throws IOException {
        String filename = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".ofd";

        Task task = new Task(storePath);
        task.userOptions = userOptions;
        task.filename = filename;
        task.ofdFile = new File(task.getWorkdir(), filename);

        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(task.ofdFile)) {
            IOUtils.copy(in, out);
        }

        task.writeInfo();

        return task;
    }

    public static Task fromUrl(UserOptions userOptions, File storePath) throws IOException {
        Task task = new Task(storePath);
        task.userOptions = userOptions;
        return task;
    }


    @Override
    public void run() {
        try {
            if (userOptions.getType() == Type.URL) {
                this.filename = "download.ofd";
                ofdFile = new File(workdir, filename);
                IOUtils.copy(new URL(userOptions.getUrl()) ,ofdFile);
            }
            setState(State.PARSING);
            Path src = Paths.get(ofdFile.getCanonicalPath());
            try (OFDReader reader = new OFDReader(src)) {
                setState(State.RENDERING);
                if (userOptions.getImageType() == ImageType.SVG) {
                    renderSvg(reader);
                } else {
                    renderPng(reader);
                }
            }
            setState(State.COMPLETED);
        } catch (Exception e) {
            e.printStackTrace();
            handleError(e);
        } finally {
            writeInfo();
        }
    }


    private void renderPng(OFDReader reader) throws IOException {
        ImageMaker imageMaker = new ImageMaker(reader, userOptions.getPpm());
        imageMaker.config.setDrawBoundary(userOptions.isDrawBoundary());
        this.totalPage = imageMaker.pageSize();
        File imageDie = getImageDir();
        for (int i = 0; i < imageMaker.pageSize(); i++) {
            // 4. 指定页码转换图片
            BufferedImage image = imageMaker.makePage(i);
            int page = i + 1;
            File imageFile = new File(imageDie, page + ".png");
            ImageIO.write(image, "PNG", imageFile);
            this.completedPage = page;
        }
    }

    private void renderSvg(OFDReader reader) throws IOException {
        SVGMaker svgMaker = new SVGMaker(reader, this.userOptions.getPpm());
        svgMaker.config.setDrawBoundary(userOptions.isDrawBoundary());
        this.totalPage = svgMaker.pageSize();
        File imageDie = getImageDir();

        for (int i = 0; i < svgMaker.pageSize(); i++) {
            String svg = svgMaker.makePage(i);
            int page = i + 1;
            File imageFile = new File(imageDie, page + ".svg");
            FileUtils.write(imageFile, svg, "UTF-8");
            this.completedPage = page;
        }
    }

    private File getImageDir() {
        return new File(workdir, "images");
    }

    private void handleError(Throwable throwable) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        String stackTraceStr = result.toString();

        setStackTrace(stackTraceStr);
        setState(State.ERROR);
    }

    private void writeInfo() {
        String json = JSON.toJSONString(this);
        File jsonFile = new File(workdir, TASK_INFO_FILE_NAME);
        writeString(jsonFile, json);
    }

    private void writeString(File file, String str) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.print(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static File getWorkdirFromUid(String storePath, String uid) {
        return new File(storePath, uid.substring(0, 8) + "/" + uid);
    }
}
