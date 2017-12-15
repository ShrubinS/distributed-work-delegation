package com.masterworker.controller;

import com.masterworker.service.WorkerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {

    private WorkerService workerService;

    @PostMapping("/register")
    public String registerWorker() {
        return workerService.registerWorker();
    }

    @PostMapping("/un-register")
    public String unRegisterWorker() {
        return workerService.unRegisterWorker();
    }

}
