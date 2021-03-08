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

import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;

/**
 * 
 * @author shangyu.wh
 * @version $Id: TaskListener.java, v 0.1 2017-12-07 18:11 shangyu.wh Exp $
 */
public interface TaskListener {

	/**
	 * com.alipay.sofa.registry.server.meta.listener type check
	 * 
	 * @return type
	 */
	TaskType support();

	/**
	 * event execute
	 * 
	 * @param event
	 */
	void handleEvent(TaskEvent event);
}