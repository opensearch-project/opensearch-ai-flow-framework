/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.flowframework;

import com.google.common.collect.ImmutableList;
import org.opensearch.action.ActionRequest;
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.core.common.io.stream.NamedWriteableRegistry;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.env.Environment;
import org.opensearch.env.NodeEnvironment;
import org.opensearch.flowframework.rest.RestCreateWorkflowAction;
import org.opensearch.flowframework.rest.RestProvisionWorkflowAction;
import org.opensearch.flowframework.transport.CreateWorkflowAction;
import org.opensearch.flowframework.transport.CreateWorkflowTransportAction;
import org.opensearch.flowframework.transport.ProvisionWorkflowAction;
import org.opensearch.flowframework.transport.ProvisionWorkflowTransportAction;
import org.opensearch.flowframework.workflow.WorkflowProcessSorter;
import org.opensearch.flowframework.workflow.WorkflowStepFactory;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.repositories.RepositoriesService;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.script.ScriptService;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.watcher.ResourceWatcherService;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * An OpenSearch plugin that enables builders to innovate AI apps on OpenSearch.
 */
public class FlowFrameworkPlugin extends Plugin implements ActionPlugin {

    /**
     * The base URI for this plugin's rest actions
     */
    public static final String AI_FLOW_FRAMEWORK_BASE_URI = "/_plugins/_ai_flow";
    /**
     * The URI for this plugin's workflow rest actions
     */
    public static final String WORKFLOWS_URI = AI_FLOW_FRAMEWORK_BASE_URI + "/workflows";

    @Override
    public Collection<Object> createComponents(
        Client client,
        ClusterService clusterService,
        ThreadPool threadPool,
        ResourceWatcherService resourceWatcherService,
        ScriptService scriptService,
        NamedXContentRegistry xContentRegistry,
        Environment environment,
        NodeEnvironment nodeEnvironment,
        NamedWriteableRegistry namedWriteableRegistry,
        IndexNameExpressionResolver indexNameExpressionResolver,
        Supplier<RepositoriesService> repositoriesServiceSupplier
    ) {
        WorkflowStepFactory workflowStepFactory = WorkflowStepFactory.create(client);
        WorkflowProcessSorter workflowProcessSorter = WorkflowProcessSorter.create(workflowStepFactory, threadPool.generic());

        return ImmutableList.of(workflowStepFactory, workflowProcessSorter);
    }

    @Override
    public List<RestHandler> getRestHandlers(
        Settings settings,
        RestController restController,
        ClusterSettings clusterSettings,
        IndexScopedSettings indexScopedSettings,
        SettingsFilter settingsFilter,
        IndexNameExpressionResolver indexNameExpressionResolver,
        Supplier<DiscoveryNodes> nodesInCluster
    ) {
        return ImmutableList.of(new RestCreateWorkflowAction(), new RestProvisionWorkflowAction());
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return ImmutableList.of(
            new ActionHandler<>(CreateWorkflowAction.INSTANCE, CreateWorkflowTransportAction.class),
            new ActionHandler<>(ProvisionWorkflowAction.INSTANCE, ProvisionWorkflowTransportAction.class)
        );
    }

}
