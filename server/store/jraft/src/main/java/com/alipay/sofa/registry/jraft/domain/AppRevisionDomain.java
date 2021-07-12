package com.alipay.sofa.registry.jraft.domain;

import java.util.Date;

public class AppRevisionDomain{
    
    /** local data center */
    private String dataCenter;

    /** revision */
    private String revision;

    /** appName */
    private String appName;

    /** clientVersion */
    private String clientVersion;

    /** base_params */
    private String baseParams;

    /** service_params */
    private String serviceParams;

    /** create time */
    private Date gmtCreate;

    /** last update time */
    private Date gmtModify;

    public String getDataCenter() {
      return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
      this.dataCenter = dataCenter;
    }

    public String getRevision() {
      return revision;
    }

    public void setRevision(String revision) {
      this.revision = revision;
    }

    public String getAppName() {
      return appName;
    }

    public void setAppName(String appName) {
      this.appName = appName;
    }

    public String getClientVersion() {
      return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
      this.clientVersion = clientVersion;
    }

    public String getBaseParams() {
      return baseParams;
    }

    public void setBaseParams(String baseParams) {
      this.baseParams = baseParams;
    }

    public String getServiceParams() {
      return serviceParams;
    }

    public void setServiceParams(String serviceParams) {
      this.serviceParams = serviceParams;
    }

    public Date getGmtCreate() {
      return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
      this.gmtCreate = gmtCreate;
    }

    public Date getGmtModify() {
      return gmtModify;
    }

    public void setGmtModify(Date gmtModify) {
      this.gmtModify = gmtModify;
    }
  }