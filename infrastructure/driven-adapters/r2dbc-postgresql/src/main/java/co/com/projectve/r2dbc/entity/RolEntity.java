package co.com.projectve.r2dbc.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rol")
@Data
public class RolEntity {

    @Id
    @Column("id_rol")
    private short idRol;

    @Column("name_rol")
    private String nameRol;

    @Column("description")
    private String description;

}
