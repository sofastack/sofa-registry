package com.alipay.sofa.registry.jraft.domain;

import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.util.MessageDigests;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

/**
 * @author : xingpeng
 * @date : 2021-07-15 08:35
 **/
public class InterfaceAppsDomain implements Serializable {
    /** local data center */
    private String dataCenter;

    /** interfaceName */
    private String interfaceName;

    /** appName */
    private String appName;

    /** reference */
    private boolean reference;

    /** hashcode */
    private String hashcode;

    /** last update time */
    private Timestamp gmtModify;

    /** interfaceMapping */
    private long nanosVersion;

    private Set<String> apps;

    public InterfaceAppsDomain() {}

    public InterfaceAppsDomain(String dataCenter, String interfaceName, String appName) {
        this.dataCenter = dataCenter;
        this.interfaceName = interfaceName;
        this.appName = appName;
        this.hashcode = MessageDigests.getMd5String(interfaceName);
    }

    public InterfaceAppsDomain(String dataCenter, String interfaceName, String appName, Timestamp gmtModify) {
        this.dataCenter = dataCenter;
        this.interfaceName = interfaceName;
        this.appName = appName;
        this.gmtModify = gmtModify;
        this.hashcode = MessageDigests.getMd5String(interfaceName);
    }

    public InterfaceAppsDomain(String dataCenter, String interfaceName, String appName, long nanosVersion , Set<String> apps) {
        this.dataCenter = dataCenter;
        this.interfaceName = interfaceName;
        this.appName = appName;
        this.hashcode = MessageDigests.getMd5String(interfaceName);
        this.nanosVersion=nanosVersion;
        this.apps=apps;
    }

    public InterfaceAppsDomain(String dataCenter, String interfaceName, String appName, boolean reference, String hashcode, Timestamp gmtModify, long nanosVersion, Set<String> apps) {
        this.dataCenter = dataCenter;
        this.interfaceName = interfaceName;
        this.appName = appName;
        this.reference = reference;
        this.hashcode = hashcode;
        this.gmtModify = gmtModify;
        this.nanosVersion = nanosVersion;
        this.apps = apps;
    }

    public InterfaceAppsDomain(String dataCenter, String interfaceName, long nanosVersion, Set<String> apps) {
        this.dataCenter = dataCenter;
        this.interfaceName = interfaceName;
        this.nanosVersion = nanosVersion;
        this.apps = apps;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Setter method for property <tt>dataCenter</tt>.
     *
     * @param dataCenter value to be assigned to property dataCenter
     */
    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    /**
     * Getter method for property <tt>interfaceName</tt>.
     *
     * @return property value of interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Setter method for property <tt>interfaceName</tt>.
     *
     * @param interfaceName value to be assigned to property interfaceName
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Getter method for property <tt>appName</tt>.
     *
     * @return property value of appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setter method for property <tt>appName</tt>.
     *
     * @param appName value to be assigned to property appName
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isReference() {
        return reference;
    }

    /**
     * Setter method for property <tt>reference</tt>.
     *
     * @param reference value to be assigned to property reference
     */
    public void setReference(boolean reference) {
        this.reference = reference;
    }

    /**
     * Getter method for property <tt>gmtModify</tt>.
     *
     * @return property value of gmtModify
     */
    public Timestamp getGmtModify() {
        return gmtModify;
    }

    /**
     * Setter method for property <tt>gmtModify</tt>.
     *
     * @param gmtModify value to be assigned to property gmtModify
     */
    public void setGmtModify(Timestamp gmtModify) {
        this.gmtModify = gmtModify;
    }

    /**
     * Getter method for property <tt>hashcode</tt>.
     *
     * @return property value of hashcode
     */
    public String getHashcode() {
        return hashcode;
    }

    /**
     * Setter method for property <tt>hashcode</tt>.
     *
     * @param hashcode value to be assigned to property hashcode
     */
    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public long getNanosVersion() {
        return nanosVersion;
    }

    public void setNanosVersion(long nanosVersion) {
        this.nanosVersion = nanosVersion;
    }

    public Set<String> getApps() {
        return apps;
    }

    public void setApps(Set<String> apps) {
        this.apps = apps;
    }

    @Override
    public String toString() {
        return "InterfaceAppsDomain{" +
                "dataCenter='" + dataCenter + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", appName='" + appName + '\'' +
                ", reference=" + reference +
                ", hashcode='" + hashcode + '\'' +
                ", gmtModify=" + gmtModify +
                ", nanosVersion=" + nanosVersion +
                ", apps=" + apps +
                '}';
    }
}
