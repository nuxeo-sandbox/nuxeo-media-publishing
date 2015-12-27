/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *      André Justo
 */
package org.nuxeo.ecm.media.publishing.youtube;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import org.nuxeo.ecm.platform.oauth2.providers.AbstractOAuth2UserEmailProvider;

import java.io.IOException;

/**
 * @since 7.3
 */
public class YoutubeOAuth2ServiceProvider extends AbstractOAuth2UserEmailProvider {

    private static final String TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";

    private static final HttpRequestFactory requestFactory =
        HTTP_TRANSPORT.createRequestFactory(request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));

    @Override
    protected String getUserEmail(String accessToken) throws IOException {
        GenericUrl url = new GenericUrl(TOKEN_INFO_URL);
        url.set("access_token", accessToken);

        HttpResponse response = requestFactory.buildGetRequest(url).execute();
        GenericJson json = response.parseAs(GenericJson.class);
        return (String) json.get("email");
    }
}
