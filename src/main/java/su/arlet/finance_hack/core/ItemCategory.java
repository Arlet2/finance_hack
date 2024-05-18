package su.arlet.finance_hack.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
