package org.nuxeo.ecm.media.publishing.facebook;

import com.google.api.client.auth.oauth2.Credential;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.media.publishing.OAuth2MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;

import java.util.Map;

public class FacebookProvider extends OAuth2MediaPublishingProvider {

    public FacebookProvider(String providerName) {
        super(providerName);
    }

    public FacebookClient getClient(String account) {
        Credential credential = getCredential(account);
        String accessToken = credential.getAccessToken();
        return new FacebookClient(accessToken);
    }

    @Override
    public String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account,
            Map<String, String> options) {
        FacebookClient client = getClient(account);

        String renditionName = options.get("renditionName");
        if (media.isPicture()) {
            Blob blob = StringUtils.isNotEmpty(renditionName) ? media.getPictureView(renditionName).getBlob() : media.getBlob();
            return client.uploadPhoto(blob, options);
        } else if (media.isVideo()) {
            Blob blob = StringUtils.isNotEmpty(renditionName) ? media.getTranscodedVideo(renditionName).getBlob() : media.getBlob();
            return client.uploadVideo(blob, options);
        } else {
            throw new NuxeoException("Unsupported media type");
        }
    }

    @Override
    public boolean unpublish(PublishableMedia media) {
        String account = media.getAccount(this.providerName);
        String mediaId = media.getId(this.providerName);
        FacebookClient client = getClient(account);
        if (isMediaPublished(mediaId, account)) {
            client.deletePost(mediaId);
        }
        return true;
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        FacebookClient client = getClient(account);
        return client.getPostPermalink(mediaId);
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
    public boolean isMediaPublished(String mediaId, String account) {
        return StringUtils.isNotEmpty(getPublishedUrl(mediaId,account));
    }

}