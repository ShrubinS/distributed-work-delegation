package com.masterworker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WorkerService {

    private final RequestUtil requestUtil;
    private final List<String> workers;

    @Autowired
    public WorkerService(RequestUtil requestUtil) {
        this.requestUtil = requestUtil;
        this.workers = Collections.synchronizedList(new ArrayList<String>());
    }

    public String registerWorker() {
        String workerURI = requestUtil.getClientIp();
        synchronized (workers) {
            workers.add(workerURI);
        }
        return workerURI;
    }

    public String unRegisterWorker() {
        String workerURI = requestUtil.getClientIp();
        synchronized (workers) {
            workers.remove(workerURI);
        }
        return workerURI;
    }

    public List<String> getWorkers() {
        return this.workers;
    }

}
