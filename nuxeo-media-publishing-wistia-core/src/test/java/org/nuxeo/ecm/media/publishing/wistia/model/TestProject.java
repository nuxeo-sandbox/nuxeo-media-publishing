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

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;

public class TestProject {

    private Project project;
    private Date testDate;

    @Before
    public void setUp() {
        project = new Project();
        testDate = new Date();
    }

    @Test
    public void testProjectProperties() {
        // Test basic properties
        project.setId(123);
        assertEquals(123, project.getId());

        project.setName("Test Project");
        assertEquals("Test Project", project.getName());

        project.setDescription("Test Description");
        assertEquals("Test Description", project.getDescription());

        project.setMediaCount(5);
        assertEquals(5, project.getMediaCount());

        project.setHashedId("test_hashed_id");
        assertEquals("test_hashed_id", project.getHashedId());

        project.setCreatedAt(testDate);
        assertEquals(testDate, project.getCreatedAt());

        project.setUpdatedAt(testDate);
        assertEquals(testDate, project.getUpdatedAt());
    }

    @Test
    public void testProjectFlags() {
        // Test boolean properties
        project.setAnonymousCanUpload(true);
        assertTrue(project.getAnonymousCanUpload());

        project.setAnonymousCanDownload(false);
        assertFalse(project.getAnonymousCanDownload());

        project.setPublic(true);
        assertTrue(project.isPublic());

        project.setPublicId("public_123");
        assertEquals("public_123", project.getPublicId());
    }

    @Test
    public void testProjectMedias() {
        // Test that getMedias returns empty list by default
        List<Media> medias = project.getMedias();
        assertNotNull("Medias list should not be null", medias);
        assertTrue("Medias list should be empty by default", medias.isEmpty());

        // Test adding media
        Media media = new Media();
        media.setId(456);
        media.setName("Test Media");

        project.addMedia(media);
        assertEquals(1, project.getMedias().size());
        assertEquals(media, project.getMedias().get(0));
    }

    @Test
    public void testToString() {
        project.setId(123);
        project.setName("Test Project");
        project.setHashedId("test_hashed_id");

        String result = project.toString();
        assertNotNull("toString should not return null", result);
        assertTrue("toString should contain project info", result.contains("Project info"));
        assertTrue("toString should contain ID", result.contains("123"));
        assertTrue("toString should contain name", result.contains("Test Project"));
        assertTrue("toString should contain hashed ID", result.contains("test_hashed_id"));
    }

    @Test
    public void testDefaultValues() {
        Project newProject = new Project();
        
        assertEquals(0, newProject.getId());
        assertNull(newProject.getName());
        assertNull(newProject.getDescription());
        assertEquals(0, newProject.getMediaCount());
        assertNull(newProject.getHashedId());
        assertNull(newProject.getCreatedAt());
        assertNull(newProject.getUpdatedAt());
        assertNull(newProject.getPublicId());
        assertFalse(newProject.getAnonymousCanUpload());
        assertFalse(newProject.getAnonymousCanDownload());
        assertFalse(newProject.isPublic());
        assertNotNull(newProject.getMedias());
        assertTrue(newProject.getMedias().isEmpty());
    }
}