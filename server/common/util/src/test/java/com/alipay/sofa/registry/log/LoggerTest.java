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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2018/12/29
 */
public class LoggerTest {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger("TRACE-LOGGER");
    private static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("DEBUG-LOGGER");
    private static final Logger INFO_LOGGER  = LoggerFactory.getLogger("INFO-LOGGER");
    private static final Logger WARN_LOGGER  = LoggerFactory.getLogger("WARN-LOGGER");
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ERROR-LOGGER");

    @Test
    public void levelTest() {
        Assert.assertTrue(TRACE_LOGGER.isTraceEnabled());
        Assert.assertTrue(TRACE_LOGGER.isDebugEnabled());
        Assert.assertTrue(TRACE_LOGGER.isInfoEnabled());
        Assert.assertTrue(TRACE_LOGGER.isWarnEnabled());
        Assert.assertTrue(TRACE_LOGGER.isErrorEnabled());

        Assert.assertFalse(DEBUG_LOGGER.isTraceEnabled());
        Assert.assertTrue(DEBUG_LOGGER.isDebugEnabled());
        Assert.assertTrue(DEBUG_LOGGER.isInfoEnabled());
        Assert.assertTrue(DEBUG_LOGGER.isWarnEnabled());
        Assert.assertTrue(DEBUG_LOGGER.isErrorEnabled());

        Assert.assertFalse(INFO_LOGGER.isTraceEnabled());
        Assert.assertFalse(INFO_LOGGER.isDebugEnabled());
        Assert.assertTrue(INFO_LOGGER.isInfoEnabled());
        Assert.assertTrue(INFO_LOGGER.isWarnEnabled());
        Assert.assertTrue(INFO_LOGGER.isErrorEnabled());

        Assert.assertFalse(WARN_LOGGER.isTraceEnabled());
        Assert.assertFalse(WARN_LOGGER.isDebugEnabled());
        Assert.assertFalse(WARN_LOGGER.isInfoEnabled());
        Assert.assertTrue(WARN_LOGGER.isWarnEnabled());
        Assert.assertTrue(WARN_LOGGER.isErrorEnabled());

        Assert.assertFalse(ERROR_LOGGER.isTraceEnabled());
        Assert.assertFalse(ERROR_LOGGER.isDebugEnabled());
        Assert.assertFalse(ERROR_LOGGER.isInfoEnabled());
        Assert.assertFalse(ERROR_LOGGER.isWarnEnabled());
        Assert.assertTrue(ERROR_LOGGER.isErrorEnabled());
    }

    @Test
    public void logTest() {
        TRACE_LOGGER.trace("First trace log.");
        TRACE_LOGGER.trace("trace log with one parameter {}.", "value1");
        TRACE_LOGGER.trace("trace log with two parameter {}, {}.", "value1", "value2");
        TRACE_LOGGER.trace("trace log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        TRACE_LOGGER.trace("trace log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        TRACE_LOGGER.trace("trace log with throwable.", new RuntimeException(
            "trace log with throwable RuntimeException"));
        TRACE_LOGGER.trace("trace log with parameter and throwable {} {} {}.", "value1", "value2",
            "value3", new RuntimeException(
                "trace log with parameter and throwable RuntimeException"));

        Assert.assertTrue(TestAppender.containsMessage("First trace log."));
        Assert.assertTrue(TestAppender.containsMessage("trace log with one parameter value1."));
        Assert.assertTrue(TestAppender
            .containsMessage("trace log with two parameter value1, value2."));
        Assert.assertTrue(TestAppender
            .containsMessage("trace log with three parameter value1, value2, value3."));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable("trace log with throwable.",
            new RuntimeException("trace log with throwable RuntimeException")));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable(
            "trace log with parameter and throwable value1 value2 value3.", new RuntimeException(
                "trace log with parameter and throwable RuntimeException")));

