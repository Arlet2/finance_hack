package su.arlet.finance_hack.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import su.arlet.finance_hack.core.Report;
import su.arlet.finance_hack.core.enums.Period;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ReportRepo extends JpaRepository<Report, Long> {

    List<Report> findByCreated(Timestamp created);

    List<Report> findAllByCreatedBetween(Timestamp startDate, Timestamp endDate);

    @Query("SELECT r FROM Report r WHERE FUNCTION('MONTH', r.created) = ?1 AND FUNCTION('YEAR', r.created) = ?2 AND r.period = ?3")
    List<Report> findReportsByMonthAndYear(int month, int year, Period period);
}
