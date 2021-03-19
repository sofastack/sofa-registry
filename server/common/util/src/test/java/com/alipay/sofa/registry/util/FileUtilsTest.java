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
package com.alipay.sofa.registry.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
public class FileUtilsTest {
  @Test
  public void doTest() throws Exception {
    File dir = new File(System.getProperty("user.home") + File.separator + "FileUtilsTestDir");
    File file = new File(dir, "FileUtilsTest");
    FileUtils.forceDelete(dir);

    String data = "FileUtilsTest";
    new FileUtils().writeByteArrayToFile(file, data.getBytes(), true);
    byte[] readByte = FileUtils.readFileToByteArray(file);
    assertEquals(data, new String(readByte));

    boolean throwException = false;
    try {
      FileUtils.forceMkdir(file);
    } catch (Exception e) {
      throwException = true;
      assertTrue(e.getClass() == IOException.class);
      assertTrue(
          e.getMessage()
              .contains(
                  "FileUtilsTest exists and is not a directory. Unable to create directory."));
    }
    if (!throwException) {
      fail("should throw Exception.");
    }
    FileUtils.forceDelete(file);
    FileUtils.forceDelete(dir);

    FileUtils.forceMkdir(dir);
    assertTrue(dir.exists());
    assertTrue(dir.isDirectory());
    FileUtils.forceDelete(dir);
  }
}
