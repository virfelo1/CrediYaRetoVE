package co.com.projectve.r2dbc.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user")
@Data
public class UserEntity {
    @Id
    @Column("id")
    private String id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("address")
    private String address;

    @Column("phone_number")
    private String phoneNumber;

    @Column("email")
    private String email;

    @Column("base_salary")
    private BigDecimal baseSalary;
}

