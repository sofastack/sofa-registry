package com.alipay.sofa.registry.util;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public class ConcurrentUtils {

    public static abstract class SafeParaLoop<T> {

        private static final Logger logger = LoggerFactory.getLogger(SafeParaLoop.class);

        private List<T> list;

        private Executor executors;

        public SafeParaLoop(Collection<T> list) {
            this(MoreExecutors.directExecutor(), list);
        }

        public SafeParaLoop(Executor executors, Collection<T> src) {
            this.executors = executors;
            this.list = src == null ? null : Lists.newLinkedList(src);
        }

        public void run() {
            if(list == null) {
                return;
            }
            for(T t : list) {
                executors.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doRun0(t);
                        } catch (Exception e) {
                            logger.error("[SafeParaLoop][{}]", getInfo(t), e);
                        }
                    }
                });

            }
        }

        protected abstract void doRun0(T t) throws Exception;

        String getInfo(T t) {
            return t.toString();
        }
    }
}
