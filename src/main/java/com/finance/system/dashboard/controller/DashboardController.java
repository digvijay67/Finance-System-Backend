package com.finance.system.dashboard.controller;

import com.finance.system.common.ApiResponse;
import com.finance.system.dashboard.dto.DashboardDtos.*;
import com.finance.system.dashboard.service.DashboardService;
import com.finance.system.security.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Aggregated financial analytics (cached via Redis)")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Returns complete dashboard: totals, category breakdown, monthly trends.
     * ADMIN can query any userId; ANALYST/VIEWER are restricted to their own.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
        summary     = "Get dashboard summary",
        description = "Returns total income, total expense, net balance, category-wise breakdown, "
                    + "and 12-month trends. Results are cached in Redis (TTL 5 min) and invalidated "
                    + "on any record mutation."
    )
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Parameter(description = "Target userId (ADMIN only – defaults to caller)")
            @RequestParam(required = false) Long userId) {

        Long targetId = resolveTargetUserId(currentUser, userId);
        DashboardSummary summary = dashboardService.getDashboardSummary(targetId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Long resolveTargetUserId(UserDetailsImpl currentUser, Long requestedUserId) {
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        if (requestedUserId != null && isAdmin) {
            return requestedUserId;
        }
        return currentUser.getId();
    }
}
