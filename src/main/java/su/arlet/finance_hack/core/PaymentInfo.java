package su.arlet.finance_hack.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.arlet.finance_hack.core.enums.PaymentType;

import java.sql.Timestamp;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {

    @Id
    private Long id;

    private Long sum;

    private Timestamp time;

    private String companyName;

    @ManyToOne
    private ItemCategory itemCategory;


    private PaymentType paymentType;

}
