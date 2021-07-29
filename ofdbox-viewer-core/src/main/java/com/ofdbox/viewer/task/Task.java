package com.ofdbox.viewer.task;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.ofdrw.converter.ImageMaker;
import org.ofdrw.reader.OFDReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class Task {
    private TaskContext context;
    private File workdir;

    public Task(TaskContext context, File workdir) {
        this.context = context;
        this.workdir = workdir;
    }

    public void run() {
        try {
            context.setState(State.PARSING);
            File file = new File(workdir, context.getFilename());

            Path src = Paths.get(file.getAbsolutePath());

            try (OFDReader reader = new OFDReader(src);) {

                ImageMaker imageMaker = new ImageMaker(reader, context.getDpi());

                File imageDie = new File(workdir, "images");
                imageDie.mkdirs();
                context.setState(State.RENDERING);

                for (int i = 0; i < imageMaker.pageSize(); i++) {
                    // 4. 指定页码转换图片
                    BufferedImage image = imageMaker.makePage(i);

                    File imageFile = new File(imageDie, i + ".png");
                    ImageIO.write(image, "PNG", imageFile);

                    context.setCurrentPage(i);
                    context.getPages().add(i);

                    context.setState(State.COMPLETED);
                }

            }
        } catch (Exception e) {

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String stackTraceStr = result.toString();

            context.setStackTrace(stackTraceStr);
            context.setState(State.ERROR);
        } finally {
            String json = JSON.toJSONString(context);
            File jsonFile = new File(workdir, "context.json");
            writeString(jsonFile, json);
            File allover = new File(workdir, "allover");
            writeString(allover, "");
        }
    }

    private void writeString(File file, String str) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.print(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
