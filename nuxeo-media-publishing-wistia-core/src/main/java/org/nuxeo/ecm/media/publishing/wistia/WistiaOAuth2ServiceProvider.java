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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.oauth2.providers.AbstractOAuth2UserEmailProvider;

import java.io.IOException;

/**
 * @since 7.3
 */
public class WistiaOAuth2ServiceProvider extends AbstractOAuth2UserEmailProvider {

    private static final Log log = LogFactory.getLog(WistiaOAuth2ServiceProvider.class);

    private static final String ACCOUNT_INFO_URL = "https://api.wistia.com/v1/account.json";

    private static final HttpRequestFactory requestFactory =
        HTTP_TRANSPORT.createRequestFactory(request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));

    @Override
    protected String getUserEmail(String accessToken) throws IOException {
        log.debug("Fetching user email from Wistia account info");
        GenericUrl url = new GenericUrl(ACCOUNT_INFO_URL);
        
        HttpRequest request = requestFactory.buildGetRequest(url);

        HttpHeaders headers = request.getHeaders();
        headers.setAuthorization("Bearer " + accessToken);

        log.debug("Making request to Wistia account API: " + ACCOUNT_INFO_URL);
        HttpResponse response = request.setHeaders(headers).execute();
        
        log.debug("Received response from Wistia account API with status: " + response.getStatusCode());
        GenericJson json = response.parseAs(GenericJson.class);
        
        String userEmail = (String) json.get("name");
        log.info("Successfully retrieved user email from Wistia account: " + userEmail);
        
        return userEmail;
    }
}
