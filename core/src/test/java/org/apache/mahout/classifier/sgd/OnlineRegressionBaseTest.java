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

package org.apache.mahout.classifier.sgd;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.AbstractVectorRegression;
import org.apache.mahout.classifier.OnlineLearner;
import org.apache.mahout.common.MahoutTestCase;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.Functions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class OnlineRegressionBaseTest extends SGDTestCase {

    private Matrix input;

    Matrix getInput() {
        return input;
    }

    Vector readStandardData() throws IOException {
        // TODO update documentation
        // 500 test samples.  First column is constant.  Second and third are normally distributed from
        // either N([2,2], 1) (rows 0...29) or N([-2,-2], 1) (rows 30...59).  The first 30 rows have a
        // target variable of 0, the last 30 a target of 1.  The remaining columns are are random noise.
        input = readCsv("sgd-regression.csv");

        // regenerate the target variable
        Vector target = new DenseVector(500);
        target.assign(1);
        return target;
    }

    static void train(Matrix input, Vector target, OnlineRegression regression) {
        RandomUtils.useTestSeed();
        Random gen = RandomUtils.getRandom();

        // train on samples in random order (but only one pass)
        for (int row : permute(gen, 500)) {
            regression.train((int) target.get(row), input.viewRow(row));
        }
        regression.close();
    }

    static void test(Matrix input, Vector target, AbstractVectorRegression lr,
                     double expected_mean_error, double expected_absolute_error) {
        // now test the accuracy
        Vector tmp = lr.predict(input);
        // mean(abs(tmp - target))
        double meanAbsoluteError = tmp.minus(target).aggregate(Functions.PLUS, Functions.ABS) / input.numRows();

        // max(abs(tmp - target)
        double maxAbsoluteError = tmp.minus(target).aggregate(Functions.MAX, Functions.ABS);

        System.out.printf("mAE = %.4f, maxAE = %.4f\n", meanAbsoluteError, maxAbsoluteError);
        assertEquals(0, meanAbsoluteError , expected_mean_error);
        assertEquals(0, maxAbsoluteError, expected_absolute_error);

        // convenience methods should give the same results
        Vector v = lr.predict(input);
        assertEquals(0, v.minus(tmp).norm(1), 1.0e-5);
        v = lr.predict(input);
        assertEquals(0, v.minus(tmp).norm(1), 1.0e-4);
    }

}
