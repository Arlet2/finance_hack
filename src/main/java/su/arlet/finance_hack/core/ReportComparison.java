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
}