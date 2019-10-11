package com.redhat.gdemo;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * DataLoaderConfiguration
 */
@ApplicationScoped
public class DataLoaderConfiguration {

    public static final String KIE_SERVER_REST_ENDPOINT_PARAMATER_KEY = "kie.server.rest.endpoint";
    public static final String KIE_SERVER_USERNAME_PARAMETER_KEY = "kie.server.username";
    public static final String KIE_SERVER_PASSWORD_PARAMETER_KEY = "kie.server.password";
    public static final String CONTAINER_ID_PARAMETER_KEY = "dmn.container.id";
    public static final String MODEL_NAMESPACE_PARAMETER_KEY = "dmn.model.namespace";
    public static final String MODEL_NAME_PARAMETER_KEY = "dmn.model.name";
    public static final String NR_OF_ENTRIES_PARAMETER_KEY = "dataloader.request.count";
    public static final String DATALOADER_THREADCOUNT_PARAMETER_KEY = "dataloader.threadcount";

    @ConfigProperty(name = KIE_SERVER_REST_ENDPOINT_PARAMATER_KEY)
    private String kieServerRestEndpoint;
    @ConfigProperty(name = KIE_SERVER_USERNAME_PARAMETER_KEY)
    private String kieServerUsername;
    @ConfigProperty(name = KIE_SERVER_PASSWORD_PARAMETER_KEY)
    private String kieServerPassword;
    @ConfigProperty(name = CONTAINER_ID_PARAMETER_KEY)
    private String containerId;
    @ConfigProperty(name = MODEL_NAMESPACE_PARAMETER_KEY)
    private String modelNamespace;
    @ConfigProperty(name = MODEL_NAME_PARAMETER_KEY)
    private String modelName;
    @ConfigProperty(name = NR_OF_ENTRIES_PARAMETER_KEY, defaultValue = "-1")
    private int nrOfEntries;
    @ConfigProperty(name = DATALOADER_THREADCOUNT_PARAMETER_KEY, defaultValue = "10")
    private int dataloaderThreadCount;

    public String getKieServerRestEndpoint() {
        return kieServerRestEndpoint;
    }

    public String getKieServerUsername() {
        return kieServerUsername;
    }

    public String getKieServerPassword() {
        return kieServerPassword;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getModelNamespace() {
        return modelNamespace;
    }

    public String getModelName() {
        return modelName;
    }

    public int getNrOfEntries() {
        return nrOfEntries;
    }

    public int getDataloaderThreadCount() {
        return dataloaderThreadCount;
    }

}