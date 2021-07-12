package com.alipay.sofa.registry.jraft.exception;

/**
 * @author : xingpeng
 * @date : 2021-07-05 11:45
 **/
public class RheaKVDriverException extends RuntimeException{

    public RheaKVDriverException(String msg){
        super(String.format("RheaKVDriver Bean: %s.",msg));
    }

}
