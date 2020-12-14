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
package com.alipay.sofa.registry.task.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.sofa.registry.task.TaskClosure;

/**
 *
 * @author shangyu.wh
 * @version $Id: TaskEvent.java, v 0.1 2017-12-07 18:12 shangyu.wh Exp $
 */
public class TaskEvent {

    public enum TaskType {
        //Session task
        SUBSCRIBER_REGISTER_FETCH_TASK("SubscriberRegisterFetchTask"), //
        SUBSCRIBER_PUSH_EMPTY_TASK("SubscriberPushEmptyTask"), //
        WATCHER_REGISTER_FETCH_TASK("WatcherRegisterFetchTask"), //
        DATA_CHANGE_FETCH_TASK("DataChangeFetchTask"), //
        DATA_PUSH_TASK("DataPushTask"), //
        DATA_CHANGE_FETCH_CLOUD_TASK("DataChangeFetchCloudTask"), //
        RECEIVED_DATA_MULTI_PUSH_TASK("ReceivedDataMultiPushTask"), //
        RECEIVED_DATA_CONFIG_PUSH_TASK("ReceivedDataConfigPushTask"), //
        CANCEL_DATA_TASK("CancelDataTask"), //
        SYNC_PUBLISHER_TASK("SyncPublisherTask"), //
        SYNC_UNPUBLISHER_TASK("SyncUnPublisherTask"), //
        SYNC_SUBSCRIBER_TASK("SyncSubscriberTask"), //
        SYNC_UNSUBSCRIBER_TASK("SyncUnSubscriberTask"), //
        PROVIDE_DATA_CHANGE_FETCH_TASK("ProvideDataChangeFetchTask"), //
        SUBSCRIBER_MULTI_FETCH_TASK("SubscriberMultiFetchTask"), //
        PUBLISH_DATA_TASK("PublishDataTask"), //
        UN_PUBLISH_DATA_TASK("UnPublishDataTask"), //

        //Session Adapter task
        USER_DATA_ELEMENT_PUSH_TASK("UserDataElementPushTask"), //
        USER_DATA_ELEMENT_MULTI_PUSH_TASK("UserDataElementMultiPushTask"), //

        //MetaServer task
        PERSISTENCE_DATA_CHANGE_NOTIFY_TASK("PersistenceDataChangeNotifyTask"), //
        ;

        private String        name;

        private AtomicInteger nextId = new AtomicInteger(0);

        TaskType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Object                    eventObj;

    private TaskType                  taskType;

    private long                      sendTimeStamp;

    private long                      createTime;

    private TaskClosure               taskClosure;

    private final String              taskId;

    private final Map<String, Object> attributes = new ConcurrentHashMap();

    /**
     * constructor
     * @param taskType
     */
    public TaskEvent(TaskType taskType) {
        this.taskType = taskType;
        this.createTime = System.currentTimeMillis();
        this.taskId = String.format("%s-%s-%s", taskType.name, this.createTime,
            taskType.nextId.getAndIncrement());
    }

    /**
     * constructor
     * @param eventObj
     * @param taskType
     */
    public TaskEvent(Object eventObj, TaskType taskType) {
        this(taskType);
        this.eventObj = eventObj;
    }

    /**
     * Getter method for property <tt>taskId</tt>.
     *
     * @return property value of taskId
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Getter method for property <tt>eventObj</tt>.
     *
     * @return property value of eventObj
     */
    public Object getEventObj() {
        return eventObj;
    }

    /**
     * Setter method for property <tt>eventObj</tt>.
     *
     * @param eventObj  value to be assigned to property eventObj
     */
    public void setEventObj(Object eventObj) {
        this.eventObj = eventObj;
    }

    /**
     * Getter method for property <tt>taskType</tt>.
     *
     * @return property value of taskType
     */
    public TaskType getTaskType() {
        return taskType;
    }

    /**
     * get attribute by key
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * set attribute
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value) {
        if (value == null) {
            //The null value is not allowed in the ConcurrentHashMap.
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * Getter method for property <tt>taskClosure</tt>.
     *
     * @return property value of taskClosure
     */
    public TaskClosure getTaskClosure() {
        return taskClosure;
    }

    /**
     * Setter method for property <tt>taskClosure</tt>.
     *
     * @param taskClosure  value to be assigned to property taskClosure
     */
    public void setTaskClosure(TaskClosure taskClosure) {
        this.taskClosure = taskClosure;
    }

    /**
     * Getter method for property <tt>sendTimeStamp</tt>.
     *
     * @return property value of sendTimeStamp
     */
    public long getSendTimeStamp() {
        return sendTimeStamp;
    }

    /**
     * Setter method for property <tt>sendTimeStamp</tt>.
     *
     * @param sendTimeStamp  value to be assigned to property sendTimeStamp
     */
    public void setSendTimeStamp(long sendTimeStamp) {
        this.sendTimeStamp = sendTimeStamp;
    }

    /**
     * Getter method for property <tt>createTime</tt>.
     *
     * @return property value of createTime
     */
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TaskEvent{");
        sb.append("eventObj=").append(eventObj);
        sb.append(", sendTimeStamp=").append(sendTimeStamp);
        sb.append(", attributes=").append(attributes);
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}