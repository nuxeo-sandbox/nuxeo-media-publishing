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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;

public class FacebookClient {

    public static final String PAGE_ID_KEY = "pageId";

    public static final String TITLE_KEY = "title";

    public static final String DESCRIPTION_KEY = "description";

    public static final String PHOTO_UPLOAD_URL = "https://graph.facebook.com/v11.0/%s/photos";

    public static final String VIDEO_UPLOAD_URL = "https://graph-video.facebook.com/v11.0/%s/videos";

    public static final String GET_POST_URL = "https://graph.facebook.com/v11.0/%s";

    public static int timeoutInMs = 10 * 1000;

    protected static CloseableHttpClient httpClient = HttpClients.custom()
                                                                 .setDefaultRequestConfig(
                                                                         RequestConfig.custom()
                                                                                      .setConnectTimeout(timeoutInMs)
                                                                                      .setConnectionRequestTimeout(
                                                                                              timeoutInMs)
                                                                                      .setSocketTimeout(timeoutInMs)
                                                                                      .build())
                                                                 .build();

    protected String accessToken;

    public FacebookClient(String accessToken) {
        this.accessToken = accessToken;
    }

    public JSONArray getAccounts() {
        try {
            URIBuilder builder = new URIBuilder("https://graph.facebook.com/v11.0/me/accounts");
            builder.setParameter("access_token", accessToken);
            HttpGet httpGet = new HttpGet(builder.build().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new NuxeoException("Couldn't get account. "
                            + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }
                JSONObject object = new JSONObject(new JSONTokener(response.getEntity().getContent()));
                return object.getJSONArray("data");
            }
        } catch (URISyntaxException | IOException e) {
            throw new NuxeoException(e);
        }
    }

    public String getPageToken(String pageId) {
        JSONArray data = getAccounts();
        if (data.length() == 0) {
            throw new NuxeoException(String.format("Page %s not found", pageId));
        }

        Optional<JSONObject> page = IntStream.range(0, data.length())
                                             .mapToObj(data::getJSONObject)
                                             .filter(element -> pageId.equals(element.get("id")))
                                             .findFirst();

        if (page.isPresent()) {
            return page.get().getString("access_token");
        } else {
            throw new NuxeoException(String.format("Couldn't find access token for page %s", pageId));
        }
    }

    public String uploadPhoto(Blob blob, Map<String, String> options) {
        String pageId = options.get(PAGE_ID_KEY);
        String pageAccessToken = getPageToken(pageId);

        try {
            URIBuilder builder = new URIBuilder(String.format(PHOTO_UPLOAD_URL, pageId));
            builder.setParameter("access_token", pageAccessToken);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                                  .addBinaryBody("source",
                                                                                          blob.getStream(),
                                                                                          ContentType.create(
                                                                                                  blob.getMimeType()),
                                                                                          blob.getFilename());

            String message = options.get(DESCRIPTION_KEY);
            if (StringUtils.isNotBlank(message)) {
                multipartEntityBuilder.addTextBody("message", message);
            }

            HttpPost httpPost = new HttpPost(builder.build().toString());
            httpPost.setEntity(multipartEntityBuilder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new NuxeoException("Couldn't upload photo. "
                            + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }
                JSONObject json = new JSONObject(new JSONTokener(response.getEntity().getContent()));
                return String.format("%s:%s",pageId,json.getString("post_id"));
            }
        } catch (URISyntaxException | IOException e) {
            throw new NuxeoException(e);
        }
    }

    public String uploadVideo(Blob blob, Map<String, String> options) {
        String pageId = options.get(PAGE_ID_KEY);
        String pageAccessToken = getPageToken(pageId);

        try {
            URIBuilder builder = new URIBuilder(String.format(VIDEO_UPLOAD_URL, pageId));
            builder.setParameter("access_token", pageAccessToken);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                                                                                  .addBinaryBody("source",
                                                                                          blob.getStream(),
                                                                                          ContentType.create(
                                                                                                  blob.getMimeType()),
                                                                                          blob.getFilename());

            String message = options.get(DESCRIPTION_KEY);
            if (StringUtils.isNotBlank(message)) {
                multipartEntityBuilder.addTextBody("description", message);
            }

            HttpPost httpPost = new HttpPost(builder.build().toString());
            httpPost.setEntity(multipartEntityBuilder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new NuxeoException("Couldn't upload video. "
                            + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }
                JSONObject json = new JSONObject(new JSONTokener(response.getEntity().getContent()));
                return String.format("%s:%s",pageId,json.getString("id"));
            }
        } catch (URISyntaxException | IOException e) {
            throw new NuxeoException(e);
        }
    }

    public String getPostPermalink(String postKey) {
        String pageAccessToken = getPageToken(getPageIdFromPostKey(postKey));
        try {
            URIBuilder builder = new URIBuilder(String.format(GET_POST_URL, getPostIdFromPostKey(postKey)));
            builder.setParameter("access_token", pageAccessToken);
            builder.setParameter("fields", "permalink_url");
            HttpGet httpGet = new HttpGet(builder.build().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new NuxeoException("Couldn't get permalink "
                            + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }
                JSONObject object = new JSONObject(new JSONTokener(response.getEntity().getContent()));
                String url = object.getString("permalink_url");
                if (url !=null && url.startsWith("/")) {
                    url = "https://facebook.com" + url;
                }
                return url;
            }
        } catch (URISyntaxException | IOException e) {
            throw new NuxeoException(e);
        }
    }

    public void deletePost(String postKey) {
        String pageAccessToken = getPageToken(getPageIdFromPostKey(postKey));
        try {
            URIBuilder builder = new URIBuilder(String.format(GET_POST_URL, getPostIdFromPostKey(postKey)));
            builder.setParameter("access_token", pageAccessToken);
            HttpDelete httpDelete = new HttpDelete(builder.build().toString());
            try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new NuxeoException("Couldn't delete post "
                            + IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new NuxeoException(e);
        }
    }

    public String getPageIdFromPostKey(String key) {
        String[] parts = key.split(":");
        return parts[0];
    }

    public String getPostIdFromPostKey(String key) {
        String[] parts = key.split(":");
        return parts[1];
    }

}
