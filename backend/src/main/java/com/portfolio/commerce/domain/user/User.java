package com.portfolio.commerce.domain.user;

import com.portfolio.commerce.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Customer of the platform.
 *
 * The {@code vip} flag identifies VIP customers. Credentials and roles
 * live in a separate auth_accounts table.
 */
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "vip", nullable = false)
    private boolean vip;

    protected User() {
        // required by JPA
    }

    public User(String dni, String firstName, String lastName, boolean vip) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.vip = vip;
    }

    public Long getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isVip() {
        return vip;
    }
}
