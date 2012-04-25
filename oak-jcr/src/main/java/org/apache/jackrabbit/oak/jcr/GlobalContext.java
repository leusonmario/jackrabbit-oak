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

package org.apache.jackrabbit.oak.jcr;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Repository;

import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.jcr.util.Unchecked;

/**
 * Poor man's dependency injection
 * todo: OAK-17: replace by some more sophisticated mechanism
 * This class implements a poor man's dependency injection mechanism.
 * It should be replaced by a more sophisticated mechanism for compile
 * time dependency injection mechanism.
 */
public class GlobalContext {

    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public GlobalContext(
            Repository repository, ContentRepository contentRepository) {
        put(Repository.class, repository);
        put(ContentRepository.class, contentRepository);
    }

    public <T> T getInstance(Class<T> forClass) {
        T instance = Unchecked.<T>cast(instances.get(forClass));
        if (instance == null) {
            throw new IllegalStateException(
                    format("Global context does not contain {0}", forClass));
        }
        return instance;
    }

    //------------------------------------------< private >---

    private <T, I extends T> void put(Class<T> classType, I instance) {
        if (instances.containsKey(classType)) {
            throw new IllegalStateException(
                    format("Global context already contains {0}", classType));
        }
        instances.put(classType, instance);
    }

}
