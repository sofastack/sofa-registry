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
package com.alipay.sofa.registry.server.session.metadata;

import io.prometheus.client.Counter;

/**
 * 
 * @author xiaojian.xj
 * @version $Id: MetadataMetrics.java, v 0.1 2021年02月25日 16:34 xiaojian.xj Exp $
 */
public class MetadataMetrics {

	private MetadataMetrics() {

	}

	static final class Fetch {
		static final Counter METADATA_FETCH_COUNTER = Counter.build()
				.namespace("metadata").subsystem("fetch")
				.name("metadata_fetch_total")
				.help("metadata query revision and apps").register();

		static final Counter.Child FETCH_REVISION_COUNTER = METADATA_FETCH_COUNTER
				.labels("revision");
		static final Counter.Child FETCH_APPS_COUNTER = METADATA_FETCH_COUNTER
				.labels("apps");
	}

	static final class Register {

		static final Counter METADATA_REGISTER_COUNTER = Counter.build()
				.namespace("metadata").subsystem("register")
				.name("metadata_register_total")
				.help("metadata revision register and heartbeat").register();

		static final Counter.Child REVISION_REGISTER_COUNTER = METADATA_REGISTER_COUNTER
				.labels("register");

		static final Counter.Child REVISION_HEARTBEAT_COUNTER = METADATA_REGISTER_COUNTER
				.labels("heartbeat");

	}
}