package su.arlet.finance_hack.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.ReportCategory;
import su.arlet.finance_hack.core.ReportComparison;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.exceptions.RepoAlreadyDeleteException;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.repos.ReportRepo;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepo reportRepo;

    public List<Report> getReports(String periodType) {
        if (periodType==null) {
            Timestamp date = new Timestamp(System.currentTimeMillis());
            return  reportRepo.findByCreated(date);
        }
        Timestamp startDate = getStartDateByPeriod(periodType);
        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        return reportRepo.findAllByCreatedBetween(startDate, endDate);
    }


    public void deleteReport (Long id) {
        Report report = reportRepo.findById(id).orElseThrow(RepoAlreadyDeleteException::new);
        reportRepo.deleteById(id);
    }

//    public List<Report> getByDate(Timestamp date) {
//        return  reportRepo.findByCreated(date);
//    }

//    public List<Report> getReportsByPeriod(String periodType) {
//        Timestamp startDate = getStartDateByPeriod(periodType);
//        Timestamp endDate = new Timestamp(System.currentTimeMillis());
//        return reportRepo.findAllByCreatedBetween(startDate, endDate);
//    }

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

    public void createNewReport(Period period) {


        Report report = new Report();
        report.setCreated(new Timestamp(System.currentTimeMillis()));
        report.setTotal(total);
        report.setReportCategories(reportCategories);
        report.setPeriod(period);

        return reportRepo.save(report);

        // TODO уведомлялка о новом репорте
    }

    public Optional<ReportComparison> compareReports(int firstMonth, int firstYear, int secondMonth, int secondYear, Period period) {
        List<Report> firstReports = reportRepo.findReportsByMonthAndYear(firstMonth, firstYear, period);
        List<Report> secondReports = reportRepo.findReportsByMonthAndYear(secondMonth, secondYear, period);

        if (!firstReports.isEmpty() && !secondReports.isEmpty()) {
            ReportComparison comparison = new ReportComparison(firstReports.get(0), secondReports.get(0));
            return Optional.of(comparison);
        }
        return Optional.empty();
    }

    public ComparisonResult displayDifferences(ReportComparison comparison) {
        Map<String, Long> categoryDifferences = calculateCategoryDifferences(comparison.getFirstReport(), comparison.getSecondReport());
        long totalDifference = calculateTotalDifference(comparison.getFirstReport(), comparison.getSecondReport());

        return new ComparisonResult(categoryDifferences, totalDifference);
    }

    private Map<String, Long> calculateCategoryDifferences(Report firstReport, Report secondReport) {
        Map<String, Long> firstCategories = sumByCategory(firstReport.getReportCategories());
        Map<String, Long> secondCategories = sumByCategory(secondReport.getReportCategories());
        Map<String, Long> differences = new HashMap<>();

        Set<String> allCategories = new HashSet<>(firstCategories.keySet());
        allCategories.addAll(secondCategories.keySet());

        for (String category : allCategories) {
            long firstSum = firstCategories.getOrDefault(category, 0L);
            long secondSum = secondCategories.getOrDefault(category, 0L);
            differences.put(category, secondSum - firstSum);
        }

        return differences;
    }

    private long calculateTotalDifference(Report firstReport, Report secondReport) {
        return secondReport.getTotal() - firstReport.getTotal();
    }

    private Map<String, Long> sumByCategory(Set<ReportCategory> categories) {
        Map<String, Long> categorySums = new HashMap<>();
        for (ReportCategory category : categories) {
            categorySums.put(category.getCategory(), category.getSum());
        }
        return categorySums;
    }

    public Report getByIdBeforeDeleting(Long id) {
        return reportRepo.findById(id).orElseThrow(WasteAlreadyDeletedException::new);
    }
    public class ComparisonResult {
        private Map<String, Long> categoryDifferences;
        private long totalDifference;

        public ComparisonResult(Map<String, Long> categoryDifferences, long totalDifference) {
            this.categoryDifferences = categoryDifferences;
            this.totalDifference = totalDifference;
        }

        public Map<String, Long> getCategoryDifferences() {
            return categoryDifferences;
        }

        public long getTotalDifference() {
            return totalDifference;
        }
    }
}
