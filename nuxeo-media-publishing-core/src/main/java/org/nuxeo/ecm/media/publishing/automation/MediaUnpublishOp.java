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
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.media.publishing.MediaPublishingService;

@Operation(
        id = MediaUnpublishOp.ID,
        category = Constants.CAT_SERVICES,
        label = "Unpublish Media",
        description = "Unpublish a Media File from an external service")
public class MediaUnpublishOp {

    public static final String ID = "MediaUnpublish";

    @Context
    protected CoreSession session;

    @Context
    protected MediaPublishingService mediaPublishingService;

    @Param(name = "service", description = "External service ID")
    protected String service;

    @OperationMethod
    public DocumentModel run(DocumentModel doc)  {
        mediaPublishingService.unpublish(doc, service);
        return doc;
    }
}
