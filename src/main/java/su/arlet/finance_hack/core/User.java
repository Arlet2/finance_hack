package su.arlet.finance_hack.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Column(name = "hashPassword", nullable = false)
    private String hashPassword;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "current_wastings", nullable = false)
    private long currentWastings;

    @ManyToOne
    private Goal[] goals;

    @ManyToOne
    private Report[] reports;

    @Column(name = "user_limit", nullable = false)
    private long limit;

}
