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
@Table(name = "users")
public class User {
    @Id
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "hashPassword", nullable = false)
    private String hashPassword;


    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;
    @Column(name = "email")
    private String email;

    @ManyToOne
    private Goal[] goals;

    @ManyToOne
    private Report[] reports;

    private long limit;
}
