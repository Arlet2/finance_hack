package su.arlet.finance_hack.core;

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