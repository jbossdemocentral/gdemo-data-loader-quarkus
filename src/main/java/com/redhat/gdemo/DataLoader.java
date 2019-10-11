package com.redhat.gdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataLoader implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    private DMNServicesClient dmnClient;
    private KieServicesConfiguration kieServicesConfiguration;

    private DataLoaderConfiguration configuration;

    
    public List<String[]> sampleData = new ArrayList<>();
    public AtomicInteger counter = new AtomicInteger(0);
    

    public boolean shutdown = false;

        protected void setupClients(KieServicesClient kieServicesClient) {
        this.dmnClient = kieServicesClient.getServicesClient(DMNServicesClient.class);
    }

    public DataLoader(final DataLoaderConfiguration configuration) {
        this.configuration = configuration;
        
        this.kieServicesConfiguration = KieServicesFactory.newRestConfiguration(configuration.getKieServerRestEndpoint(), configuration.getKieServerUsername(), configuration.getKieServerPassword());
    }

    protected KieServicesClient createDefaultClient() throws Exception {

        kieServicesConfiguration.setTimeout(3000);
        kieServicesConfiguration.setMarshallingFormat(MarshallingFormat.JSON);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(kieServicesConfiguration);

        setupClients(kieServicesClient);
        return kieServicesClient;
    }

    public DMNServicesClient getDmnClient() {
        return dmnClient;
    }

    @Override
    public void run() {
        
        LOGGER.info("Starting DataLoader runnable!");

        loadSampleData();
        try {
            createDefaultClient();
        } catch (Exception e) {
            String msg = "Caught exception while creating default client. Shutting down!";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        final int parallelism = configuration.getDataloaderThreadCount();
        final ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        final CyclicBarrier started = new CyclicBarrier(parallelism);
        final Callable<Long> task = () -> {
            started.await();
            final Thread current = Thread.currentThread();
            long executions = 0;
            int nrOfEntries = configuration.getNrOfEntries();
            while (!current.isInterrupted() && (counter.get() < nrOfEntries || nrOfEntries == -1)) {
                evaluateDMNWithPause(getDmnClient());
                executions++;
                if (executions % 1000 == 0) {
                    LOGGER.info(executions + " requests sent");
                }
            }
            LOGGER.info("Thread '" + current.getId() + "' executed '" + executions + "' requests.");
            return executions;
        };
        final ArrayList<Future<Long>> tasks = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            tasks.add(executor.submit(task));
        }
        executor.shutdown();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            tasks.forEach(future -> future.cancel(true));
        }));

    }
    private void loadSampleData() {
        String text = new Scanner(DataLoader.class.getClassLoader().getResourceAsStream("sampleDataLarge"), "UTF-8")
                .useDelimiter("\\Z").next();

        sampleData = Arrays.stream(text.split("\\n")).map(row -> row.split(",")).collect(Collectors.toList());
    }

    private void evaluateDMNWithPause(DMNServicesClient dmnClient) {
        try {
            DMNContext dmnContext = dmnClient.newContext();

            int index = counter.getAndIncrement() % sampleData.size();
            String[] sampleDataRow = sampleData.get(index);

            double fraudAmount = Double.valueOf(sampleDataRow[0]);
            String cardHolderStatus = sampleDataRow[1];
            double incidentCount = Double.valueOf(sampleDataRow[2]);
            double age = Double.valueOf(sampleDataRow[3]);

            dmnContext.set("Fraud Amount", fraudAmount);
            dmnContext.set("Cardholder Status", cardHolderStatus);
            dmnContext.set("Incident Count", incidentCount);
            dmnContext.set("Age", age);
            LOGGER.info("Evaluating DMN with values: Fraud Amount: " + fraudAmount + ", Cardholder Status: "
                    + cardHolderStatus + ", Incident Count: " + incidentCount + ", Age: " + age);
            ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(configuration.getContainerId(), configuration.getModelNamespace(), configuration.getModelName(), dmnContext);
            LOGGER.info("result" + evaluateAll.getMsg());
            if (evaluateAll.getResult().hasErrors()) {
                LOGGER.info("Result has errors.");
            }
   
        } catch (org.kie.server.common.rest.NoEndpointFoundException nef) {
            LOGGER.error("Error during DMN Execution. No endpoint found. Ignoring.", nef);
        } catch (Throwable t) {
            LOGGER.error("Error during DMN Execution", t);
            throw t;
        }
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = true;
    }

    
}
