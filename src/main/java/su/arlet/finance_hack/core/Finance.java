package su.arlet.finance_hack.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Finance {
    @Id
    private int id;
}
