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

package org.nuxeo.ecm.media.publishing.mock;

import java.io.IOException;
import java.util.Map;

import org.nuxeo.ecm.media.publishing.MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;

public class MockMediaProvider implements MediaPublishingProvider {

    public static final String UPLOAD_ID = "123";

    public MockMediaProvider(String providerId) {
    }

    @Override
    public String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account,
            Map<String, String> options) throws IOException {
        return UPLOAD_ID;
    }

    @Override
    public boolean unpublish(PublishableMedia media) throws IOException {
        return true;
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        return null;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        return null;
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean isMediaAvailable(PublishableMedia media) {
        return false;
    }

    @Override
    public boolean isMediaPublished(String mediaId, String account) {
        return false;
    }
}
