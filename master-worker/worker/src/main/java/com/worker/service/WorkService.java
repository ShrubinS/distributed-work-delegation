package com.worker.service;

import org.springframework.stereotype.Service;

@Service
public class WorkService {

    public String findComplexity(String file) {
        /*
            call library to calculate complexity, return value
            returning dummy value
         */
        return String.valueOf(1);
    }

}
