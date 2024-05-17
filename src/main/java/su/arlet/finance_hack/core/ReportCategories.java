package su.arlet.finance_hack.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportCategories {
    @Id
    private long report_id;
    @Id
    private String category; // todo: change to Category
    private long sum;
}
