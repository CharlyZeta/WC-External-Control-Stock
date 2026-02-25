package com.stockdual.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;
}
