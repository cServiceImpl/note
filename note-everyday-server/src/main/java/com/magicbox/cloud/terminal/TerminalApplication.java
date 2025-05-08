package com.magicbox.cloud.terminal;

import com.magicbox.cloud.log.annotation.EnableTracing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;


@EnableOpenApi
@EnableFeignClients
@EnableTransactionManagement
@SpringBootApplication
@EnableTracing
public class TerminalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerminalApplication.class, args);
    }

}
