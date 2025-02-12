/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cassandra.spark.bulkwriter.token;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.cassandra.spark.common.model.CassandraInstance;
import org.apache.cassandra.spark.data.ReplicationFactor;

import static org.apache.cassandra.spark.bulkwriter.token.ConsistencyLevel.CL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BulkWriterConsistencyLevelTest
{
    private static final ReplicationFactor replicationFactor = new ReplicationFactor(ImmutableMap.of(
    "class", "NetworkTopologyStrategy",
    "dc1", "3"));

    private static List<CassandraInstance> succeededNone = Collections.emptyList();
    private static List<CassandraInstance> succeededOne;
    private static List<CassandraInstance> succeededTwo;
    private static List<CassandraInstance> succeededThree;

    private static Set<String> zero = Collections.emptySet();
    private static Set<String> one = intToSet(1);
    private static Set<String> two = intToSet(2);
    private static Set<String> three = intToSet(3);

    @BeforeAll
    static void setup()
    {
        CassandraInstance i1 = mockInstance("dc1");
        CassandraInstance i2 = mockInstance("dc1");
        CassandraInstance i3 = mockInstance("dc1");
        succeededOne = Arrays.asList(i1);
        succeededTwo = Arrays.asList(i1, i2);
        succeededThree = Arrays.asList(i1, i2, i3);
    }

    @Test
    void testCanBeSatisfiedReturnsTrue()
    {
        testCanBeSatisfied(CL.ONE, succeededOne, true);
        testCanBeSatisfied(CL.ONE, succeededTwo, true);
        testCanBeSatisfied(CL.ONE, succeededThree, true);

        testCanBeSatisfied(CL.TWO, succeededTwo, true);
        testCanBeSatisfied(CL.TWO, succeededThree, true);

        testCanBeSatisfied(CL.LOCAL_ONE, succeededOne, true);
        testCanBeSatisfied(CL.LOCAL_ONE, succeededTwo, true);
        testCanBeSatisfied(CL.LOCAL_ONE, succeededThree, true);

        testCanBeSatisfied(CL.LOCAL_QUORUM, succeededTwo, true);
        testCanBeSatisfied(CL.LOCAL_QUORUM, succeededThree, true);

        testCanBeSatisfied(CL.EACH_QUORUM, succeededTwo, true);
        testCanBeSatisfied(CL.EACH_QUORUM, succeededThree, true);

        testCanBeSatisfied(CL.QUORUM, succeededTwo, true);
        testCanBeSatisfied(CL.QUORUM, succeededThree, true);

        testCanBeSatisfied(CL.ALL, succeededThree, true);
    }

    @Test
    void testCanBeSatisfiedReturnsFalse()
    {
        testCanBeSatisfied(CL.ONE, succeededNone, false);

        testCanBeSatisfied(CL.TWO, succeededNone, false);
        testCanBeSatisfied(CL.TWO, succeededOne, false);

        testCanBeSatisfied(CL.LOCAL_ONE, succeededNone, false);

        testCanBeSatisfied(CL.LOCAL_QUORUM, succeededNone, false);
        testCanBeSatisfied(CL.LOCAL_QUORUM, succeededOne, false);

        testCanBeSatisfied(CL.EACH_QUORUM, succeededNone, false);
        testCanBeSatisfied(CL.EACH_QUORUM, succeededOne, false);

        testCanBeSatisfied(CL.QUORUM, succeededNone, false);
        testCanBeSatisfied(CL.QUORUM, succeededOne, false);

        testCanBeSatisfied(CL.ALL, succeededNone, false);
        testCanBeSatisfied(CL.ALL, succeededOne, false);
        testCanBeSatisfied(CL.ALL, succeededTwo, false);
    }

    @Test
    void testCheckConsistencyReturnsTrue()
    {
        testCheckConsistency(CL.ONE, /* total */ three, /* failed */ zero, zero, true);
        testCheckConsistency(CL.ONE, /* total */ three, /* failed */ one, zero, true);
        testCheckConsistency(CL.ONE, /* total */ three, /* failed */ two, zero, true);

        testCheckConsistency(CL.TWO, /* total */ three, /* failed */ zero, zero, true);
        testCheckConsistency(CL.TWO, /* total */ three, /* failed */ one, zero, true);

        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ zero, /* pending */ zero, true);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ zero, /* pending */ one, true);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ zero, /* pending */ two, true);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ one, /* pending */ one, true);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ two, /* pending */ zero, true);

        testCheckConsistency(CL.LOCAL_QUORUM, /* total */ three, /* failed */ zero, zero, true);
        testCheckConsistency(CL.LOCAL_QUORUM, /* total */ three, /* failed */ one, zero, true);

        testCheckConsistency(CL.EACH_QUORUM, /* total */ three, /* failed */ zero, zero, true);
        testCheckConsistency(CL.EACH_QUORUM, /* total */ three, /* failed */ one, zero, true);

        testCheckConsistency(CL.QUORUM, /* total */ three, /* failed */ zero, zero, true);
        testCheckConsistency(CL.QUORUM, /* total */ three, /* failed */ one, zero, true);

        testCheckConsistency(CL.ALL, /* total */ three, /* failed */ zero, zero, true);
    }

    @Test
    void testCheckConsistencyReturnsFalse()
    {
        testCheckConsistency(CL.ONE, /* total */ three, /* failed */ three, zero, false);

        testCheckConsistency(CL.TWO, /* total */ three, /* failed */ three, zero, false);
        testCheckConsistency(CL.TWO, /* total */ three, /* failed */ two, zero, false);

        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ three, /* pending */ zero, false);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ two, /* pending */ one, false);
        testCheckConsistency(CL.LOCAL_ONE, /* total */ three, /* failed */ one, /* pending */ two, false);

        testCheckConsistency(CL.LOCAL_QUORUM, /* total */ three, /* failed */ three, zero, false);
        testCheckConsistency(CL.LOCAL_QUORUM, /* total */ three, /* failed */ two, zero, false);

        testCheckConsistency(CL.EACH_QUORUM, /* total */ three, /* failed */ three, zero, false);
        testCheckConsistency(CL.EACH_QUORUM, /* total */ three, /* failed */ two, zero, false);

        testCheckConsistency(CL.QUORUM, /* total */ three, /* failed */ three, zero, false);
        testCheckConsistency(CL.QUORUM, /* total */ three, /* failed */ two, zero, false);

        testCheckConsistency(CL.ALL, /* total */ three, /* failed */ one, zero, false);
        testCheckConsistency(CL.ALL, /* total */ three, /* failed */ two, zero, false);
        testCheckConsistency(CL.ALL, /* total */ three, /* failed */ three, zero, false);
    }

    private void testCanBeSatisfied(ConsistencyLevel cl, List<CassandraInstance> succeeded, boolean expectedResult)
    {
        assertThat(cl.canBeSatisfied(succeeded, replicationFactor, "dc1")).isEqualTo(expectedResult);
    }

    private void testCheckConsistency(ConsistencyLevel cl, Set<String> total, Set<String> failed, Set<String> pending, boolean expectedResult)
    {
        assertThat(cl.checkConsistency(total, pending, zero, // replacement is not used
                                       zero, // include blocking instance set in failed set
                                       failed, "dc1")).isEqualTo(expectedResult);
    }

    private static CassandraInstance mockInstance(String dc)
    {
        CassandraInstance i = mock(CassandraInstance.class);
        when(i.datacenter()).thenReturn(dc);
        return i;
    }

    private static Set<String> intToSet(int i)
    {
        return IntStream.range(0, i).mapToObj(String::valueOf).collect(Collectors.toSet());
    }
}
