package com.finance.system.records.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.system.common.ApiResponse;
import com.finance.system.entity.FinancialRecord;
import com.finance.system.records.dto.RecordDtos.RecordRequest;
import com.finance.system.records.dto.RecordDtos.RecordResponse;
import com.finance.system.records.service.FinancialRecordService;
import com.finance.system.security.service.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Data
@Setter
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/records")
@Tag(name = "Financial Records", description = "CRUD operations for financial records")
@SecurityRequirement(name = "BearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // ── Create ────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Create a financial record")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody RecordRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        RecordResponse response = recordService.createRecord(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", response));
    }

    // ── Read All (with filters) ───────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get financial records with optional filters")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> getRecords(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(description = "Filter by type: INCOME or EXPENSE")
            @RequestParam(required = false) FinancialRecord.RecordType type,
            @Parameter(description = "Filter by category (exact match)")
            @RequestParam(required = false) String category,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // ADMIN sees all records by passing null userId → we always scope to self unless ADMIN
        Long userId = currentUser.getId();
        String role = currentUser.getRole().name();

        List<RecordResponse> records = recordService.getRecords(
                role.equals("ADMIN") ? null : userId,
                type, category, from, to);

        return ResponseEntity.ok(ApiResponse.success("Records retrieved", records));
    }

    // ── Read by ID ────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get a single financial record by ID")
    public ResponseEntity<ApiResponse<RecordResponse>> getRecordById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        RecordResponse response = recordService.getRecordById(
                id, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.ok(ApiResponse.success("Record retrieved", response));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Update a financial record")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        RecordResponse response = recordService.updateRecord(
                id, request, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully", response));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a financial record (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        recordService.deleteRecord(id, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully", null));
    }
}
