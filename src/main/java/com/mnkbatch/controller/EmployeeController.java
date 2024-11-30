package com.mnkbatch.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @GetMapping("/employees")
    public String loadCsvToDb() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("Start ", System.currentTimeMillis()).toJobParameters();
        if((jobLauncher.run(job, jobParameters)).getStatus() == BatchStatus.COMPLETED) {
            return "records inserted successful";
        } else
            return "batch Job failed";
    }
}