        DEBUG_LOGGER.debug("First debug log.");
        DEBUG_LOGGER.debug("debug log with one parameter {}.", "value1");
        DEBUG_LOGGER.debug("debug log with two parameter {}, {}.", "value1", "value2");
        DEBUG_LOGGER.debug("debug log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        DEBUG_LOGGER.debug("debug log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        DEBUG_LOGGER.debug("debug log with throwable.", new RuntimeException(
            "debug log with throwable RuntimeException"));
        DEBUG_LOGGER.debug("debug log with parameter and throwable {} {} {}.", "value1", "value2",
            "value3", new RuntimeException(
                "debug log with parameter and throwable RuntimeException"));

        Assert.assertTrue(TestAppender.containsMessage("First debug log."));
        Assert.assertTrue(TestAppender.containsMessage("debug log with one parameter value1."));
        Assert.assertTrue(TestAppender
            .containsMessage("debug log with two parameter value1, value2."));
        Assert.assertTrue(TestAppender
            .containsMessage("debug log with three parameter value1, value2, value3."));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable("debug log with throwable.",
            new RuntimeException("debug log with throwable RuntimeException")));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable(
            "debug log with parameter and throwable value1 value2 value3.", new RuntimeException(
                "debug log with parameter and throwable RuntimeException")));

        INFO_LOGGER.info("First info log.");
        INFO_LOGGER.info("info log with one parameter {}.", "value1");
        INFO_LOGGER.info("info log with two parameter {}, {}.", "value1", "value2");
        INFO_LOGGER.info("info log with three parameter {}, {}, {}.", "value1", "value2", "value3");
        INFO_LOGGER.info("info log with three parameter {}, {}, {}.", "value1", "value2", "value3");
        INFO_LOGGER.info("info log with throwable.", new RuntimeException(
            "info log with throwable RuntimeException"));
        INFO_LOGGER.info("info log with parameter and throwable {} {} {}.", "value1", "value2",
            "value3",
            new RuntimeException("info log with parameter and throwable RuntimeException"));

        Assert.assertTrue(TestAppender.containsMessage("First info log."));
        Assert.assertTrue(TestAppender.containsMessage("info log with one parameter value1."));
        Assert.assertTrue(TestAppender
            .containsMessage("info log with two parameter value1, value2."));
        Assert.assertTrue(TestAppender
            .containsMessage("info log with three parameter value1, value2, value3."));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable("info log with throwable.",
            new RuntimeException("info log with throwable RuntimeException")));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable(
            "info log with parameter and throwable value1 value2 value3.", new RuntimeException(
                "info log with parameter and throwable RuntimeException")));

        WARN_LOGGER.warn("First warn log.");
        WARN_LOGGER.warn("warn log with one parameter {}.", "value1");
        WARN_LOGGER.warn("warn log with two parameter {}, {}.", "value1", "value2");
        WARN_LOGGER.warn("warn log with three parameter {}, {}, {}.", "value1", "value2", "value3");
        WARN_LOGGER.warn("warn log with three parameter {}, {}, {}.", "value1", "value2", "value3");
        WARN_LOGGER.warn("warn log with throwable.", new RuntimeException(
            "warn log with throwable RuntimeException"));
        WARN_LOGGER.warn("warn log with parameter and throwable {} {} {}.", "value1", "value2",
            "value3",
            new RuntimeException("warn log with parameter and throwable RuntimeException"));

        Assert.assertTrue(TestAppender.containsMessage("First warn log."));
        Assert.assertTrue(TestAppender.containsMessage("warn log with one parameter value1."));
        Assert.assertTrue(TestAppender
            .containsMessage("warn log with two parameter value1, value2."));
        Assert.assertTrue(TestAppender
            .containsMessage("warn log with three parameter value1, value2, value3."));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable("warn log with throwable.",
            new RuntimeException("warn log with throwable RuntimeException")));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable(
            "warn log with parameter and throwable value1 value2 value3.", new RuntimeException(
                "warn log with parameter and throwable RuntimeException")));

        ERROR_LOGGER.error("First error log.");
        ERROR_LOGGER.error("error log with one parameter {}.", "value1");
        ERROR_LOGGER.error("error log with two parameter {}, {}.", "value1", "value2");
        ERROR_LOGGER.error("error log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        ERROR_LOGGER.error("error log with three parameter {}, {}, {}.", "value1", "value2",
            "value3");
        ERROR_LOGGER.error("error log with throwable.", new RuntimeException(
            "error log with throwable RuntimeException"));
        ERROR_LOGGER.error("error log with parameter and throwable {} {} {}.", "value1", "value2",
            "value3", new RuntimeException(
                "error log with parameter and throwable RuntimeException"));

        Assert.assertTrue(TestAppender.containsMessage("First error log."));
        Assert.assertTrue(TestAppender.containsMessage("error log with one parameter value1."));
        Assert.assertTrue(TestAppender
            .containsMessage("error log with two parameter value1, value2."));
        Assert.assertTrue(TestAppender
            .containsMessage("error log with three parameter value1, value2, value3."));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable("error log with throwable.",
            new RuntimeException("error log with throwable RuntimeException")));
        Assert.assertTrue(TestAppender.containsMessageAndThrowable(
            "error log with parameter and throwable value1 value2 value3.", new RuntimeException(
                "error log with parameter and throwable RuntimeException")));
    }
}
