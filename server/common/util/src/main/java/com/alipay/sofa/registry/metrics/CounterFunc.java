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
package com.alipay.sofa.registry.metrics;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.SimpleCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CounterFunc extends SimpleCollector<CounterFunc.Child>
    implements Collector.Describable {

  CounterFunc(Builder b) {
    super(b);
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<>(children.size());
    for (Map.Entry<List<String>, CounterFunc.Child> c : children.entrySet()) {
      samples.add(
          new MetricFamilySamples.Sample(fullname, labelNames, c.getKey(), c.getValue().get()));
    }
    return familySamplesList(Type.COUNTER, samples);
  }

  @Override
  public List<MetricFamilySamples> describe() {
    return Collections.<MetricFamilySamples>singletonList(
        new CounterMetricFamily(fullname, help, labelNames));
  }

  @Override
  protected Child newChild() {
    return new Child();
  }

  public static class Builder extends SimpleCollector.Builder<Builder, CounterFunc> {

    @Override
    public CounterFunc create() {
      return new CounterFunc(this);
    }
  }

  public static Builder build() {
    return new Builder();
  }

  public static class Child {
    private CounterFuncCallable callable;

    public synchronized Child func(CounterFuncCallable c) {
      callable = c;
      return this;
    }

    public double get() {
      return callable.get();
    }
  }

  public synchronized CounterFunc func(CounterFuncCallable c) {
    noLabelsChild.func(c);
    return this;
  }

  public interface CounterFuncCallable {
    long get();
  }
}
