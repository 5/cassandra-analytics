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

package org.apache.cassandra.spark.utils;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.cassandra.spark.common.Digest;

/**
 * Interface that computes a {@link Digest}
 */
public interface DigestAlgorithm
{
    /**
     * Calculates the {@link Digest} for the given file in the {@code path}.
     *
     * @param path the path of the file
     * @return the calculated digest for the given {@code path}
     * @throws IOException when an error occurs while reading the file or calculating the digest
     */
    Digest calculateFileDigest(Path path) throws IOException;
}
