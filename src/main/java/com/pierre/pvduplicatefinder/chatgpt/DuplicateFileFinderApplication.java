package com.pierre.pvduplicatefinder.chatgpt;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.context.annotation.Bean;

@SpringBootApplication
        public class DuplicateFileFinderApplication {

            public static void main(String[] args) {
                SpringApplication.run(DuplicateFileFinderApplication.class, args);
            }

            @Bean
            CommandLineRunner run(DuplicateFinderService duplicateFinderService) {
                return args -> {
                    String directoryPath = args.length > 0 ? args[0] : "D:\\";
                    duplicateFinderService.findDuplicates(directoryPath);
                };
            }
        }