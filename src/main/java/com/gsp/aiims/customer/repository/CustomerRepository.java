package com.gsp.aiims.customer.repository;

import com.gsp.aiims.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalseAndIdNot(String email, Long id);

    @Query("""
            SELECT c FROM Customer c
            WHERE c.deleted = false
            AND (:search IS NULL
                 OR LOWER(c.name)    LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.email)   LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.company) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.phone)   LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Customer> searchActive(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.deleted = false")
    long countActive();
}
