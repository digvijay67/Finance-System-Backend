package com.finance.system.config;

import com.finance.system.entity.FinancialRecord;
import com.finance.system.entity.User;
import com.finance.system.repository.FinancialRecordRepository;
import com.finance.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")   // skip during tests
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: data already present, skipping.");
            return;
        }

        log.info("DataSeeder: seeding initial data...");
        seedUsers();
        log.info("DataSeeder: done.");
    }

    // ── Users ─────────────────────────────────────────────────────────────

    private void seedUsers() {
        String pw = passwordEncoder.encode("Password1!");

        User admin = createUser("Admin User",   "admin@finance.com",   pw, User.Role.ADMIN);
        User yash = createUser("Yash Rajput","yash@finance.com",   pw, User.Role.ANALYST);
        User Raj   = createUser("Raj Rathod",    "raj@finance.com",     pw, User.Role.ANALYST);
        User Samir = createUser("Samir shaikh",  "samir@finance.com",   pw, User.Role.VIEWER);
        User Amir = createUser("Amir khan",  "amir@finance.com",   pw, User.Role.VIEWER);

        userRepository.saveAll(List.of(admin, yash, Raj, Samir, Amir));
        log.info("DataSeeder: 5 users created.");

        seedRecords(yash, Raj);
    }

    private User createUser(String name, String email, String pw, User.Role role) {
        return User.builder()
                .fullName(name).email(email)
                .password(pw).role(role)
                .status(User.UserStatus.ACTIVE)
                .build();
    }

    // ── Financial Records ─────────────────────────────────────────────────

    private void seedRecords(User yash, User raj) {
        List<FinancialRecord> records = List.of(

            // Alice – income
            record(new BigDecimal("5000.00"), FinancialRecord.RecordType.INCOME,
                   "Salary",    LocalDate.of(2024, 1, 31), "January salary", yash),
            record(new BigDecimal("5000.00"), FinancialRecord.RecordType.INCOME,
                   "Salary",    LocalDate.of(2024, 2, 29), "February salary", yash),
            record(new BigDecimal("800.00"),  FinancialRecord.RecordType.INCOME,
                   "Freelance", LocalDate.of(2024, 3, 15), "Website project", yash),

            // Alice – expense
            record(new BigDecimal("1200.00"), FinancialRecord.RecordType.EXPENSE,
                   "Rent",      LocalDate.of(2024, 1, 5),  "Monthly rent", yash),
            record(new BigDecimal("350.00"),  FinancialRecord.RecordType.EXPENSE,
                   "Groceries", LocalDate.of(2024, 1, 20), "Supermarket", yash),
            record(new BigDecimal("200.00"),  FinancialRecord.RecordType.EXPENSE,
                   "Utilities", LocalDate.of(2024, 2, 10), "Electric + water", yash),

            // Bob – income
            record(new BigDecimal("6500.00"), FinancialRecord.RecordType.INCOME,
                   "Salary",    LocalDate.of(2024, 1, 31), "January salary", raj),
            record(new BigDecimal("1500.00"), FinancialRecord.RecordType.INCOME,
                   "Bonus",     LocalDate.of(2024, 3, 1),  "Q1 performance bonus", raj),

            // Bob – expense
            record(new BigDecimal("2000.00"), FinancialRecord.RecordType.EXPENSE,
                   "Rent",      LocalDate.of(2024, 1, 5),  "Monthly rent", raj),
            record(new BigDecimal("500.00"),  FinancialRecord.RecordType.EXPENSE,
                   "Travel",    LocalDate.of(2024, 2, 18), "Business trip flights", raj),
            record(new BigDecimal("150.00"),  FinancialRecord.RecordType.EXPENSE,
                   "Dining",    LocalDate.of(2024, 3, 22), "Team dinner", raj)
        );

        recordRepository.saveAll(records);
        log.info("DataSeeder: {} financial records created.", records.size());
    }

    private FinancialRecord record(BigDecimal amount, FinancialRecord.RecordType type,
                                   String category, LocalDate date, String desc, User user) {
        return FinancialRecord.builder()
                .amount(amount).type(type).category(category)
                .date(date).description(desc).user(user)
                .build();
    }
}
