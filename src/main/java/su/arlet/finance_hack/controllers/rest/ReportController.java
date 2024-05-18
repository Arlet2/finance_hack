package su.arlet.finance_hack.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.ReportComparison;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.services.ReportService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.path}/reports")
@Tag(name = "Report API")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/date")
    @Operation(summary = "Get report by date")
    public ResponseEntity<Report> getReportByDate(@RequestParam Timestamp date) {
        Report report = reportService.getByDate(date);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete report by ID")
    @ApiResponse(responseCode = "204", description = "No content")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/period")
    @Operation(summary = "Get reports by period")
    public ResponseEntity<List<Report>> getReportsByPeriod(@RequestParam String periodType) {
        List<Report> reports = reportService.getReportsByPeriod(periodType);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare reports")
    public ResponseEntity<?> compareReports(
            @RequestParam int firstMonth,
            @RequestParam int firstYear,
            @RequestParam int secondMonth,
            @RequestParam int secondYear,
            @RequestParam String periodType) {
        Period period = Period.valueOf(periodType.toUpperCase());
        Optional<ReportComparison> comparison = reportService.compareReports(firstMonth, firstYear, secondMonth, secondYear, period);

        if (comparison.isPresent()) {
            ReportService.ComparisonResult comparisonResult = reportService.displayDifferences(comparison.get());
            return ResponseEntity.ok(comparisonResult);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
