/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.spi.security.authorization.cug.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.jackrabbit.oak.AbstractSecurityTest;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.jackrabbit.oak.spi.mount.MountInfoProvider;
import org.apache.jackrabbit.oak.spi.mount.Mounts;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.security.authorization.AuthorizationConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

public class CugConfigurationWithMountsTest extends AbstractSecurityTest {

    private static CugConfiguration createConfiguration(MountInfoProvider mip) {
        ConfigurationParameters params = ConfigurationParameters.of(
                AbstractCugTest.CUG_CONFIG,
                ConfigurationParameters.of(CugConstants.PARAM_MOUNT_PROVIDER, mip));

        SecurityProvider sp = new CugSecurityProvider(ConfigurationParameters.of(ImmutableMap.of(AuthorizationConfiguration.NAME, params)));
        return new CugConfiguration(sp);
    }

    @Test
    public void testDefaultMountInfoProvider() {
        CugConfiguration configuration = createConfiguration(Mounts.defaultMountInfoProvider());

        ConfigurationParameters params = configuration.getParameters();
        assertSame(Mounts.defaultMountInfoProvider(), params.get(CugConstants.PARAM_MOUNT_PROVIDER));
    }

    @Test(expected = IllegalStateException.class)
    public void testMountAtCugSupportedPath() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", AbstractCugTest.SUPPORTED_PATH).build();
        CugConfiguration configuration = createConfiguration(mip);
    }

    @Test(expected = IllegalStateException.class)
    public void testMountBelowCugSupportedPath() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", AbstractCugTest.SUPPORTED_PATH + "/mount").build();
        CugConfiguration configuration = createConfiguration(mip);
    }

    @Test(expected = IllegalStateException.class)
    public void testMountAboveCugSupportedPath() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", PathUtils.getParentPath(AbstractCugTest.SUPPORTED_PATH3)).build();
        CugConfiguration configuration = createConfiguration(mip);
    }

    @Test(expected = IllegalStateException.class)
    public void testMountAtRootWithSupportedPaths() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", PathUtils.ROOT_PATH).build();
        CugConfiguration configuration = createConfiguration(mip);
    }

    @Test
    public void testMountAtUnsupportedPath() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", AbstractCugTest.UNSUPPORTED_PATH).build();
        CugConfiguration configuration = createConfiguration(mip);
        assertArrayEquals(AbstractCugTest.SUPPORTED_PATHS, configuration.getParameters().getConfigValue(CugConstants.PARAM_CUG_SUPPORTED_PATHS, new String[0]));
    }

    @Test
    public void testMountBelowUnsupportedPath() {
        MountInfoProvider mip = Mounts.newBuilder().mount("mnt", AbstractCugTest.UNSUPPORTED_PATH + "/mount").build();
        CugConfiguration configuration = createConfiguration(mip);
        assertArrayEquals(AbstractCugTest.SUPPORTED_PATHS, configuration.getParameters().getConfigValue(CugConstants.PARAM_CUG_SUPPORTED_PATHS, new String[0]));
    }
}