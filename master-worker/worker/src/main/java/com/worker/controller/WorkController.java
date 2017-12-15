package com.worker.controller;

import com.worker.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@RestController
public class WorkController {

    private final WorkService workService;
    private final RestTemplate restTemplate;

    @Value("${master.address}")
    private String MASTER_URI;

    @Autowired
    public WorkController(WorkService workService, RestTemplate restTemplate) {
        this.workService = workService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/file/{file}")
    public String findComplexity(@PathVariable String file) {
        return workService.findComplexity(file);
    }

    @PostConstruct
    public void register() {
        restTemplate.getForObject(MASTER_URI+"register", Void.class);
    }

    @PreDestroy
    public void unregister() {
        restTemplate.getForObject(MASTER_URI+"un-register", Void.class);
    }

}
