package su.arlet.finance_hack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import su.arlet.finance_hack.core.*;
import su.arlet.finance_hack.core.enums.Period;
import su.arlet.finance_hack.exceptions.EntityWasAlreadyDeleteException;
import su.arlet.finance_hack.repos.PaymentInfoRepo;
import su.arlet.finance_hack.repos.ReportRepo;
import su.arlet.finance_hack.services.AuthService;
import su.arlet.finance_hack.services.ReportService;
import su.arlet.finance_hack.services.UserService;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepo reportRepo;

    @Mock
    private PaymentInfoRepo paymentInfoRepo;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReportsWithoutPeriod() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<Report> expectedReports = Collections.singletonList(new Report());
        when(reportRepo.findByCreated(any(Timestamp.class))).thenReturn(expectedReports);

        List<Report> reports = reportService.getReports(null);

        assertEquals(expectedReports, reports);
        verify(reportRepo).findByCreated(any(Timestamp.class));
    }

    @Test
    void testGetReportsWithPeriod() {
        Timestamp startDate = reportService.getStartDateByPeriod("DAILY");
        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        List<Report> expectedReports = Collections.singletonList(new Report());
        when(reportRepo.findAllByCreatedBetween(startDate, endDate)).thenReturn(expectedReports);

        List<Report> reports = reportService.getReports("DAILY");

        assertEquals(expectedReports, reports);
        verify(reportRepo).findAllByCreatedBetween(startDate, endDate);
    }

    @Test
    void testDeleteReport() {
        Long reportId = 1L;
        Report report = new Report();
        when(reportRepo.findById(reportId)).thenReturn(Optional.of(report));

        reportService.deleteReport(reportId);

        verify(reportRepo).findById(reportId);
        verify(reportRepo).deleteById(reportId);
    }

    @Test
    void testDeleteReportThrowsExceptionIfNotFound() {
        Long reportId = 1L;
        when(reportRepo.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(EntityWasAlreadyDeleteException.class, () -> reportService.deleteReport(reportId));

        verify(reportRepo).findById(reportId);
        verify(reportRepo, never()).deleteById(anyLong());
    }

    @Test
    void testCreateNewReport() {
        List<User> users = Collections.singletonList(new User());
        List<PaymentInfo> payments = Collections.singletonList(new PaymentInfo());
        when(userService.getAllUsers()).thenReturn(users);
        when(paymentInfoRepo.findByUser(any(User.class))).thenReturn(payments);

        reportService.createNewReport(Period.DAILY);

        verify(userService).getAllUsers();
        verify(paymentInfoRepo).findByUser(any(User.class));
        verify(reportRepo).save(any(Report.class));
    }

    @Test
    void testCompareReports() {
        ReportService.DateAndPeriod dap = new ReportService.DateAndPeriod(1, 2023, 2, 2023, "MONTHLY");
        Report firstReport = new Report();
        Report secondReport = new Report();
        when(reportRepo.findReportsByMonthAndYear(1, 2023, Period.MONTHLY))
                .thenReturn(Collections.singletonList(firstReport));
        when(reportRepo.findReportsByMonthAndYear(2, 2023, Period.MONTHLY))
                .thenReturn(Collections.singletonList(secondReport));

        Optional<ReportComparison> comparison = reportService.compareReports(dap);

        assertTrue(comparison.isPresent());
        assertEquals(firstReport, comparison.get().getFirstReport());
        assertEquals(secondReport, comparison.get().getSecondReport());
    }

    @Test
    void testCompareReportsReturnsEmptyIfNoReportsFound() {
        ReportService.DateAndPeriod dap = new ReportService.DateAndPeriod(1, 2023, 2, 2023, "MONTHLY");
        when(reportRepo.findReportsByMonthAndYear(1, 2023, Period.MONTHLY))
                .thenReturn(Collections.emptyList());
        when(reportRepo.findReportsByMonthAndYear(2, 2023, Period.MONTHLY))
                .thenReturn(Collections.emptyList());

        Optional<ReportComparison> comparison = reportService.compareReports(dap);

        assertFalse(comparison.isPresent());
    }

    @Test
    void testDisplayDifferences() {
        Report firstReport = new Report();
        Report secondReport = new Report();
        firstReport.setTotal(100);
        secondReport.setTotal(150);
        ReportCategory category1 = new ReportCategory("Food", 50);
        ReportCategory category2 = new ReportCategory("Food", 70);
        firstReport.setReportCategories(Set.of(category1));
        secondReport.setReportCategories(Set.of(category2));

        ReportComparison comparison = new ReportComparison(firstReport, secondReport);
        ReportService.ComparisonResult result = reportService.displayDifferences(comparison);

        assertEquals(50, result.getTotalDifference());
        assertEquals(20, result.getCategoryDifferences().get("Food"));
    }
}

