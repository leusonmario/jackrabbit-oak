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
package org.apache.jackrabbit.oak.jcr.security.privilege;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.oak.security.privilege.PrivilegeConstants;
import org.junit.Ignore;
import org.junit.Test;

/**
 * PrivilegeManagerTest...
 *
 * TODO: more tests for cyclic aggregation
 */
public class PrivilegeManagerImplTest extends AbstractPrivilegeTest {

    public void testGetRegisteredPrivileges() throws RepositoryException {
        Privilege[] registered = privilegeManager.getRegisteredPrivileges();
        Set<Privilege> set = new HashSet<Privilege>();
        Privilege all = privilegeManager.getPrivilege(Privilege.JCR_ALL);
        set.add(all);
        set.addAll(Arrays.asList(all.getAggregatePrivileges()));

        for (Privilege p : registered) {
            assertTrue(set.remove(p));
        }
        assertTrue(set.isEmpty());
    }
    
    public void testGetPrivilege() throws RepositoryException {
        for (String privName : NON_AGGR_PRIVILEGES) {
            Privilege p = privilegeManager.getPrivilege(privName);
            assertPrivilege(p, privName, false, false);
        }

        for (String privName : AGGR_PRIVILEGES) {
            Privilege p = privilegeManager.getPrivilege(privName);
            assertPrivilege(p, privName, true, false);
        }
    }

    public void testJcrAll() throws RepositoryException {
        Privilege all = privilegeManager.getPrivilege(Privilege.JCR_ALL);
        assertPrivilege(all, JCR_ALL, true, false);

        List<Privilege> decl = Arrays.asList(all.getDeclaredAggregatePrivileges());
        List<Privilege> aggr = new ArrayList<Privilege>(Arrays.asList(all.getAggregatePrivileges()));

        assertFalse(decl.contains(all));
        assertFalse(aggr.contains(all));

        // declared and aggregated privileges are the same for jcr:all
        assertTrue(decl.containsAll(aggr));

        // test individual built-in privileges are listed in the aggregates
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_READ)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_ADD_CHILD_NODES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_REMOVE_CHILD_NODES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_MODIFY_PROPERTIES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_REMOVE_NODE)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_READ_ACCESS_CONTROL)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_MODIFY_ACCESS_CONTROL)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_LIFECYCLE_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_LOCK_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_NODE_TYPE_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_RETENTION_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_VERSION_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(Privilege.JCR_WRITE)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.REP_WRITE)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.REP_ADD_PROPERTIES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.REP_ALTER_PROPERTIES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.REP_REMOVE_PROPERTIES)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.JCR_NAMESPACE_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.JCR_NODE_TYPE_DEFINITION_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.JCR_WORKSPACE_MANAGEMENT)));
        assertTrue(aggr.remove(privilegeManager.getPrivilege(PrivilegeConstants.REP_PRIVILEGE_MANAGEMENT)));

        // there may be no privileges left
        assertTrue(aggr.isEmpty());
    }

    @Test
    public void testGetPrivilegeFromName() throws AccessControlException, RepositoryException {
        Privilege p = privilegeManager.getPrivilege(Privilege.JCR_READ);

        assertTrue(p != null);
        assertEquals("jcr:read", p.getName());
        assertFalse(p.isAggregate());

        p = privilegeManager.getPrivilege(Privilege.JCR_WRITE);

        assertTrue(p != null);
        assertEquals("jcr:write", p.getName());
        assertTrue(p.isAggregate());
    }

    @Test
    public void testGetPrivilegesFromInvalidName() throws RepositoryException {
        try {
            privilegeManager.getPrivilege("unknown");
            fail("invalid privilege name");
        } catch (AccessControlException e) {
            // OK
        }
    }

    @Test
    public void testGetPrivilegesFromEmptyNames() {
        try {
            privilegeManager.getPrivilege("");
            fail("invalid privilege name array");
        } catch (AccessControlException e) {
            // OK
        } catch (RepositoryException e) {
            // OK
        }
    }

    @Test
    public void testGetPrivilegesFromNullNames() {
        try {
            privilegeManager.getPrivilege(null);
            fail("invalid privilege name (null)");
        } catch (Exception e) {
            // OK
        }
    }
}