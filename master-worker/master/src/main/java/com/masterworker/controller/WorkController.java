package com.masterworker.controller;

import com.masterworker.dto.WorkResponse;
import com.masterworker.service.ComplexityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkController {

    private final ComplexityService complexityService;

    @Autowired
    public WorkController(ComplexityService complexityService) {
        this.complexityService = complexityService;
    }

    @GetMapping("/complexity")
    public WorkResponse getComplexity(@RequestParam("repo-name") String repoName) throws Exception{
        return complexityService.getComplexity(repoName);
    }
}
