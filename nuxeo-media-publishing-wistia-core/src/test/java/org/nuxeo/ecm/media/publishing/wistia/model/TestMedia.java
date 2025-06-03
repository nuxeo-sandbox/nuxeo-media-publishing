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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.media.publishing.wistia.model.Assets;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;

public class TestMedia {

    private Media media;
    private Date testDate;

    @Before
    public void setUp() {
        media = new Media();
        testDate = new Date();
    }

    @Test
    public void testMediaProperties() {
        // Test basic properties
        media.setId(123);
        assertEquals(123, media.getId());

        media.setName("Test Media");
        assertEquals("Test Media", media.getName());

        media.setType("Video");
        assertEquals("Video", media.getType());

        media.setHashedId("test_hashed_id");
        assertEquals("test_hashed_id", media.getHashedId());

        media.setDuration(120.5f);
        assertEquals(120.5f, media.getDuration(), 0.001);

        media.setCreatedAt(testDate);
        assertEquals(testDate, media.getCreatedAt());

        media.setUpdatedAt(testDate);
        assertEquals(testDate, media.getUpdatedAt());
    }

    @Test
    public void testMediaWithProject() {
        Project project = new Project();
        project.setId(456);
        project.setName("Test Project");

        media.setProject(project);
        assertEquals(project, media.getProject());
    }

    @Test
    public void testMediaWithStats() {
        Stats stats = new Stats();
        stats.setVisitors(100);
        stats.setPlays(250);

        media.setStats(stats);
        assertEquals(stats, media.getStats());
    }

    @Test
    public void testMediaAssets() {
        // Test that getAssets returns empty list by default
        List<Assets> assets = media.getAssets();
        assertNotNull("Assets list should not be null", assets);
        assertTrue("Assets list should be empty by default", assets.isEmpty());
    }

    @Test
    public void testToString() {
        media.setId(123);
        media.setName("Test Media");
        media.setHashedId("test_hashed_id");

        String result = media.toString();
        assertNotNull("toString should not return null", result);
        assertTrue("toString should contain media info", result.contains("Media info"));
        assertTrue("toString should contain ID", result.contains("123"));
        assertTrue("toString should contain name", result.contains("Test Media"));
        assertTrue("toString should contain hashed ID", result.contains("test_hashed_id"));
    }

    @Test
    public void testDefaultValues() {
        Media newMedia = new Media();
        
        assertEquals(0, newMedia.getId());
        assertNull(newMedia.getName());
        assertNull(newMedia.getType());
        assertNull(newMedia.getHashedId());
        assertEquals(0.0f, newMedia.getDuration(), 0.001);
        assertNull(newMedia.getCreatedAt());
        assertNull(newMedia.getUpdatedAt());
        assertNull(newMedia.getProject());
        assertNull(newMedia.getStats());
        assertNotNull(newMedia.getAssets());
    }
}