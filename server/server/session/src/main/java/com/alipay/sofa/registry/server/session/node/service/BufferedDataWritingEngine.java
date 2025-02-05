/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.BatchRequest;
import com.alipay.sofa.registry.common.model.dataserver.DataServerReq;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;

/** DataWritingEngine implementation that can cache write requests. */
public class BufferedDataWritingEngine implements DataWritingEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(BufferedDataWritingEngine.class);

  private final String sessionServerDataCenter;
  private final int avgSingleQueueBufferSize;
  private final int halfMaximumBufferSize;
  private final SlotTableCache slotTableCache;
  private final NodeExchanger dataNodeExchanger;
  private final SpinyWorker[] workers;

  private final int dataNodeRetryTimes;
  private final int dataNodeRetryQueueSize;
  private final int dataNodeRetryBackoffMillis;
  private final int dataNodePort;

  /**
   * Create an {@link DataWritingEngine} with cache capability. The maximum number of cached
   * requests is twice that of {@param halfMaximumBufferSize}.
   *
   * @param threads the total threads to execute request in the engine
   * @param halfMaximumBufferSize the half of the maximum cached requests
   * @param maxBatchSize the max size of requests batch
   */
  public BufferedDataWritingEngine(
      String sessionServerDataCenter,
      int threads,
      int halfMaximumBufferSize,
      int maxBatchSize,
      SessionServerConfig sessionServerConfig,
      SlotTableCache slotTableCache,
      NodeExchanger dataNodeExchanger) {
    ParaCheckUtil.checkIsPositive(threads, "threads");
    ParaCheckUtil.checkIsPositive(halfMaximumBufferSize, "halfMaximumBufferSize");

    this.sessionServerDataCenter = sessionServerDataCenter;
    this.workers = new SpinyWorker[threads];
    for (int i = 0; i < threads; i++) {
      this.workers[i] =
          new SpinyWorker("req-data-worker-" + i, halfMaximumBufferSize, maxBatchSize);
    }
    this.avgSingleQueueBufferSize = halfMaximumBufferSize / threads;
    this.halfMaximumBufferSize = halfMaximumBufferSize;
    this.slotTableCache = slotTableCache;
    this.dataNodeExchanger = dataNodeExchanger;
    this.dataNodeRetryTimes = sessionServerConfig.getDataNodeRetryTimes();
    this.dataNodeRetryQueueSize = sessionServerConfig.getDataNodeRetryQueueSize();
    this.dataNodeRetryBackoffMillis = sessionServerConfig.getDataNodeRetryBackoffMillis();
    this.dataNodePort = sessionServerConfig.getDataServerPort();
  }

  @Override
  public void submit(DataServerReq dataServerReq) {
    final int slotId = slotTableCache.slotOf(dataServerReq.getDataInfoId());
    int idx = slotId % workers.length;
    SpinyWorker worker = this.workers[idx];

    int singleWorkerCachedRequest;
    int totalCachedRequests = -1;
    boolean failed = true;
    /*
       For example: avgSingleQueueBufferSize = 4 & halfMaximumBufferSize = 16 (that means we have 4 worker)
       we can successfully add 16 elements to one worker, and at most add 4 elements to other 3 workers,
       so in the end we totally cached 16 + 12 < 32 (two times of halfMaximumBufferSize)
    */
    if ((singleWorkerCachedRequest = worker.cachedRequests()) <= avgSingleQueueBufferSize
        || (totalCachedRequests = totalCachedRequests()) <= halfMaximumBufferSize) {
      failed = !worker.offer(new Req(slotId, dataServerReq));
    }
    if (failed) {
      throw new FastRejectedExecutionException(
          String.format(
              "BlockingQueues.put overflow, idx=%d, totalCachedRequests=%d, singleWorkerCachedRequests=%d, halfMaximumBufferSize=%d, avgSingleQueueBufferSize=%d",
              idx,
              totalCachedRequests,
              singleWorkerCachedRequest,
              halfMaximumBufferSize,
              avgSingleQueueBufferSize));
    }
  }

  private int totalCachedRequests() {
    int count = 0;
    for (SpinyWorker worker : workers) {
      count += worker.cachedRequests();
    }
    return count;
  }

  private class SpinyWorker {

    private final BlockingQueue<Req> blockingQueue;
    private final LinkedList<RetryBatch> retryBatches;

    public SpinyWorker(String workerName, int bufferSize, int maxBatchSize) {
      this.blockingQueue = new LinkedBlockingQueue<>(bufferSize);
      this.retryBatches = Lists.newLinkedList();

      ConcurrentUtils.createDaemonThread(
              workerName,
              () -> {
                while (true) {
                  try {
                    Req firstReq = blockingQueue.poll(200, TimeUnit.MILLISECONDS);
                    if (firstReq != null) {
                      Map<Integer, LinkedList<Object>> reqs = drainReq(blockingQueue, maxBatchSize);
                      // send by order, firstReq.slotId is the first one
                      LinkedList<Object> firstBatch = reqs.remove(firstReq.slotId);
                      if (firstBatch == null) {
                        firstBatch = Lists.newLinkedList();
                      }
                      firstBatch.addFirst(firstReq.req);
                      request(firstReq.slotId, firstBatch);
                      for (Map.Entry<Integer, LinkedList<Object>> batch : reqs.entrySet()) {
                        request(batch.getKey(), batch.getValue());
                      }
                    }

                    if (!retryBatches.isEmpty()) {
                      final Iterator<RetryBatch> it = retryBatches.iterator();
                      List<RetryBatch> retries = Lists.newArrayList();
                      while (it.hasNext()) {
                        RetryBatch batch = it.next();
                        it.remove();
                        if (!request(batch.batch)) {
                          retries.add(batch);
                        }
                      }
                      for (RetryBatch retry : retries) {
                        retry(retry);
                      }
                    }
                  } catch (Throwable cause) {
                    LOGGER.safeError("failed to request batch", cause);
                  }
                }
              })
          .start();
    }

    public boolean offer(Req req) {
      return this.blockingQueue.offer(req);
    }

    public int cachedRequests() {
      return this.blockingQueue.size();
    }

    private Map<Integer, LinkedList<Object>> drainReq(BlockingQueue<Req> queue, int max) {
      List<Req> reqs = new ArrayList<>(max);
      queue.drainTo(reqs, max);
      if (reqs.isEmpty()) {
        return Collections.emptyMap();
      }
      Map<Integer, LinkedList<Object>> ret = Maps.newLinkedHashMap();
      for (Req req : reqs) {
        LinkedList<Object> objects = ret.computeIfAbsent(req.slotId, k -> Lists.newLinkedList());
        objects.add(req.req);
      }
      return ret;
    }

    private void request(int slotId, List<Object> reqs) {
      final BatchRequest batch = new BatchRequest(ServerEnv.PROCESS_ID, slotId, reqs);
      if (!request(batch)) {
        retry(new RetryBatch(batch));
      }
    }

    private boolean request(BatchRequest batch) {
      try {
        final Slot slot = slotTableCache.getSlot(sessionServerDataCenter, batch.getSlotId());
        if (slot == null) {
          throw new RequestException(
              StringFormatter.format("slot not found, slotId={}", batch.getSlotId()));
        }
        batch.setSlotTableEpoch(slotTableCache.getEpoch(sessionServerDataCenter));
        batch.setSlotLeaderEpoch(slot.getLeaderEpoch());
        sendRequest(
            new Request() {
              @Override
              public Object getRequestBody() {
                return batch;
              }

              @Override
              public URL getRequestUrl() {
                final String dataIp = slot.getLeader();
                if (StringUtils.isBlank(dataIp)) {
                  throw new RequestException(String.format("slot has no leader, slotId=%s", slot));
                }
                return new URL(dataIp, dataNodePort);
              }
            });
        return true;
      } catch (Throwable e) {
        LOGGER.error("failed to request batch, {}", batch, e);
        return false;
      }
    }

    private CommonResponse sendRequest(Request request) throws RequestException {
      Response response = dataNodeExchanger.request(request);
      Object result = response.getResult();
      SlotAccessGenericResponse resp = (SlotAccessGenericResponse) result;
      if (!resp.isSuccess()) {
        throw new RuntimeException(
            String.format(
                "response failed, target: %s, request: %s, message: %s",
                request.getRequestUrl(), request.getRequestBody(), resp.getMessage()));
      }
      return resp;
    }

    private void retry(RetryBatch retry) {
      retry.retryCount++;
      if (retry.retryCount <= dataNodeRetryTimes) {
        if (retryBatches.size() >= dataNodeRetryQueueSize) {
          // remove the oldest
          retryBatches.removeFirst();
        }
        retry.expireTimestamp = System.currentTimeMillis() + dataNodeRetryBackoffMillis;
        retryBatches.add(retry);
      } else {
        LOGGER.warn(
            "RetryBatch(slotId={}, sessionProcessId={}) reach the dataNodeRetryTimes({})",
            retry.batch.getSlotId(),
            retry.batch.getSessionProcessId(),
            dataNodeRetryTimes);
      }
    }
  }

  private static final class Req {
    final int slotId;
    final DataServerReq req;

    Req(int slotId, DataServerReq req) {
      this.slotId = slotId;
      this.req = req;
    }
  }

  private static final class RetryBatch {
    final BatchRequest batch;
    long expireTimestamp;
    int retryCount;

    RetryBatch(BatchRequest batch) {
      this.batch = batch;
    }

    @Override
    public String toString() {
      return "RetryBatch{"
          + "batch="
          + batch
          + ", retry="
          + retryCount
          + ", expire="
          + expireTimestamp
          + '}';
    }
  }
}
