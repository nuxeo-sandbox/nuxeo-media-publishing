package org.nuxeo.ecm.media.publishing.facebook;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import org.nuxeo.ecm.platform.oauth2.providers.AbstractOAuth2UserEmailProvider;

import java.io.IOException;

public class FacebookOAuth2Provider extends AbstractOAuth2UserEmailProvider {

    private static final String USER_INFO_URL = "https://graph.facebook.com/me";

    private static final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(
            request -> request.setParser(new JsonObjectParser(JSON_FACTORY)));

    @Override
    protected String getUserEmail(String accessToken) throws IOException {
        GenericUrl url = new GenericUrl(USER_INFO_URL);
        url.set("access_token", accessToken);
        url.set("fields","id,name,email");
        HttpResponse response = requestFactory.buildGetRequest(url).execute();
        GenericJson json = response.parseAs(GenericJson.class);
        return (String) json.get("email");
    }
}