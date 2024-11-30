package com.mnkbatch.config;

import com.mnkbatch.entity.Employee;
import com.mnkbatch.repository.EmployeeRepo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class EmployeeConfig {

    @Autowired
    EmployeeRepo employeeRepo;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public FlatFileItemReader<Employee> employeeReader() {
        FlatFileItemReader<Employee> itemReader = new FlatFileItemReader<>();

        itemReader.setResource(new FileSystemResource("src/main/resources/employee-data.csv"));
        itemReader.setName("csv-reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        return itemReader;
    }

    private LineMapper<Employee> lineMapper() {
        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","email","gender","salary");

        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public EmployeeProcessor employeeProcessor(){
        return new EmployeeProcessor();
    }

    @Bean
    public RepositoryItemWriter<Employee> employeeWriter(){
        RepositoryItemWriter<Employee> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(employeeRepo);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }

    @Bean
    public Step step(){
        return new StepBuilder("step-1", jobRepository).<Employee, Employee>chunk(10, transactionManager)
                .reader(employeeReader())
                .processor(employeeProcessor())
                .writer(employeeWriter())
                .build();
    }

    @Bean
    public Job job(){
        return new JobBuilder("employee-job", jobRepository)
                .flow(step())
                .end().build();
    }
}
