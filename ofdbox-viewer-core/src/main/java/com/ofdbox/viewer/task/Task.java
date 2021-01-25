package com.ofdbox.viewer.task;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ofdbox.convertor.img.Ofd2Img;
import com.ofdbox.core.*;
import com.ofdbox.core.model.OFD;
import com.ofdbox.core.model.page.Page;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class Task {
    private TaskContext context;
    private File workdir;

    private static OFDReader ofdReader = null;
    private static Ofd2Img ofd2Img = null;


    public Task(TaskContext context, File workdir) {
        this.context = context;
        this.workdir = workdir;

        ofdReader = new OFDReader();
        ofdReader.getConfig().setValid(false);
        ofd2Img = new Ofd2Img();
        if (context.isDrawBoundary()) {
            ofd2Img.getConfig().setDrawBoundary(context.isDrawBoundary());
        }
    }

    public void run() {
        try {
            context.setState(State.PARSING);
            File file = new File(workdir, context.getFilename());
            OFD ofd = ofdReader.read(file);
            File imageDie = new File(workdir, "images");
            imageDie.mkdirs();
            context.setState(State.RENDERING);
            int currentPage = 0;
            for (Page page : ofd.getDocuments().get(0).getPages()) {
                currentPage++;
                File imageFile = new File(imageDie, currentPage + ".png");
                BufferedImage image = ofd2Img.toImage(page, context.getDpi());
                ImageIO.write(image, "PNG", imageFile);
                context.setCurrentPage(currentPage);
                context.getPages().add(currentPage);
            }
            context.setState(State.COMPLETED);
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
