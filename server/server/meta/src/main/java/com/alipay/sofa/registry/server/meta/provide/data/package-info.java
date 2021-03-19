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
package com.alipay.sofa.registry.server.meta.provide.data;

/**
 * The so called "Provide Data" is designed for two scenario, as below: 1. Dynamic Configurations
 * inter-active between Sofa-Registry system 2. Service Gaven (or say 'Watcher') are
 * subscribing/watching messages through Session-Server
 *
 * <p>All above user cases stage a Config Center role by Sofa-Registry And all these infos are
 * mandatory being persistence to disk The idea is simple, leveraging meta server's JRaft feature,
 * infos are reliable and stable to be stored on MetaServer
 *
 * <p>So, besides meta-info control, another functionality has been assigned to MetaServer, that it
 * holds some dynamic configs or receive changes from session. And then send out a notification to
 * data-servers/session servers, so that these two buddy could come and take some stuff it needs
 *
 * <p>Not a big deal, but it's a 'must' feature for MetaServer, be careful if you want to refactor
 * this feature
 */
