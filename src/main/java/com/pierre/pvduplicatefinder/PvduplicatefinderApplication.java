package com.pierre.pvduplicatefinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PvduplicatefinderApplication implements CommandLineRunner {
    @Autowired
    FinderService finderService;

    public static void main(String[] args) {
        SpringApplication.run(PvduplicatefinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<DuplicateCollection> result = finderService.findDuplicates("D:\\");
        System.out.println(result);
    }
}
