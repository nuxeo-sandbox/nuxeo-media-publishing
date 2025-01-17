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
 *      Nelson Silva
 */

package org.nuxeo.ecm.media.publishing.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.video.TranscodedVideo;

import java.util.List;
import java.util.Map;

/**
 * @since 7.3
 */
public interface PublishableMedia {

    String getMediaType();

    Map<String, Object> getProvider(String provider);

    void putProvider(Map<String, Object> provider);

    void removeProvider(String provider);

    boolean isPublishedByProvider(String provider);

    List<Map<String, Object>> getProviders();

    void setProviders(List<Map<String, Object>> providers);

    String getId(String provider);

    String getAccount(String provider);

    void setId(String id);

    String getTitle();

    String getDescription();

    Blob getBlob();

    boolean isPicture();

    PictureView getPictureView(String viewName);

    List<PictureView> getAllViews();

    boolean isVideo();

    public List<TranscodedVideo> getTranscodedVideos();

    public TranscodedVideo getTranscodedVideo(String name);

    String getUrl(String provider);

    String getEmbedCode(String provider);

    Map<String, String> getStats(String provider);
}
