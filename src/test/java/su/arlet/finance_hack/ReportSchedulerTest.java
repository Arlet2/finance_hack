package su.arlet.finance_hack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.services.ReportScheduler;
import su.arlet.finance_hack.services.ReportService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReportSchedulerTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportScheduler reportScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitSchedulesReports() {
        // Manually call the init method to trigger the scheduling
        reportScheduler.init();

        // Verify that the scheduler schedules tasks with the correct periods
        verify(taskScheduler).schedule(any(Runnable.class), eq(new CronTrigger(Period.DAILY.getPeriodMessage())));
        verify(taskScheduler).schedule(any(Runnable.class), eq(new CronTrigger(Period.WEEKLY.getPeriodMessage())));
        verify(taskScheduler).schedule(any(Runnable.class), eq(new CronTrigger(Period.MONTHLY.getPeriodMessage())));
        verify(taskScheduler).schedule(any(Runnable.class), eq(new CronTrigger(Period.YEARLY.getPeriodMessage())));
    }

    @Test
    void testScheduledTasksCallCreateNewReport() {
        // Capture the Runnables passed to the scheduler
        ArgumentCaptor<Runnable> dailyTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> weeklyTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> monthlyTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> yearlyTaskCaptor = ArgumentCaptor.forClass(Runnable.class);

        // Manually call the init method to trigger the scheduling
        reportScheduler.init();

        // Capture the scheduled tasks
        verify(taskScheduler).schedule(dailyTaskCaptor.capture(), eq(new CronTrigger(Period.DAILY.getPeriodMessage())));
        verify(taskScheduler).schedule(weeklyTaskCaptor.capture(), eq(new CronTrigger(Period.WEEKLY.getPeriodMessage())));
        verify(taskScheduler).schedule(monthlyTaskCaptor.capture(), eq(new CronTrigger(Period.MONTHLY.getPeriodMessage())));
        verify(taskScheduler).schedule(yearlyTaskCaptor.capture(), eq(new CronTrigger(Period.YEARLY.getPeriodMessage())));

        // Run the captured tasks and verify that they call the reportService.createNewReport method with the correct arguments
        dailyTaskCaptor.getValue().run();
        verify(reportService).createNewReport(Period.DAILY);

        weeklyTaskCaptor.getValue().run();
        verify(reportService).createNewReport(Period.WEEKLY);

        monthlyTaskCaptor.getValue().run();
        verify(reportService).createNewReport(Period.MONTHLY);

        yearlyTaskCaptor.getValue().run();
        verify(reportService).createNewReport(Period.YEARLY);
    }
}

