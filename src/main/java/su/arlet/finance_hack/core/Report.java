package su.arlet.finance_hack.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.arlet.finance_hack.core.enums.Period;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Timestamp created;
    @Enumerated(EnumType.STRING)
    private Period period;
    private long total;
    @ElementCollection
    @CollectionTable
    private Set<ReportCategory> reportCategories = new HashSet<>();
    @ManyToOne
    private User user;
}
