package com.gsp.aiims.invoice.repository;

import com.gsp.aiims.common.enums.InvoiceStatus;
import com.gsp.aiims.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>,
        JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByIdAndDeletedFalse(Long id);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("""
            SELECT i FROM Invoice i
            WHERE i.deleted = false
            AND (:status IS NULL OR i.status = :status)
            AND (:customerId IS NULL OR i.customer.id = :customerId)
            AND (:search IS NULL
                 OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Invoice> search(
            @Param("search") String search,
            @Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.deleted = false AND i.status IN :statuses")
    List<Invoice> findByStatusIn(@Param("statuses") List<InvoiceStatus> statuses);

    @Query("SELECT i FROM Invoice i WHERE i.deleted = false AND i.status = :status AND i.dueDate < :today")
    List<Invoice> findOverdue(@Param("status") InvoiceStatus status, @Param("today") LocalDate today);

    // ---- reporting queries ----

    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i
            WHERE i.deleted = false
            AND i.status NOT IN ('DRAFT', 'CANCELLED')
            AND year(i.issueDate) = :year AND month(i.issueDate) = :month
            """)
    BigDecimal sumRevenueByMonth(@Param("year") int year, @Param("month") int month);

    @Query("""
            SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i
            WHERE i.deleted = false
            AND i.status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')
            """)
    BigDecimal sumOutstandingAmount();

    @Query("""
            SELECT i.customer.id, i.customer.name, SUM(i.totalAmount) as revenue
            FROM Invoice i
            WHERE i.deleted = false AND i.status NOT IN ('DRAFT', 'CANCELLED')
            GROUP BY i.customer.id, i.customer.name
            ORDER BY revenue DESC
            """)
    List<Object[]> findTopCustomersByRevenue(Pageable pageable);

    @Query("""
            SELECT year(i.issueDate), month(i.issueDate), SUM(i.totalAmount)
            FROM Invoice i
            WHERE i.deleted = false AND i.status NOT IN ('DRAFT', 'CANCELLED')
            AND i.issueDate >= :from
            GROUP BY year(i.issueDate), month(i.issueDate)
            ORDER BY year(i.issueDate), month(i.issueDate)
            """)
    List<Object[]> findMonthlyRevenue(@Param("from") LocalDate from);
}
