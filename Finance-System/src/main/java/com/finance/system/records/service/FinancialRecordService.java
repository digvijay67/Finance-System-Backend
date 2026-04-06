package com.finance.system.records.service;

import com.finance.system.entity.FinancialRecord;
import com.finance.system.entity.User;
import com.finance.system.exception.ApiException;
import com.finance.system.records.dto.RecordDtos.*;
import com.finance.system.repository.FinancialRecordRepository;
import com.finance.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.finance.system.config.RedisConfig.DASHBOARD_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    // ── Create ────────────────────────────────────────────────────────────

    @Transactional
    public RecordResponse createRecord(RecordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found: " + userId, HttpStatus.NOT_FOUND));

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .description(request.getDescription())
                .user(user)
                .build();

        record = recordRepository.save(record);
        evictDashboardCache(userId);
        log.info("Created record id={} for user={}", record.getId(), userId);
        return mapToResponse(record);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecordResponse> getRecords(Long userId,
                                           FinancialRecord.RecordType type,
                                           String category,
                                           java.time.LocalDate from,
                                           java.time.LocalDate to) {
        return recordRepository.findByFilters(userId, type, category, from, to)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecordResponse getRecordById(Long id, Long userId, String role) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ApiException("Record not found: " + id, HttpStatus.NOT_FOUND));

        if (!role.equals("ADMIN") && !record.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied to record: " + id, HttpStatus.FORBIDDEN);
        }
        return mapToResponse(record);
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Transactional
    public RecordResponse updateRecord(Long id, RecordRequest request, Long userId, String role) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ApiException("Record not found: " + id, HttpStatus.NOT_FOUND));

        if (!role.equals("ADMIN") && !record.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied to record: " + id, HttpStatus.FORBIDDEN);
        }

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        record = recordRepository.save(record);
        evictDashboardCache(record.getUser().getId());
        log.info("Updated record id={}", id);
        return mapToResponse(record);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteRecord(Long id, Long userId, String role) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ApiException("Record not found: " + id, HttpStatus.NOT_FOUND));

        if (!role.equals("ADMIN") && !record.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied to record: " + id, HttpStatus.FORBIDDEN);
        }

        Long ownerId = record.getUser().getId();
        recordRepository.deleteById(id);
        evictDashboardCache(ownerId);
        log.info("Deleted record id={}", id);
    }

    // ── Cache Eviction ────────────────────────────────────────────────────

    private void evictDashboardCache(Long userId) {
        String cacheKey = "dashboard:user:" + userId;
        Objects.requireNonNull(cacheManager.getCache(DASHBOARD_CACHE)).evict(cacheKey);
        log.debug("Evicted dashboard cache for user={}", userId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    public RecordResponse mapToResponse(FinancialRecord r) {
        return RecordResponse.builder()
                .id(r.getId())
                .amount(r.getAmount())
                .type(r.getType().name())
                .category(r.getCategory())
                .date(r.getDate())
                .description(r.getDescription())
                .userId(r.getUser().getId())
                .userEmail(r.getUser().getEmail())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
