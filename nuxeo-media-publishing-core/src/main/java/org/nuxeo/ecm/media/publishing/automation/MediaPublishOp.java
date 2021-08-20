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

package org.nuxeo.ecm.media.publishing.automation;


import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.media.publishing.MediaPublishingService;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;

@Operation(
        id = MediaPublishOp.ID,
        category = Constants.CAT_SERVICES,
        label = "Publish Media",
        description = "Publish a Media File to an external service")
public class MediaPublishOp {

    public static final String ID = "MediaPublish";

    @Context
    protected CoreSession session;

    @Context
    protected MediaPublishingService mediaPublishingService;

    @Param(name = "xpath", required = false, description = "File xpath")
    protected String xpath="file:content";

    @Param(name = "service", description = "External service ID")
    protected String service;

    @Param(name = "serviceLogin", description = "External service ID")
    protected String serviceLogin;


    @Param(name = "options", required = false, description = "Publishing options")
    protected Properties options = new Properties();

    @OperationMethod
    public DocumentModel run(DocumentModel doc)  {
        mediaPublishingService.publish(doc, service, serviceLogin, options, new MediaPublishingProgressListener() {
            @Override
            public void onStart() {
                //todo
            }

            @Override
            public void onProgress(double progress) {
                //todo
            }

            @Override
            public void onComplete() {
                //todo
            }

            @Override
            public void onError() {
                //todo
            }
        });
        return doc;
    }
}
