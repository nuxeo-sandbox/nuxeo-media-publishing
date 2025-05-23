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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;
import org.nuxeo.ecm.media.publishing.wistia.model.Account;
import org.nuxeo.ecm.media.publishing.wistia.model.Media;
import org.nuxeo.ecm.media.publishing.wistia.model.Project;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;
import org.nuxeo.ecm.media.publishing.wistia.rest.RestRequest;
import org.nuxeo.ecm.media.publishing.wistia.rest.RestResponse;
import org.nuxeo.ecm.media.publishing.wistia.rest.WistiaResponseParser;
import org.nuxeo.ecm.media.publishing.wistia.rest.RequestType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class WistiaClient {

    private static final String BASE_URL = "https://api.wistia.com/v1";

    private static final String BASE_UPLOAD_URL = "https://upload.wistia.com";

    private static final String BASE_EMBED_URL = "http://fast.wistia.net";

    protected final String apiToken;

    protected final WebResource dataService;

    protected final WebResource uploadService;

    protected final WebResource embedService;

    public WistiaClient(String apiToken) {
        this.apiToken = apiToken;
        dataService = new Client().resource(BASE_URL);
        uploadService = new Client().resource(BASE_UPLOAD_URL);
        embedService = new Client().resource(BASE_EMBED_URL);
    }

    /**
     * Obtains a list of all the media in an account.
     * 
     * @return list of media
     */
    public List<Media> getMedias() {
        var response = new RestRequest(dataService, "medias.json")
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asMediaList(response.getClientResponse());
    }

    /**
     * Gets information about a specific piece of media uploaded to an account.
     * 
     * @param hashedId the media ID
     * @return the media information
     */
    public Media getMedia(String hashedId) {
        var response = new RestRequest(dataService, "medias/" + hashedId + ".json")
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asMedia(response.getClientResponse());
    }

    /**
     * Updates attributes on a piece of media.
     * 
     * @param hashedId the media ID
     * @param queryParams parameters to update
     * @return updated media information
     */
    public Media updateMedia(String hashedId, MultivaluedMap<String, String> queryParams) {
        var response = new RestRequest(dataService, "medias/" + hashedId + ".json")
                .requestType(RequestType.PUT)
                .header("Authorization", "Bearer " + apiToken)
                .queryParams(queryParams)
                .execute();

        return WistiaResponseParser.asMedia(response.getClientResponse());
    }

    /**
     * Deletes a media from an account.
     * 
     * @param hashedId the media ID to delete
     * @return deleted media information
     */
    public Media deleteMedia(String hashedId) {
        var response = new RestRequest(dataService, "medias/" + hashedId + ".json")
                .requestType(RequestType.DELETE)
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asMedia(response.getClientResponse());
    }

    /**
     * Aggregates tracking statistics for a video that has been embedded in a
     * website.
     * 
     * @param hashedId the media ID
     * @return statistics for the media
     */
    public Stats getMediaStats(String hashedId) {
        var response = new RestRequest(dataService, "medias/" + hashedId + "/stats.json")
                .requestType(RequestType.GET)
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asMedia(response.getClientResponse()).getStats();
    }

    /**
     * Gets information about an account.
     * 
     * @return the account information
     */
    public Account getAccount() {
        var response = new RestRequest(dataService, "account.json")
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asAccount(response.getClientResponse());
    }

    /**
     * Obtains a list of all the projects in an account.
     * 
     * @return list of projects
     */
    public List<Project> getProjects() {
        var response = new RestRequest(dataService, "projects.json")
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asProjectList(response.getClientResponse());
    }

    /**
     * Gets information about a specific project.
     * 
     * @param hashedId the project ID
     * @return project information
     */
    public Project getProject(String hashedId) {
        var response = new RestRequest(dataService, "projects/" + hashedId + ".json")
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asProject(response.getClientResponse());
    }

    /**
     * Creates a new project.
     * 
     * @param name project name
     * @param queryParams additional parameters
     * @return the created project
     */
    public Project createProject(String name, MultivaluedMap<String, String> queryParams) {
        var response = new RestRequest(dataService, "projects.json")
                .requestType(RequestType.POST)
                .header("Authorization", "Bearer " + apiToken)
                .queryParams(queryParams)
                .queryParam("name", name)
                .execute();

        return WistiaResponseParser.asProject(response.getClientResponse());
    }

    /**
     * Updates attributes on a project.
     * 
     * @param hashedId the project ID
     * @param queryParams parameters to update
     * @return updated project information
     */
    public Project updateProject(String hashedId, MultivaluedMap<String, String> queryParams) {
        var response = new RestRequest(dataService, "projects/" + hashedId + ".json")
                .requestType(RequestType.PUT)
                .header("Authorization", "Bearer " + apiToken)
                .queryParams(queryParams)
                .execute();

        return WistiaResponseParser.asProject(response.getClientResponse());
    }

    /**
     * Deletes a project from an account.
     * 
     * @param hashedId the project ID to delete
     * @return deleted project information
     */
    public Project deleteProject(String hashedId) {
        var response = new RestRequest(dataService, "projects/" + hashedId + ".json")
                .requestType(RequestType.DELETE)
                .header("Authorization", "Bearer " + apiToken)
                .execute();

        return WistiaResponseParser.asProject(response.getClientResponse());
    }

    /**
     * Uploads a file from URL.
     * 
     * @param url file URL
     * @param queryParams additional parameters
     * @return uploaded media information
     */
    public Media upload(String url, MultivaluedMap<String, String> queryParams) {
        var response = new RestRequest(uploadService, "")
                .requestType(RequestType.POST)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + apiToken)
                .queryParams(queryParams)
                .queryParam("url", url)
                .execute();

        return WistiaResponseParser.asMedia(response.getClientResponse());
    }

    /**
     * Uploads a file.
     * 
     * @param file the file to upload
     * @param queryParams additional parameters
     * @return uploaded media information
     */
    public Media upload(File file, MultivaluedMap<String, String> queryParams) {
        var bodyPart = new FileDataBodyPart(file.getName(),
                file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return upload(bodyPart, queryParams);
    }

    /**
     * Uploads a file.
     * 
     * @param filename the name of the file
     * @param stream input stream of file data
     * @param queryParams additional parameters
     * @return uploaded media information
     */
    public Media upload(String filename, InputStream stream, MultivaluedMap<String, String> queryParams) {
        var bodyPart = new StreamDataBodyPart(filename,
                stream, filename, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        return upload(bodyPart, queryParams);
    }

    private Media upload(BodyPart bodyPart, MultivaluedMap<String, String> queryParams) {
        var multiPart = new MultiPart();
        multiPart.bodyPart(bodyPart);

        var response = new RestRequest(uploadService, "")
                .requestType(RequestType.POST)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + apiToken)
                .queryParams(queryParams)
                .execute(multiPart);

        return WistiaResponseParser.asMedia(response.getClientResponse());
    }

    /**
     * Gets embed code for media URL.
     * 
     * @param mediaUrl the media URL
     * @return the embed HTML code or null if not available
     */
    public String getEmbedCode(String mediaUrl) {
        var response = new RestRequest(embedService, "oembed")
                .queryParam("url", mediaUrl)
                .execute();

        if (response.getStatus() == 200) {
            return response.asJson().get("html").textValue();
        }

        return null;
    }
}
