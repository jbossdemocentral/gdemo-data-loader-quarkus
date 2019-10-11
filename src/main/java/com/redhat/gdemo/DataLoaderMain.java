package com.redhat.gdemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * DataLoaderMain
 */
public class DataLoaderMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoaderMain.class);

    private DataLoader applicationClient;

    @Inject
    private DataLoaderConfiguration dataLoaderConfiguration;

    public void onStartup(@Observes StartupEvent startupEvent) {
        LOGGER.info("DataLoader bootstrap. Starting loader.");
        //Start the DataLoader
        this.applicationClient = new DataLoader(dataLoaderConfiguration);
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(this.applicationClient);
        LOGGER.info("Submitted DataLoader to Executor.");

    }

    public void onShutdown(@Observes ShutdownEvent shutdownEvent) {
        LOGGER.info("DataLoader shutdown. Stopping loader");
     }



}