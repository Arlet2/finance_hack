package su.arlet.finance_hack.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.exceptions.AccessDeniedException;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.ValidationException;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.repos.ReportRepo;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepo reportRepo;
    private final PaymentInfoRepo paymentInfoRepo;
    private final UserService userService;

    public List<Report> getReports(String periodType) {
        if (periodType == null) {
            Timestamp date = new Timestamp(System.currentTimeMillis());
            return reportRepo.findByCreated(date);
        }

        Timestamp startDate = getStartDateByPeriod(periodType);
        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        return reportRepo.findAllByCreatedBetween(startDate, endDate);
    }


    public void deleteReport(Long id, User user) {
        Report report = reportRepo.findById(id).orElseThrow(EntityWasAlreadyDeletedException::new);

        if (!report.getUser().getUsername().equals(user.getUsername()))
            throw new AccessDeniedException();

        reportRepo.deleteById(id);
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
                throw new ValidationException("Unknown period type: " + periodType + ". Use DAILY, WEEKLY, MONTHLY, YEARLY");
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    public void createNewReport(Period period) {

        List<User> users = userService.getAllUsers();
        for (User user : users) {
            List<PaymentInfo> payments = paymentInfoRepo.findByUser(user);

            long totalSum = payments.stream().mapToLong(PaymentInfo::getSum).sum();
            Map<String, Long> categorySums = payments.stream()
                    .collect(Collectors.groupingBy(
                            payment -> payment.getItemCategory().getName(),
                            Collectors.summingLong(PaymentInfo::getSum)
                    ));

            Set<ReportCategory> reportCategories = categorySums.entrySet().stream()
                    .map(entry -> new ReportCategory(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toSet());

            Report report = new Report();
            report.setCreated(new Timestamp(System.currentTimeMillis()));
            report.setPeriod(period);
            report.setTotal(totalSum);
            report.setReportCategories(reportCategories);
            report.setUser(user);
            reportRepo.save(report);
        }

        // TODO уведомлялка о новом репорте
    }

    public Optional<ReportComparison> compareReports(DateAndPeriod dap) {
        List<Report> firstReports = reportRepo.findReportsByMonthAndYear(dap.getFirstMonth(), dap.getFirstYear(), dap.getPeriod());
        List<Report> secondReports = reportRepo.findReportsByMonthAndYear(dap.getSecondMonth(), dap.getSecondYear(), dap.getPeriod());

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

    public record ComparisonResult(Map<String, Long> categoryDifferences, long totalDifference) {
    }


    @Getter
    public static class DateAndPeriod {
        private final int firstMonth;
        private final int firstYear;
        private final int secondMonth;
        private final int secondYear;
        private final Period period;

        public DateAndPeriod(int firstMonth, int firstYear, int secondMonth, int secondYear, String periodType) {
            this.firstMonth = firstMonth;
            this.firstYear = firstYear;
            this.secondMonth = secondMonth;
            this.secondYear = secondYear;
            this.period = Period.valueOf(periodType);
        }

        public void validate() {
            int currentYear = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();
            if (firstMonth != currentMonth) throw new ValidationException("Month can't be not positive");
            if (firstYear != currentYear) throw new ValidationException("Year can't be not positive");
            if (secondMonth != currentMonth) throw new ValidationException("Month can't be not positive");
            if (secondYear != currentYear) throw new ValidationException("Month can't be not positive");
            if (Period.isEnumContains(period)) throw new ValidationException("period can't be not in enum");
        }
    }
}
