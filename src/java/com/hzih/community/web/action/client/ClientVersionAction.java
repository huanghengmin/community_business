package com.hzih.community.web.action.client;

import com.hzih.community.utils.StringContext;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-25
 * Time: 下午12:22
 * To change this template use File | Settings | File Templates.
 */
public class ClientVersionAction extends ActionSupport {
    private Logger logger = Logger.getLogger(ClientVersionAction.class);
    private File uploadFile;
    private static final int BUFFER_SIZE = 1 * 1024;
    private String fileContentType;
    private String uploadFileFileName;

    public String qrcode() throws Exception {
        HttpServletResponse response = ServletActionContext.getResponse();
        String qrcodeDir = StringContext.systemPath + "/client";
        String qrcodepath = qrcodeDir + "/android_no_logo.jpg";
        ServletOutputStream output = null;
        InputStream in = null;
        try {
//            response.reset();
            output = response.getOutputStream();
            in = new FileInputStream(qrcodepath);
            byte tmp[] = new byte[256];
            int i = 0;
            while ((i = in.read(tmp)) != -1) {
                output.write(tmp, 0, i);
            }
            output.flush(); //强制清出缓冲区
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public File getUploadFile() {
        return this.uploadFile;
    }

    public void setUploadFile(File file) {
        this.uploadFile = file;
    }

    public String getUploadFileFileName() {
        return uploadFileFileName;
    }

    public void setUploadFileFileName(String uploadFileFileName) {
        this.uploadFileFileName = uploadFileFileName;
    }

    private static void writeFile(File src, File dst) {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(src), BUFFER_SIZE);
                out = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE);
                byte[] buffer = new byte[BUFFER_SIZE];
                while (in.read(buffer) > 0) {
                    out.write(buffer);
                }
            } finally {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
