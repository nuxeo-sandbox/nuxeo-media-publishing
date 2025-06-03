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

package org.nuxeo.ecm.media.publishing.wistia;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.api.client.auth.oauth2.Credential;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ 
    "org.nuxeo.ecm.platform.tag", 
    "org.nuxeo.ecm.platform.video", 
    "nuxeo-media-publishing-core",
    "nuxeo-media-publishing-wistia-core",
    "nuxeo-media-publishing-wistia-core:wistia-test-provider-contrib.xml" 
})
public class TestWistiaService {

    @Inject
    CoreSession session;

    @Mock
    private WistiaClient mockWistiaClient;

    @Mock 
    private Credential mockCredential;

    @Mock
    private MediaPublishingProgressListener mockProgressListener;

    private WistiaService wistiaService;
    private PublishableMedia testMedia;
    private DocumentModel testDocument;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create test service
        wistiaService = new WistiaService("wistia");
        
        // Create test document
        testDocument = session.createDocumentModel("/", "testVideo", "Video");
        testDocument.setPropertyValue("dc:title", "Test Video");
        
        // Create test blob
        Blob testBlob = new StringBlob("test video content", "video/mp4", "UTF-8");
        testBlob.setFilename("test-video.mp4");
        testDocument.setPropertyValue("file:content", (java.io.Serializable) testBlob);
        
        testDocument = session.createDocument(testDocument);
        session.save();
        
        // Create publishable media
        testMedia = testDocument.getAdapter(PublishableMedia.class);
        assertNotNull("PublishableMedia adapter should not be null", testMedia);
    }

    @Test
    public void testGetWistiaClientWithNoCredential() {
        WistiaClient client = wistiaService.getWistiaClient("invalid_account");
        assertNull("Should return null when no credential found", client);
    }

    @Test(expected = NuxeoException.class)
    public void testUploadWithNoProjects() throws IOException {
        // Mock empty projects list
        WistiaService spyService = spy(wistiaService);
        doReturn(Collections.emptyList()).when(spyService).getProjects("test_account");
        
        Map<String, String> options = new HashMap<>();
        spyService.upload(testMedia, mockProgressListener, "test_account", options);
    }

    @Test
    public void testUploadWithProjectId() throws IOException {
        // Create mock objects
        Media mockMedia = new Media();
        mockMedia.setHashedId("test_hashed_id");
        
        // Mock service behavior
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient("test_account");
        when(mockWistiaClient.upload(anyString(), any(), any())).thenReturn(mockMedia);
        
        Map<String, String> options = new HashMap<>();
        options.put("project_id", "123");
        
        String result = spyService.upload(testMedia, mockProgressListener, "test_account", options);
        assertEquals("test_hashed_id", result);
    }

    @Test
    public void testUnpublishSuccess() {
        // Mock successful unpublish
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient(anyString());
        when(mockWistiaClient.deleteMedia(anyString())).thenReturn(new Media());
        
        // Set up media as if it was published
        Map<String, Object> providerData = new HashMap<>();
        providerData.put("provider", "wistia");
        providerData.put("externalId", "test_media_id");
        providerData.put("account", "test_account");
        testMedia.putProvider(providerData);
        
        boolean result = spyService.unpublish(testMedia);
        assertTrue("Unpublish should succeed", result);
    }

    @Test
    public void testUnpublishFailure() {
        // Mock failed unpublish
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient(anyString());
        when(mockWistiaClient.deleteMedia(anyString())).thenReturn(null);
        
        // Set up media as if it was published
        Map<String, Object> providerData = new HashMap<>();
        providerData.put("provider", "wistia");
        providerData.put("externalId", "test_media_id");
        providerData.put("account", "test_account");
        testMedia.putProvider(providerData);
        
        boolean result = spyService.unpublish(testMedia);
        assertFalse("Unpublish should fail", result);
    }

    @Test
    public void testGetPublishedUrl() {
        // Mock account and client
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient("test_account");
        
        // Mock account URL
        org.nuxeo.ecm.media.publishing.wistia.model.Account mockAccount = 
            new org.nuxeo.ecm.media.publishing.wistia.model.Account();
        mockAccount.setUrl("https://test.wistia.com");
        when(mockWistiaClient.getAccount()).thenReturn(mockAccount);
        
        String url = spyService.getPublishedUrl("test_media_id", "test_account");
        assertEquals("https://test.wistia.com/medias/test_media_id", url);
    }

    @Test
    public void testGetPublishedUrlWithNoClient() {
        WistiaService spyService = spy(wistiaService);
        doReturn(null).when(spyService).getWistiaClient("invalid_account");
        
        String url = spyService.getPublishedUrl("test_media_id", "invalid_account");
        assertNull("Should return null when no client available", url);
    }

    @Test
    public void testGetStats() {
        // Mock client and stats
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient("test_account");
        
        Stats mockStats = new Stats();
        mockStats.setVisitors(100);
        mockStats.setPlays(250);
        mockStats.setAveragePercentWatched(75);
        mockStats.setPageLoads(150);
        mockStats.setPercentOfVisitorsClickingPlay(60);
        
        when(mockWistiaClient.getMediaStats("test_media_id")).thenReturn(mockStats);
        
        Map<String, String> stats = spyService.getStats("test_media_id", "test_account");
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("100", stats.get("label.mediaPublishing.stats.visitors"));
        assertEquals("250", stats.get("label.mediaPublishing.stats.plays"));
        assertEquals("75", stats.get("label.mediaPublishing.stats.averagePercentWatched"));
        assertEquals("150", stats.get("label.mediaPublishing.stats.pageLoads"));
        assertEquals("60", stats.get("label.mediaPublishing.stats.percentOfVisitorsClickingPlay"));
    }

    @Test
    public void testIsMediaPublished() {
        // Mock client and media
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient("test_account");
        
        Media mockMedia = new Media();
        when(mockWistiaClient.getMedia("test_media_id")).thenReturn(mockMedia);
        
        boolean isPublished = spyService.isMediaPublished("test_media_id", "test_account");
        assertTrue("Media should be considered published", isPublished);
    }

    @Test
    public void testGetProjects() {
        // Mock client and projects
        WistiaService spyService = spy(wistiaService);
        doReturn(mockWistiaClient).when(spyService).getWistiaClient("test_account");
        
        Project project1 = new Project();
        project1.setId(1);
        project1.setName("Test Project 1");
        
        Project project2 = new Project();
        project2.setId(2);
        project2.setName("Test Project 2");
        
        List<Project> mockProjects = List.of(project1, project2);
        when(mockWistiaClient.getProjects()).thenReturn(mockProjects);
        
        List<Project> projects = spyService.getProjects("test_account");
        
        assertNotNull("Projects should not be null", projects);
        assertEquals("Should return 2 projects", 2, projects.size());
        assertEquals("Test Project 1", projects.get(0).getName());
        assertEquals("Test Project 2", projects.get(1).getName());
    }
}