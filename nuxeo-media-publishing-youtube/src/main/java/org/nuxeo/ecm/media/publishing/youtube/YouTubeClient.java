/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      Nelson Silva
 */

package org.nuxeo.ecm.media.publishing.youtube;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Videos.Insert;
import com.google.api.services.youtube.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Client for the YouTube API
 *
 * @since 7.3
 */
public class YouTubeClient {
    private static final Log log = LogFactory.getLog(YouTubeClient.class);

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    YouTube youtube;

    Credential credential;

    public YouTubeClient(Credential credential) {
        this.credential = credential;
    }

    protected OAuth2ServiceProviderRegistry getOAuth2ServiceProviderRegistry() {
        return Framework.getLocalService(OAuth2ServiceProviderRegistry.class);
    }

    public boolean isAuthorized() {
        return (credential != null && credential.getAccessToken() != null);
    }

    public YouTube getYouTube() throws IOException {

        // if credential found with an access token, invoke the user code
        if (youtube == null) {
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                credential).setApplicationName("nuxeo-media-publishing").build();
        }
        return youtube;
    }

    public List<Video> getVideos() throws IOException {
        return getYouTube().videos().list("snippet").execute().getItems();
    }

    public List<Channel> getChannels() throws IOException {
        return getYouTube().channels().list("snippet").setMine(true).execute().getItems();
    }

    public List<PlaylistItem> getVideos(Channel channel) throws IOException {
        String id = channel.getId();
        return getYouTube().playlistItems().list("snippet").setPlaylistId(id).execute().getItems();
    }

    public Video upload(Video video, InputStream stream, String type, long length,
        final MediaHttpUploaderProgressListener uploadListener) throws IOException {

        InputStreamContent mediaContent = new InputStreamContent(type, stream);
        mediaContent.setLength(length);

        Insert insert = getYouTube().videos().insert("snippet,status", video, mediaContent);

        // Set the upload type and add event listener.
        MediaHttpUploader uploader = insert.getMediaHttpUploader();

        /*
         * Sets whether direct media upload is enabled or disabled. True = whole
         * media content is uploaded in a single request. False (default) =
         * resumable media upload protocol to upload in data chunks.
         */
        uploader.setDirectUploadEnabled(false);

        uploader.setProgressListener(uploadListener);

        // Execute upload.
        Video returnedVideo = insert.execute();

        // Print out returned results.
        if (returnedVideo != null) {
            log.info("\n================== Returned Video ==================\n");
            log.info("  - Id: " + returnedVideo.getId());
            log.info("  - Title: " + returnedVideo.getSnippet().getTitle());
            log.info("  - Tags: " + returnedVideo.getSnippet().getTags());
            log.info("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
        }

        return returnedVideo;

    }

    public void setPrivacyStatus(String videoId, String privacyStatus) throws IOException {
        Video youtubeVideo = new Video();
        youtubeVideo.setId(videoId);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(privacyStatus);
        youtubeVideo.setStatus(status);

        getYouTube().videos().update("status", youtubeVideo).execute();
    }

    public void delete(String videoId) throws IOException {
        getYouTube().videos().delete(videoId).execute();
    }

    public VideoStatistics getStatistics(String videoId) throws IOException {
        VideoListResponse list = getYouTube().videos().list("statistics").setId(videoId).execute();

        if (list.isEmpty() || list.getItems().size() == 0) {
            return null;
        }

        Video video = list.getItems().get(0);
        return video.getStatistics();
    }
}
