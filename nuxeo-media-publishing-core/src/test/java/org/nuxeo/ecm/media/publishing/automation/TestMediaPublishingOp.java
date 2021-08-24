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

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.media.publishing.MediaPublishingConstants;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.tag", "org.nuxeo.ecm.platform.video", "nuxeo-media-publishing-core",
        "nuxeo-media-publishing-core:mock-provider-contrib.xml" })
public class TestMediaPublishingOp {

    @Inject
    CoreSession session;

    @Inject
    AutomationService as;

    @Test
    public void testPublishOp() throws Exception {
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet(MediaPublishingConstants.PUBLISHABLE_MEDIA_FACET);
        doc = session.createDocument(doc);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestPublish");
        chain.add(MediaPublishOp.ID).set("service", "mock").set("serviceLogin", "Administrator");
        doc = (DocumentModel) as.run(ctx, chain);
        assertNotNull(doc);
    }

    @Test
    public void testUnpublishOp() throws Exception {
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet(MediaPublishingConstants.PUBLISHABLE_MEDIA_FACET);
        doc = session.createDocument(doc);
        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestUnpublish");
        chain.add(MediaUnpublishOp.ID).set("service", "mock");
        doc = (DocumentModel) as.run(ctx, chain);
        assertNotNull(doc);
    }

}
