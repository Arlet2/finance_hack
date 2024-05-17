package su.arlet.finance_hack.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class ItemCategory {

    @Id
    private Long id;

    @Column(unique = true)
    private String name;
}
