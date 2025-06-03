/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Test Suite Author
 */

package org.nuxeo.ecm.media.publishing.wistia.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.media.publishing.wistia.model.Account;

public class TestAccount {

    private Account account;

    @Before
    public void setUp() {
        account = new Account();
    }

    @Test
    public void testAccountProperties() {
        // Test all account properties
        account.setId(123);
        assertEquals(123, account.getId());

        account.setName("Test Account");
        assertEquals("Test Account", account.getName());

        account.setUrl("https://test.wistia.com");
        assertEquals("https://test.wistia.com", account.getUrl());
    }

    @Test
    public void testDefaultValues() {
        Account newAccount = new Account();
        
        assertEquals(0, newAccount.getId());
        assertNull(newAccount.getName());
        assertNull(newAccount.getUrl());
    }

    @Test
    public void testToString() {
        account.setId(123);
        account.setName("Test Account");
        account.setUrl("https://test.wistia.com");

        String result = account.toString();
        assertNotNull("toString should not return null", result);
        assertTrue("toString should contain account info", result.contains("Account info"));
        assertTrue("toString should contain ID", result.contains("123"));
        assertTrue("toString should contain name", result.contains("Test Account"));
        assertTrue("toString should contain URL", result.contains("https://test.wistia.com"));
    }

    @Test
    public void testEmptyAndNullValues() {
        // Test with empty values
        account.setName("");
        assertEquals("", account.getName());

        account.setUrl("");
        assertEquals("", account.getUrl());

        // Test with null values
        account.setName(null);
        assertNull(account.getName());

        account.setUrl(null);
        assertNull(account.getUrl());
    }
}
