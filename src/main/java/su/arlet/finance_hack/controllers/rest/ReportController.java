package su.arlet.finance_hack.controllers.rest;

import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.client.Counter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.ReportComparison;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.services.ReportService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.path}/reports")
@Tag(name = "Report API")
public class ReportController {
    private final ReportService reportService;

    private final Counter reportsCounter;

    @Autowired
    public ReportController(ReportService reportService, MeterRegistry meterRegistry) {
        this.reportService = reportService;
        reportsCounter = (Counter) meterRegistry.counter("report_counter");
    }

    @GetMapping("/")
    @Operation(summary = "get reports by filters")
    @ApiResponse(responseCode = "200", description = "Success - report deleted", content = {
            @Content(schema = @Schema(implementation = Report.class))})
    public ResponseEntity<List<Report>> getReports(@RequestParam String periodType) {
        List<Report> reports = reportService.getReports(periodType);
        reportsCounter.inc();
        return ResponseEntity.ok(reports);
    }
//    @GetMapping("/date")
//    @Operation(summary = "Get report by date")
//    public ResponseEntity<List<Report>> getReportByDate(@RequestParam Timestamp date) {
//        List<Report> reports = reportService.getByDate(date);
//        return ResponseEntity.ok(reports);
//    }
//
//    @GetMapping("/period")
//    @Operation(summary = "Get reports by period")
//    public ResponseEntity<List<Report>> getReportsByPeriod(@RequestParam String periodType) {
//        List<Report> reports = reportService.getReportsByPeriod(periodType);
//        return ResponseEntity.ok(reports);
//    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete report by ID")
    @ApiResponse(responseCode = "200", description = "Success - report deleted", content = {
            @Content(schema = @Schema(implementation = Report.class))})
    @ApiResponse(responseCode = "204", description = "No content")
    @ApiResponse(responseCode = "403", description = "Forbidden - access denied")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> deleteReport(@RequestParam Long id, @PathVariable String username) {
        Report report = reportService.getByIdBeforeDeleting(id);
        if (!report.getUser().getUsername().equals(username)) {
            throw new UserNotFoundException();
        }
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare reports")
    public ResponseEntity<?> compareReports(
            @RequestBody ReportService.DateAndPeriod dap) {
        dap.validate();
        Optional<ReportComparison> comparison = reportService.compareReports(dap);

        if (comparison.isPresent()) {
            ReportService.ComparisonResult comparisonResult = reportService.displayDifferences(comparison.get());
            return ResponseEntity.ok(comparisonResult);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}