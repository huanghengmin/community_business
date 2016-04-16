package com.hzih.community.web.action.tenant;

import com.hzih.community.dao.CommunityDoorplateDao;
import com.hzih.community.dao.CommunityRoomDao;
import com.hzih.community.dao.TenantDao;
import com.hzih.community.dao.TenantLogDao;
import com.hzih.community.domain.*;
import com.hzih.community.service.LogService;
import com.hzih.community.utils.FileUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.Hibernate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.util.Date;

/**
 * Created by Administrator on 16-2-26.
 */
public class TenantAction extends ActionSupport {
    private static Logger logger = Logger.getLogger(TenantAction.class);
    private TenantDao tenantDao;
    private TenantLogDao tenantLogDao;
    private Tenant tenant;
    private int start;
    private int limit;
    private LogService logService;
    private CommunityRoomDao communityRoomDao;
    private CommunityDoorplateDao communityDoorplateDao;

    public TenantLogDao getTenantLogDao() {
        return tenantLogDao;
    }

    public void setTenantLogDao(TenantLogDao tenantLogDao) {
        this.tenantLogDao = tenantLogDao;
    }

    public CommunityRoomDao getCommunityRoomDao() {
        return communityRoomDao;
    }

    public void setCommunityRoomDao(CommunityRoomDao communityRoomDao) {
        this.communityRoomDao = communityRoomDao;
    }

    public TenantDao getTenantDao() {
        return tenantDao;
    }

    public void setTenantDao(TenantDao tenantDao) {
        this.tenantDao = tenantDao;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
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

    public String loadTenantHead() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setCharacterEncoding("utf-8");
        String msg = null;
        String id = request.getParameter("id");
        InputStream in = null;
        Tenant lardLord = tenantDao.findById(Long.parseLong(id));
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
            CommunityRoom communityRoom = communityRoomDao.findById(Long.parseLong(sss[sss.length - 1]));
            if(communityRoom!=null){
                if (tenant.getOcr() == 1) {
                    String idCard_before = request.getParameter("idCard_before");
                    byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(idCard_before.getBytes());
                    Blob photo = Hibernate.createBlob(b);
                    tenant.setCardPicBefore(photo);
                    tenant.setCommunityRoom(communityRoom);
                    tenant.setInitDate(new Date());
                    tenantDao.create(tenant);
                    msg = "新增租客信息成功!";
                    json = "{success:true,msg:'" + msg + "'}";
                } else {
                    Tenant l = tenantDao.findByIdCard(communityRoom.getCommunityDoorplate().getId(), tenant.getIdCard());
                    if (l != null) {
                        l.setLastDate(new Date());
                        tenantDao.update(l);
                        msg = "租客信息在此门牌下已存在！";
                        json = "{success:false,msg:'" + msg + "'}";
                    } else {
                        String bytes = request.getParameter("bytes");
                        byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(bytes.getBytes());
                        Blob photo = Hibernate.createBlob(b);
                        tenant.setBytes(photo);
                        tenant.setCommunityRoom(communityRoom);
                        tenant.setInitDate(new Date());
                        tenantDao.create(tenant);
                        msg = "新增租客信息成功!";
                        json = "{success:true,msg:'" + msg + "'}";
                    }
                }
            }else {
                CommunityDoorplate communityDoorplate = communityDoorplateDao.findById(Long.parseLong(sss[sss.length - 1]));
                if (communityDoorplate != null) {
                    String room = request.getParameter("room");
                    CommunityRoom communityR = communityRoomDao.findByRoom(communityDoorplate.getId(), room);
                    if(communityR==null){
                        CommunityRoom communityRoom1 = new CommunityRoom();
                        communityRoom1.setRoom(Integer.parseInt(room));
                        communityRoom1.setCommunityDoorplate(communityDoorplate);
                        communityRoomDao.create(communityRoom1);
                        communityR = communityRoomDao.findByRoom(communityDoorplate.getId(), room);
                    }
                    if (tenant.getOcr() == 1) {
                        String idCard_before = request.getParameter("idCard_before");
                        byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(idCard_before.getBytes());
                        Blob photo = Hibernate.createBlob(b);
                        tenant.setCardPicBefore(photo);
                        tenant.setCommunityRoom(communityR);
                        tenant.setInitDate(new Date());
                        tenantDao.create(tenant);
                        msg = "新增租客信息成功!";
                        json = "{success:true,msg:'" + msg + "'}";
                    } else {
                        Tenant l = tenantDao.findByIdCard(communityDoorplate.getId(), tenant.getIdCard());
                        if (l != null) {
                            l.setLastDate(new Date());
                            tenantDao.update(l);
                            msg = "租客信息在此门牌下已存在！";
                            json = "{success:false,msg:'" + msg + "'}";
                        } else {
                            String bytes = request.getParameter("bytes");
                            byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(bytes.getBytes());
                            Blob photo = Hibernate.createBlob(b);
                            tenant.setBytes(photo);
                            tenant.setCommunityRoom(communityR);
                            tenant.setInitDate(new Date());
                            tenantDao.create(tenant);
                            msg = "新增租客信息成功!";
                            json = "{success:true,msg:'" + msg + "'}";
                        }
                    }
                }
            }
        }catch (Exception e){
            msg = "新增租客信息失败!";
            json = "{success:false,msg:'" + msg + "'}";
        }
        writer.write(json);
        writer.flush();
        writer.close();
        return null;
    }

    public String exitByClient() throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        String id = request.getParameter("id");
        String msg = null;
        String json = null;
        try {
            Tenant tenant = tenantDao.findById(Long.parseLong(id));
            if(tenant!=null){
                TenantLog tenantLog =  new TenantLog();
                tenantLog.setId(tenant.getId());
                tenantLog.setName(tenant.getName());
                tenantLog.setSex(tenant.getSex());
                tenantLog.setIdCard(tenant.getIdCard());
                tenantLog.setMz(tenant.getMz());
                tenantLog.setBirth(tenant.getBirth());
                tenantLog.setSign(tenant.getSign());
                tenantLog.setAddress(tenant.getAddress());
                tenantLog.setDN(tenant.getDN());
                tenantLog.setValidity(tenant.getValidity());
                tenantLog.setPhone(tenant.getPhone());
                tenantLog.setDescription(tenant.getDescription());
                tenantLog.setBytes(tenant.getBytes());
                tenantLog.setCardPicBefore(tenant.getCardPicBefore());
                tenantLog.setCardPicAfter(tenant.getCardPicAfter());
                tenantLog.setStatus(tenant.getStatus());
                tenantLog.setAttention(tenant.getAttention());
                tenantLog.setOcr(tenant.getOcr());
                tenantLog.setInitDate(tenant.getInitDate());
                tenantLog.setLastDate(tenant.getLastDate());
                tenantLog.setExitDate(new Date());
                tenantLog.setCommunityRoom(tenant.getCommunityRoom());
                tenantLogDao.create(tenantLog);
                tenantDao.remove(tenant);
                msg = "退租成功!";
                json = "{success:true,msg:'" + msg + "'}";
            }

        }catch (Exception e){
            msg = "退租失败!";
            json = "{success:false,msg:'" + msg + "'}";
        }
        writer.write(json);
        writer.flush();
        writer.close();
        return null;
    }

    public CommunityDoorplateDao getCommunityDoorplateDao() {
        return communityDoorplateDao;
    }

    public void setCommunityDoorplateDao(CommunityDoorplateDao communityDoorplateDao) {
        this.communityDoorplateDao = communityDoorplateDao;
    }
}
