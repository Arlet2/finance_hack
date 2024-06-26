package su.arlet.finance_hack.core;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportCategory {
    @Id
    private String category; // todo: change to Category
    private long sum;
}
