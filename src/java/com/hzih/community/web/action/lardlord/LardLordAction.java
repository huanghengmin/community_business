package com.hzih.community.web.action.lardlord;

import com.hzih.community.dao.CommunityDoorplateDao;
import com.hzih.community.dao.LardLordDao;
import com.hzih.community.domain.*;
import com.hzih.community.service.LogService;
import com.hzih.community.utils.FileUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Hibernate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Blob;
import java.util.*;

/**
 * Created by Administrator on 16-2-26.
 */
public class LardLordAction extends ActionSupport {
    private static Logger logger = Logger.getLogger(LardLordAction.class);
    private LardLordDao lardLordDao;
    private LardLord lardLord;
    private int start;
    private int limit;
    private LogService logService;
    private CommunityDoorplateDao communityDoorplateDao;


    public LardLordDao getLardLordDao() {
        return lardLordDao;
    }

    public void setLardLordDao(LardLordDao lardLordDao) {
        this.lardLordDao = lardLordDao;
    }

    public LardLord getLardLord() {
        return lardLord;
    }

    public void setLardLord(LardLord lardLord) {
        this.lardLord = lardLord;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public LogService getLogService() {
        return logService;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public CommunityDoorplateDao getCommunityDoorplateDao() {
        return communityDoorplateDao;
    }

    public void setCommunityDoorplateDao(CommunityDoorplateDao communityDoorplateDao) {
        this.communityDoorplateDao = communityDoorplateDao;
    }

    public String loadLardHead() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        String msg = null;
        String id = request.getParameter("id");
        InputStream in = null;
        LardLord lardLord = lardLordDao.findById(Long.parseLong(id));
        if (lardLord.getBytes() != null) {
            in = lardLord.getBytes().getBinaryStream();
        } else {
            String str = request.getServletContext().getRealPath("js") + "/ext/resources/images/default/s.gif";
            in = new FileInputStream(str);
        }
        FileUtil.copy(in, response);

        return null;
    }

    public String insertByClient() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        String msg = null;
        String json = null;
        try {
            String qrCode = request.getParameter("qrCode");
            String sss[] = qrCode.split("-");
            CommunityDoorplate communityDoorplate = communityDoorplateDao.findById(Long.parseLong(sss[sss.length - 1]));
            if (communityDoorplate != null) {
                if (lardLord.getOcr() == 1) {
                    String idCard_before = request.getParameter("idCard_before");
                    byte[] b = Base64.decodeBase64(idCard_before.getBytes());
                    Blob photo = Hibernate.createBlob(b);
                    lardLord.setCardPicBefore(photo);
                    lardLord.setStatus(1);
                    lardLord.setCommunityDoorplate(communityDoorplate);
                    lardLord.setInitDate(new Date());
                    lardLordDao.clearLardLord();
                    lardLordDao.create(lardLord);
                    msg = "新增房东信息成功!";
                    json = "{success:true,msg:'" + msg + "'}";
                } else {
                    LardLord l = lardLordDao.findByIdCard(communityDoorplate.getId(), lardLord.getIdCard());
                    if (l != null) {
                        l.setLastDate(new Date());
                        l.setStatus(1);
                        lardLordDao.clearLardLord();
                        lardLordDao.update(l);
                        msg = "房东信息已存在此门牌，无需再次添加！";
                        json = "{success:false,msg:'" + msg + "'}";
                    } else {
                        String bytes = request.getParameter("bytes");
                        byte[] b = Base64.decodeBase64(bytes.getBytes());
                        Blob photo = Hibernate.createBlob(b);
                        lardLord.setBytes(photo);
                        lardLord.setStatus(1);
                        lardLord.setCommunityDoorplate(communityDoorplate);
                        lardLord.setInitDate(new Date());
                        lardLordDao.clearLardLord();
                        lardLordDao.create(lardLord);
                        msg = "新增房东信息成功!";
                        json = "{success:true,msg:'" + msg + "'}";
                    }
                }
            }
        }catch (Exception e){
            msg = "新增房东信息失败!";
            json = "{success:false,msg:'" + msg + "'}";
        }
        writer.write(json);
        writer.flush();
        writer.close();
        return null;
    }

}
