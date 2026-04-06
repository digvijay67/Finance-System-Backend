package com.finance.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Data
@Setter
@EqualsAndHashCode
@Getter
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class FinanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceSystemApplication.class, args);
    }
}
