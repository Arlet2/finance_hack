package su.arlet.finance_hack.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReportComparison {
    private final Report firstReport;
    private final Report secondReport;

    public ReportComparison(Report firstReport, Report secondReport) {
        this.firstReport = firstReport;
        this.secondReport = secondReport;
    }

    public Report getFirstReport() {
        return firstReport;
    }

    public Report getSecondReport() {
        return secondReport;
    }

    // разница по категориям
    public Map<String, Long> getCategoryDifferences() {
        Map<String, Long> differences = new HashMap<>();
        Map<String, Long> firstCategories = sumByCategory(firstReport.getReportCategories());
        Map<String, Long> secondCategories = sumByCategory(secondReport.getReportCategories());

        // Собираем все категории
        Set<String> allCategories = new HashSet<>(firstCategories.keySet());
        allCategories.addAll(secondCategories.keySet());

        // Вычисляем разницу для каждой категории
        for (String category : allCategories) {
            long firstSum = firstCategories.getOrDefault(category, 0L);
            long secondSum = secondCategories.getOrDefault(category, 0L);
            differences.put(category, firstSum - secondSum);
        }

        return differences;
    }

    // разница сумм
    public long getTotalDifference() {
        return firstReport.getTotal() - secondReport.getTotal();
    }

    // суммы по категориям
    private Map<String, Long> sumByCategory(Set<ReportCategory> categories) {
        Map<String, Long> categorySums = new HashMap<>();
        for (ReportCategory category : categories) {
            categorySums.put(category.getCategory(), category.getSum());
        }
        return categorySums;
    }
}