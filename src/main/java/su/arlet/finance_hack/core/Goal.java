package su.arlet.finance_hack.core;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long sum;
    private LocalDate deadline;
    private long currentTotal;
    private String name;
    private String description;
    private boolean isDone;
    private long priority;
    @ManyToOne
    private User user;

}
