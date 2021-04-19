package com.alipay.sofa.registry.server.session;

/**
 * @author chen.zhu
 * <p>
 * Apr 12, 2021
 */

import com.alipay.sofa.registry.server.session.listener.ProvideDataChangeFetchTaskListenerTest;
import com.alipay.sofa.registry.server.session.node.service.MetaServerServiceImplTest;
import com.alipay.sofa.registry.server.session.node.service.SessionMetaServerManagerTest;
import com.alipay.sofa.registry.server.session.slot.SlotTableCacheImplTest;
import com.alipay.sofa.registry.server.session.store.DataCacheTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  WrapperInvocationTest.class,
  DataCacheTest.class,
  SlotTableCacheImplTest.class,
  MetaServerServiceImplTest.class,
  SessionMetaServerManagerTest.class,
  ProvideDataChangeFetchTaskListenerTest.class
})
public class AllTests {
}
