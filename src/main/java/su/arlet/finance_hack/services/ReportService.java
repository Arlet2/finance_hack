package su.arlet.finance_hack.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.ReportCategory;
import su.arlet.finance_hack.core.ReportComparison;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.repos.ReportRepo;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepo reportRepo;

    public Report getByDate(Timestamp date) {
        return  reportRepo.findByCreated(date);
    }

    public void deleteReport (Long id) {reportRepo.deleteById(id);}

    public List<Report> getReportsByPeriod(String periodType) {
        Timestamp startDate = getStartDateByPeriod(periodType);
        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        return reportRepo.findAllByCreatedBetween(startDate, endDate);
    }

    private Timestamp getStartDateByPeriod(String periodType) {
        Calendar calendar = Calendar.getInstance();
        switch (periodType.toUpperCase()) {
            case "DAILY":
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case "WEEKLY":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "MONTHLY":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "YEARLY":
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                throw new IllegalArgumentException("Unknown period type: " + periodType);
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    public Report createNewReport(long total, Set<ReportCategory> reportCategories, Period period) {
        Report report = new Report();
        report.setCreated(new Timestamp(System.currentTimeMillis()));
        report.setTotal(total);
        report.setReportCategories(reportCategories);
        report.setPeriod(period);

        return reportRepo.save(report);
    }

    public Optional<ReportComparison> compareReports(int firstMonth, int firstYear, int secondMonth, int secondYear, Period period) {
        List<Report> firstReports = reportRepo.findReportsByMonthAndYear(firstMonth, firstYear, period);
        List<Report> secondReports = reportRepo.findReportsByMonthAndYear(secondMonth, secondYear, period);

        if (!firstReports.isEmpty() && !secondReports.isEmpty()) {
            ReportComparison comparison = new ReportComparison(firstReports.get(0), secondReports.get(0));
            displayDifferences(comparison);
            return Optional.of(comparison);
        }
        return Optional.empty();
    }

    private void displayDifferences(ReportComparison comparison) {
        Map<String, Long> categoryDifferences = comparison.getCategoryDifferences();
        //categoryDifferences.forEach((category, difference) ->
                //System.out.println("Category: " + category + ", Difference: " + difference));

        long totalDifference = comparison.getTotalDifference();
        //System.out.println("Total Difference: " + totalDifference);
    }

}
