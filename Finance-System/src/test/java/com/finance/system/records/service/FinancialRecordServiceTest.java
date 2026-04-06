package com.finance.system.records.service;

import com.finance.system.entity.FinancialRecord;
import com.finance.system.entity.User;
import com.finance.system.exception.ApiException;
import com.finance.system.records.dto.RecordDtos.RecordRequest;
import com.finance.system.records.dto.RecordDtos.RecordResponse;
import com.finance.system.repository.FinancialRecordRepository;
import com.finance.system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private FinancialRecordService recordService;

    @Test
    void createRecord_shouldPersistAndReturnRecordResponse() {
        User user = User.builder()
                .id(7L)
                .email("test@finance.com")
                .password("pass")
                .role(User.Role.ANALYST)
                .status(User.UserStatus.ACTIVE)
                .build();

        RecordRequest request = RecordRequest.builder()
                .amount(new BigDecimal("123.45"))
                .type(FinancialRecord.RecordType.INCOME)
                .category(" Salary ")
                .date(LocalDate.of(2026, 4, 5))
                .description("test record")
                .build();

        FinancialRecord savedRecord = FinancialRecord.builder()
                .id(33L)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .description(request.getDescription())
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(savedRecord);
        when(cacheManager.getCache("dashboard")).thenReturn(cache);

        RecordResponse response = recordService.createRecord(request, 7L);

        assertThat(response.getId()).isEqualTo(33L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(response.getCategory()).isEqualTo("Salary");
        assertThat(response.getType()).isEqualTo("INCOME");
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getUserEmail()).isEqualTo("test@finance.com");

        assertThat(response.getCreatedAt()).isNotNull();

        org.mockito.Mockito.verify(cache).evict("dashboard:user:7");
    }

    @Test
    void getRecordById_nonOwnerShouldThrowForbidden() {
        User user = User.builder().id(7L).email("owner@finance.com").role(User.Role.ANALYST).status(User.UserStatus.ACTIVE).password("pass").build();
        FinancialRecord record = FinancialRecord.builder().id(88L).user(user).build();

        when(recordRepository.findById(88L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> recordService.getRecordById(88L, 9L, "VIEWER"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Access denied to record: 88");
    }

    @Test
    void getRecordById_missingRecordShouldThrowNotFound() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L, 7L, "ADMIN"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Record not found: 99");
    }
}
