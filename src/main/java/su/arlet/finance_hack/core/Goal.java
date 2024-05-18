package su.arlet.finance_hack.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Goal {
    @Id
    private long id;
    private long sum;
    private LocalDate deadline;
    private long currentTotal;
    private String name;
    private String description;
    private boolean isDone;
    @ManyToOne
    private User user;

}
