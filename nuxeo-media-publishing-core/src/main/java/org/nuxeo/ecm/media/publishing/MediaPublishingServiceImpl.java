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

package org.nuxeo.ecm.media.publishing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.versioning.VersioningService;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @since 7.3
 */
public class MediaPublishingServiceImpl extends DefaultComponent implements MediaPublishingService {

    public static final String PROVIDER_EP = "providers";
    protected static final Log log = LogFactory.getLog(MediaPublishingServiceImpl.class);
    protected Map<String, MediaPublishingProvider> providers = new HashMap<>();

    @Override
    public List<String> getAvailableProviders() {
        return new ArrayList<>(providers.keySet());
    }

    public MediaPublishingProvider getProvider(String provider) {
        return providers.get(provider);
    }

    @Override
    public String publish(DocumentModel doc, String providerId, String account, Map<String, String> options,
            MediaPublishingProgressListener listener) {
        PublishableMedia media = doc.getAdapter(PublishableMedia.class);
        MediaPublishingProvider provider = Framework.getService(MediaPublishingService.class).getProvider(providerId);
        try {
            String mediaId = provider.upload(media, listener, account, options);
            Map<String, Object> entry = new HashMap<>();
            entry.put(MediaPublishingConstants.ID_PROPERTY_NAME, mediaId);
            entry.put(MediaPublishingConstants.PROVIDER_PROPERTY_NAME, providerId);
            entry.put(MediaPublishingConstants.ACCOUNT_PROPERTY_NAME, account);
            entry.put(MediaPublishingConstants.PERMALINK_PROPERTY_NAME,provider.getPublishedUrl(mediaId,account));
            media.putProvider(entry);

            CoreSession session = doc.getCoreSession();

            // We don't want to erase the current version
            doc.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.NONE);
            doc.putContextData(VersioningService.DISABLE_AUTO_CHECKOUT, Boolean.TRUE);

            // Track media publication in document history
            DocumentEventContext ctx = new DocumentEventContext(session, session.getPrincipal(), doc);
            ctx.setComment("Published to " + providerId);
            ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);

            EventProducer evtProducer = Framework.getService(EventProducer.class);
            Event event = ctx.newEvent(DocumentEventTypes.DOCUMENT_PUBLISHED);
            evtProducer.fireEvent(event);
            return mediaId;
        } catch (IOException e) {
            log.warn(e);
            throw new NuxeoException(e);
        }
    }

    @Override
    public void unpublish(DocumentModel doc, String providerId) {
        MediaPublishingProvider provider = getProvider(providerId);
        PublishableMedia media = doc.getAdapter(PublishableMedia.class);
        try {
            if (provider.unpublish(media)) {
                // Remove provider from the list of published providers
                media.removeProvider(providerId);

                // Track unpublish in document history
                CoreSession session = doc.getCoreSession();
                DocumentEventContext ctx = new DocumentEventContext(session, session.getPrincipal(), doc);
                ctx.setComment("Video unpublished from " + provider);
                ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
                Event event = ctx.newEvent(DocumentEventTypes.DOCUMENT_PUBLISHED);
                EventProducer evtProducer = Framework.getService(EventProducer.class);
                evtProducer.fireEvent(event);
            }
        } catch (IOException e) {
            log.warn(e);
            throw new NuxeoException("Failed to unpublish media", e);
        }
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (PROVIDER_EP.equals(extensionPoint)) {
            MediaPublishingProviderDescriptor provider = (MediaPublishingProviderDescriptor) contribution;
            try {
                providers.put(provider.getId(),
                        (MediaPublishingProvider) provider.getService()
                                                          .getConstructor(String.class)
                                                          .newInstance(provider.getId()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new NuxeoException(e);
            }
        }
    }

}
