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
 *      André Justo
 */

package org.nuxeo.ecm.media.publishing.wistia;

import com.google.api.client.auth.oauth2.Credential;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.OAuth2MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wistia Media Publishing Provider Service
 *
 * @since 7.3
 */
public class WistiaService extends OAuth2MediaPublishingProvider {

    private static final Log log = LogFactory.getLog(WistiaService.class);

    public WistiaService(String providerName) {
        super(providerName);
    }

    public WistiaClient getWistiaClient(String account) {
        log.debug("Getting Wistia client for account: " + account);
        Credential credential = getCredential(account);
        if (credential == null) {
            log.warn("No credential found for account: " + account);
            return null;
        }
        try {
            // Refresh access token if needed (based on com.google.api.client.auth.oauth.Credential.intercept())
            // TODO: rely on Google Oauth aware client instead
            Long expiresIn = credential.getExpiresInSeconds();
            // check if token will expire in a minute
            if (credential.getAccessToken() == null || expiresIn != null && expiresIn <= 60) {
                log.info("Refreshing access token for account: " + account + " (expires in: " + expiresIn + " seconds)");
                credential.refreshToken();
                if (credential.getAccessToken() == null) {
                    // nothing we can do without an access token
                    log.error("Failed to refresh access token for account: " + account);
                    throw new NuxeoException("Failed to refresh access token");
                }
                log.info("Successfully refreshed access token for account: " + account);
            }
        } catch (IOException e) {
            log.error("Error refreshing access token for account: " + account, e);
            throw new NuxeoException(e.getMessage(), e);
        }

        log.debug("Successfully created Wistia client for account: " + account);
        return new WistiaClient(credential.getAccessToken());
    }

    @Override
    public String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account, Map<String, String> options) throws IOException {
        log.info("Starting upload for media: " + media.getTitle() + " to account: " + account);
        log.debug("Upload options: " + options);

        String projectId = options.get("project_id");
        if (projectId == null || projectId.isEmpty()) {
            log.warn("No project ID provided, using default project.");
            List<Project> projects = getProjects(account);
            if (projects.isEmpty()) {
                log.error("No projects found for account: " + account);
                throw new NuxeoException("No projects available for account: " + account);
            }
            projectId = String.valueOf(projects.get(0).getId());
        }
        
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        for (Entry<String, String> entry : options.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length() > 0) {
                params.putSingle(entry.getKey(), entry.getValue());
            }
        }

        // upload original video
        Blob blob = media.getBlob();
        log.info("Uploading blob: " + blob.getFilename() + " (size: " + blob.getLength() + " bytes)");

        Media video = getWistiaClient(account).upload(blob.getFilename(), blob.getStream(), params);
        
        log.info("Successfully uploaded media. Wistia ID: " + video.getHashedId());
        return video.getHashedId();
    }

    @Override
    public boolean unpublish(PublishableMedia media) {
        String account = media.getAccount(this.providerName);
        String mediaId = media.getId(this.providerName);
        log.info("Unpublishing media ID: " + mediaId + " from account: " + account);
        
        boolean result = getWistiaClient(account).deleteMedia(mediaId) != null;
        
        if (result) {
            log.info("Successfully unpublished media ID: " + mediaId);
        } else {
            log.warn("Failed to unpublish media ID: " + mediaId);
        }
        
        return result;
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        log.debug("Getting published URL for media ID: " + mediaId + " from account: " + account);
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            log.warn("Cannot get published URL - no client available for account: " + account);
            return null;
        }
        String url = client.getAccount().getUrl() + "/medias/" + mediaId;
        log.debug("Published URL: " + url);
        return url;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        log.debug("Getting embed code for media ID: " + mediaId + " from account: " + account);
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            log.warn("Cannot get embed code - no client available for account: " + account);
            return null;
        }
        String embedCode = client.getEmbedCode(getPublishedUrl(mediaId, account));
        log.debug("Successfully retrieved embed code for media ID: " + mediaId);
        return embedCode;
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        log.debug("Getting stats for media ID: " + mediaId + " from account: " + account);
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            log.warn("Cannot get stats - no client available for account: " + account);
            return null;
        }

        Stats stats = client.getMediaStats(mediaId);
        if (stats == null) {
            log.warn("No stats available for media ID: " + mediaId);
            return null;
        }

        log.debug("Retrieved stats for media ID: " + mediaId + " - visitors: " + stats.getVisitors() + ", plays: " + stats.getPlays());
        Map<String, String> map = new HashMap<>();
        map.put("label.mediaPublishing.stats.visitors", Integer.toString(stats.getVisitors()));
        map.put("label.mediaPublishing.stats.plays", Integer.toString(stats.getPlays()));
        map.put("label.mediaPublishing.stats.averagePercentWatched", Integer.toString(stats.getAveragePercentWatched()));
        map.put("label.mediaPublishing.stats.pageLoads", Integer.toString(stats.getPageLoads()));
        map.put("label.mediaPublishing.stats.percentOfVisitorsClickingPlay", Integer.toString(stats.getPercentOfVisitorsClickingPlay()));
        return map;
    }

    @Override
    public boolean isMediaPublished(String mediaId, String account) {
        log.debug("Checking if media is published - ID: " + mediaId + " account: " + account);
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            log.warn("Cannot check publication status - no client available for account: " + account);
            return false;
        }

        Media media = client.getMedia(mediaId);
        boolean isPublished = media != null;
        log.debug("Media " + mediaId + " publication status: " + isPublished);
        return isPublished;
    }

    public List<Project> getProjects(String account) {
        log.debug("Getting projects for account: " + account);
        WistiaClient client = getWistiaClient(account);
        if (client == null) {
            log.warn("Cannot get projects - no client available for account: " + account);
            return Collections.emptyList();
        }
        List<Project> projects = client.getProjects();
        log.info("Retrieved " + projects.size() + " projects for account: " + account);
        return projects;
    }
}
