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
package com.alipay.sofa.registry.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuanbei
 * @since 2018/12/29
 */
public class TestAppender extends AppenderBase<LoggingEvent> {
    public static List<LoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(LoggingEvent e) {
        events.add(e);
    }

    public static boolean containsMessage(String message) {
        for (LoggingEvent loggingEvent : events) {
            if (loggingEvent.getFormattedMessage().contains(message)) {
                return true;
            }
        }

        return false;
    }

    public static boolean containsMessageAndThrowable(String message, Throwable throwable) {
        for (LoggingEvent loggingEvent : events) {
            if (loggingEvent.getFormattedMessage().contains(message)
                && loggingEvent.getThrowableProxy().getMessage().equals(throwable.getMessage())) {
                return true;
            }
        }

        return false;
    }
}