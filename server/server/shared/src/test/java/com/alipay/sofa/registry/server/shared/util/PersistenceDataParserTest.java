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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import org.junit.Assert;
import org.junit.Test;

public class PersistenceDataParserTest {
  @Test
  public void missingAndBlankValuesUseTheCallersDefault() {
    for (boolean defaultValue : new boolean[] {false, true}) {
      Assert.assertEquals(
          defaultValue,
          PersistenceDataParser.parse2BoolIgnoreCase((PersistenceData) null, defaultValue));
      for (String value : new String[] {null, "", " \t"}) {
        Assert.assertEquals(
            defaultValue, PersistenceDataParser.parse2BoolIgnoreCase(data(value), defaultValue));
      }
    }
  }

  @Test
  public void explicitValuesOverrideTheDefaultIgnoringCase() {
    for (boolean defaultValue : new boolean[] {false, true}) {
      for (String value : new String[] {"true", "TRUE", "TrUe"}) {
        Assert.assertTrue(PersistenceDataParser.parse2BoolIgnoreCase(data(value), defaultValue));
      }
      for (String value : new String[] {"false", "FALSE", "invalid"}) {
        Assert.assertFalse(PersistenceDataParser.parse2BoolIgnoreCase(data(value), defaultValue));
      }
    }
  }

  @Test
  public void missingOrUnsuccessfulResponsesUseTheDefaultEvenWithAnEntity() {
    for (boolean defaultValue : new boolean[] {false, true}) {
      Assert.assertEquals(
          defaultValue,
          PersistenceDataParser.parse2BoolIgnoreCase(
              (DBResponse<PersistenceData>) null, defaultValue));
      Assert.assertEquals(
          defaultValue,
          PersistenceDataParser.parse2BoolIgnoreCase(
              new DBResponse<PersistenceData>(null, OperationStatus.SUCCESS), defaultValue));
      Assert.assertEquals(
          defaultValue,
          PersistenceDataParser.parse2BoolIgnoreCase(
              new DBResponse<>(data(Boolean.toString(!defaultValue)), OperationStatus.NOTFOUND),
              defaultValue));
    }
  }

  @Test
  public void successfulResponseParsesItsEntity() {
    Assert.assertTrue(
        PersistenceDataParser.parse2BoolIgnoreCase(
            new DBResponse<>(data("TRUE"), OperationStatus.SUCCESS), false));
    Assert.assertFalse(
        PersistenceDataParser.parse2BoolIgnoreCase(
            new DBResponse<>(data("false"), OperationStatus.SUCCESS), true));
    Assert.assertTrue(
        PersistenceDataParser.parse2BoolIgnoreCase(
            new DBResponse<>(data(""), OperationStatus.SUCCESS), true));
  }

  private static PersistenceData data(String value) {
    PersistenceData data = new PersistenceData();
    data.setData(value);
    return data;
  }
}
