/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.metadata.store.etcd;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.metadata.report.identifier.MetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.ServiceMetadataIdentifier;
import org.apache.dubbo.metadata.report.identifier.SubscriberMetadataIdentifier;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReport;
import org.apache.dubbo.remoting.etcd.jetcd.JEtcdClient;

import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_SEPARATOR;

/**
 * Report Metadata to Etcd
 */
public class EtcdMetadataReport extends AbstractMetadataReport {

    private final static Logger logger = LoggerFactory.getLogger(EtcdMetadataReport.class);

    private final String root;

    /**
     * The etcd client
     */
    private final JEtcdClient etcdClient;

    public EtcdMetadataReport(URL url) {
        super(url);
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }
        String group = url.getParameter(GROUP_KEY, DEFAULT_ROOT);
        if (!group.startsWith(PATH_SEPARATOR)) {
            group = PATH_SEPARATOR + group;
        }
        this.root = group;
        etcdClient = new JEtcdClient(url);
    }

    @Override
    protected void doStoreProviderMetadata(MetadataIdentifier providerMetadataIdentifier, String serviceDefinitions) {
        storeMetadata(providerMetadataIdentifier, serviceDefinitions);
    }

    @Override
    protected void doStoreConsumerMetadata(MetadataIdentifier consumerMetadataIdentifier, String value) {
        storeMetadata(consumerMetadataIdentifier, value);
    }

    @Override
    protected void doSaveMetadata(ServiceMetadataIdentifier serviceMetadataIdentifier, URL url) {
        throw new UnsupportedOperationException("This extension does not support working as a remote metadata center.");
    }

    @Override
    protected void doRemoveMetadata(ServiceMetadataIdentifier serviceMetadataIdentifier) {
        throw new UnsupportedOperationException("This extension does not support working as a remote metadata center.");
    }

    @Override
    protected List<String> doGetExportedURLs(ServiceMetadataIdentifier metadataIdentifier) {
        throw new UnsupportedOperationException("This extension does not support working as a remote metadata center.");
    }

    @Override
    protected void doSaveSubscriberData(SubscriberMetadataIdentifier subscriberMetadataIdentifier, String urlListStr) {

    }

    @Override
    protected String doGetSubscribedURLs(SubscriberMetadataIdentifier subscriberMetadataIdentifier) {
        throw new UnsupportedOperationException("This extension does not support working as a remote metadata center.");
    }

    @Override
    public String getServiceDefinition(MetadataIdentifier consumerMetadataIdentifier) {
        throw new UnsupportedOperationException("This extension does not support working as a remote metadata center.");
    }

    private void storeMetadata(MetadataIdentifier identifier, String v) {
        String key = getNodeKey(identifier);
        if (!etcdClient.put(key, v)) {
            logger.error("Failed to put " + identifier + " to etcd, value: " + v);
        }
    }

    String getNodeKey(MetadataIdentifier identifier) {
        return toRootDir() + identifier.getUniqueKey(MetadataIdentifier.KeyTypeEnum.PATH);
    }

    String toRootDir() {
        if (root.equals(PATH_SEPARATOR)) {
            return root;
        }
        return root + PATH_SEPARATOR;
    }
}