package com.alipay.sofa.registry.lifecycle.impl;

import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.Lifecycle;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public class LifecycleHelper {

    public static void initializeIfPossible(Object obj) throws InitializeException{

        if(obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canInitialize()){
            ((Lifecycle) obj).initialize();
        }

    }


    public static void startIfPossible(Object obj) throws StartException {

        if(obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canStart()){
            ((Lifecycle) obj).start();
        }

    }

    public static void stopIfPossible(Object obj) throws StopException {

        if(obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canStop()){
            ((Lifecycle) obj).stop();
        }
    }

    public static void disposeIfPossible(Object obj) throws DisposeException {
        if(obj instanceof Lifecycle && ((Lifecycle) obj).getLifecycleState().canDispose()){
            ((Lifecycle) obj).dispose();
        }
    }
}
