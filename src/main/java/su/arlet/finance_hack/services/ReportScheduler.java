package su.arlet.finance_hack.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import su.arlet.finance_hack.core.enums.Period;

@Component
public class ReportScheduler {

    private final TaskScheduler scheduler;

    private final ReportService reportService;

    @Autowired
    public ReportScheduler(TaskScheduler scheduler, ReportService reportService) {
        this.scheduler = scheduler;
        this.reportService = reportService;
    }

    @PostConstruct
    public void init() {
        scheduler.schedule(() -> reportService.createNewReport(Period.DAILY), new CronTrigger(Period.DAILY.getPeriodMessage()));
        scheduler.schedule(() -> reportService.createNewReport(Period.WEEKLY), new CronTrigger(Period.WEEKLY.getPeriodMessage()));
        scheduler.schedule(() -> reportService.createNewReport(Period.MONTHLY), new CronTrigger(Period.MONTHLY.getPeriodMessage()));
        scheduler.schedule(() -> reportService.createNewReport(Period.YEARLY), new CronTrigger(Period.YEARLY.getPeriodMessage()));
    }
}
