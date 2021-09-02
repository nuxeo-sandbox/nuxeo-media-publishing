/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
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
 *     Michael Vachette
 */

package org.nuxeo.ecm.media.publishing.facebook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.media.publishing.MediaPublishingService;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.ID_PROPERTY_NAME;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.PERMALINK_PROPERTY_NAME;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.PUBLISHABLE_MEDIA_FACET;
import static org.nuxeo.ecm.media.publishing.facebook.FacebookClient.DESCRIPTION_KEY;
import static org.nuxeo.ecm.media.publishing.facebook.FacebookClient.PAGE_ID_KEY;
import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.VIDEO_FACET;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.tag", "org.nuxeo.ecm.platform.video","org.nuxeo.ecm.platform.picture.core",  "org.nuxeo.ecm.platform.oauth",
        "nuxeo-media-publishing-core", "nuxeo-media-publishing-facebook-core",
        "nuxeo-media-publishing-facebook-core:facebook-test-config.xml"

})
public class TestFacebookProvider {

    public static final String FACEBOOK_TEST = "facebook-test";

    @Inject
    CoreSession session;

    @Inject
    MediaPublishingService mediaPublishingService;

    @Test
    public void testPublishImage() {
        Blob blob = new FileBlob(new File(getClass().getResource("/files/snow.jpg").getPath()), "image/jpeg");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc.addFacet(PUBLISHABLE_MEDIA_FACET);
        doc.addFacet(PICTURE_FACET);
        doc = session.createDocument(doc);

        Map<String, String> options = new HashMap<>();
        options.put(PAGE_ID_KEY, System.getProperty("facebookTestPageId"));
        options.put(DESCRIPTION_KEY, "Hello Nuxeo!!!");

        String id = mediaPublishingService.publish(doc, FACEBOOK_TEST, session.getPrincipal().getName(), options, null);

        PublishableMedia media = doc.getAdapter(PublishableMedia.class);
        assertTrue(media.isPublishedByProvider(FACEBOOK_TEST));
        Map<String, Object> providerData = media.getProvider(FACEBOOK_TEST);
        assertNotNull(providerData.get(ID_PROPERTY_NAME));
        assertEquals(id,providerData.get(ID_PROPERTY_NAME));
        assertNotNull(providerData.get(PERMALINK_PROPERTY_NAME));
        assertTrue(((String)providerData.get(PERMALINK_PROPERTY_NAME)).startsWith("https://"));
    }

    @Test
    public void testPublishVideo() {
        Blob blob = new FileBlob(new File(getClass().getResource("/files/beach.mp4").getPath()), "video/mp4");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc.addFacet(PUBLISHABLE_MEDIA_FACET);
        doc.addFacet(VIDEO_FACET);
        doc = session.createDocument(doc);

        Map<String, String> options = new HashMap<>();
        options.put(PAGE_ID_KEY, System.getProperty("facebookTestPageId"));
        options.put(DESCRIPTION_KEY, "Hello Nuxeo!!!");

        String id = mediaPublishingService.publish(doc, FACEBOOK_TEST, session.getPrincipal().getName(), options, null);

        PublishableMedia media = doc.getAdapter(PublishableMedia.class);
        assertTrue(media.isPublishedByProvider(FACEBOOK_TEST));
        Map<String, Object> providerData = media.getProvider(FACEBOOK_TEST);
        assertNotNull(providerData.get(ID_PROPERTY_NAME));
        assertEquals(id,providerData.get(ID_PROPERTY_NAME));
        assertNotNull(providerData.get(PERMALINK_PROPERTY_NAME));
        assertTrue(((String)providerData.get(PERMALINK_PROPERTY_NAME)).startsWith("https://"));
    }

}
