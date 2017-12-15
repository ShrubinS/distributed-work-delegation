package com.masterworker.service;

import com.masterworker.dto.WorkResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ComplexityService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ComplexityService.class);

    private final Map<String, Long> workersMap;
    private final List<String> workers;
    private final RestTemplate restTemplate;

    @Value("${worker.max_thread}")
    private Integer MAX_WORKERS;

    @Autowired
    public ComplexityService(WorkerService workerService, RestTemplate restTemplate) {
        this.workers = workerService.getWorkers();
        this.workersMap = new HashMap<>();
        this.workers.forEach(worker -> this.workersMap.put(worker, 0L));
        this.restTemplate = restTemplate;
    }

    public WorkResponse getComplexity(String repoName) throws InterruptedException, ExecutionException {
        List<String> fileURIs = getFileURI(repoName);
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(fileURIs.size());

        blockingQueue.addAll(fileURIs);

        Double complexityValue = 0d;
        List<CompletableFuture<OptionalDouble>> all = new ArrayList<>();

        while (!fileURIs.isEmpty()) {

            Map<CompletableFuture<String>, String> futures = workers.stream()
                    .map(worker -> {
                        String file;
                        try {
                            file = blockingQueue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e.getCause());
                        }
                        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                                    String s;
                                    try {
                                        s = getResponse(file, worker);
                                    } catch (UnsupportedEncodingException | ResourceAccessException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e.getCause());
                                    }
                                    return s;
                                });
                        return new AbstractMap.SimpleImmutableEntry<>(future, file);
                        }
                    )
                    .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));

            futures.keySet().forEach(future -> future
                    .exceptionally(err -> {
                        if (err instanceof ResourceAccessException) {
                            try {
                                blockingQueue.put(futures.get(future));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e.getCause());
                            }
                        }
                        err.printStackTrace();
                        return null;
                    })
                    .thenAccept(s -> {
                        fileURIs.remove(futures.get(future));
                    }));

            CompletableFuture<List<String>> allDone = sequence(futures.keySet());

            all.add(allDone.thenApply(future -> future
                    .stream()
                    .mapToDouble(Double::valueOf)
                    .average()
            ));

        }

        CompletableFuture<List<OptionalDouble>> allDone = sequence(all);

        CompletableFuture<OptionalDouble> complexityFuture = allDone.thenApply(future -> future
                .stream()
                .mapToDouble(val -> {
                    if( val.isPresent() ){
                        return val.getAsDouble();
                    }
                    return 0;
                })
                .average()
        );

        Double complexity;

        try {
            complexity = complexityFuture.get().getAsDouble();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw e;
        }

        WorkResponse workResponse = new WorkResponse();
        workResponse.setComplexity(complexity);
        return workResponse;
    }


    /*
        Utility to convert to CompletableFuture<List<T>>, instead of CompletableFuture<Void>
     */
    private static <T> CompletableFuture<List<T>> sequence(Collection<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.toList())
        );
    }

    private String getResponse(String fileURI, String workerURI) throws UnsupportedEncodingException, ResourceAccessException{
        String response;
        try {
            response = restTemplate.getForObject(workerURI + "file/{file}", String.class, URLEncoder.encode(fileURI, StandardCharsets.UTF_8.toString()));
        } catch (ResourceAccessException e) {
            log.error(e.getMessage());
            throw e;
        }
        return response;
    }

//    private String getFile(BlockingQueue<String> blockingQueue) throws InterruptedException{
//        String s;
//        try {
//            s = blockingQueue.take();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            throw new InterruptedException(e.getMessage());
//        }
//        return s;
//    }

    private List<String> getFileURI(String repoName) {
        /*
            get the URI of all files, using github API
            currently returning a dummy response
         */
        return new ArrayList<>();
    }

}
