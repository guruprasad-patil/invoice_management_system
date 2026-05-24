package com.gsp.aiims.customer.entity;

import com.gsp.aiims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String company;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(length = 100)
    private String country;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
