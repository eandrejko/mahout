/**
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

package org.apache.mahout.fpm.pfpgrowth.convertors.integer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.mahout.common.Pair;
import org.apache.mahout.fpm.pfpgrowth.convertors.string.TopKStringPatterns;

public final class IntegerStringOutputConvertor implements
    OutputCollector<Integer, List<Pair<List<Integer>, Long>>> {

  private OutputCollector<Text, TopKStringPatterns> collector = null;

  private List<String> featureReverseMap = null;

  public IntegerStringOutputConvertor(
      OutputCollector<Text, TopKStringPatterns> collector,
      List<String> featureReverseMap) {
    this.collector = collector;
    this.featureReverseMap = featureReverseMap;
  }

  @Override
  public final void collect(Integer key, List<Pair<List<Integer>, Long>> value)
      throws IOException {
    String StringKey = featureReverseMap.get(key);
    List<Pair<List<String>, Long>> stringValues = new ArrayList<Pair<List<String>, Long>>();
    for (Pair<List<Integer>, Long> e : value) {
      List<String> pattern = new ArrayList<String>();
      for (Integer i : e.getFirst()) {
        pattern.add(featureReverseMap.get(i));
      }
      stringValues.add(new Pair<List<String>, Long>(pattern, e.getSecond()));
    }

    collector
        .collect(new Text(StringKey), new TopKStringPatterns(stringValues));
  }

}