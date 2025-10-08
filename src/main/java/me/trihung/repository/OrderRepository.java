package me.trihung.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import me.trihung.dto.EventTypeRevenueDto;
import me.trihung.dto.OrderDto;
import me.trihung.dto.RevenueDataDto;
import me.trihung.dto.TopEventDto;
import me.trihung.entity.Order;
import me.trihung.entity.User;
import me.trihung.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Lấy orders theo owner (user hiện tại)
     */
    @Query("""
        SELECT new me.trihung.dto.OrderDto(
            o.id,
            z.id,
            u.id,
            o.quantity,
            o.totalAmount,
            o.createdAt,
            z.price,
            z.name,
            e.eventName,
            p.paymentData
        )
        FROM Order o
        JOIN o.zone z
        JOIN z.event e
        JOIN o.owner u
        LEFT JOIN Payment p ON p.order = o
        WHERE u = :owner
        """)
    Page<OrderDto> findOrderDtosByOwner(@Param("owner") User owner, Pageable pageable);

    /**
     * Lấy orders theo event và status COMPLETED (có phân trang)
     */
    @Query("""
        SELECT new me.trihung.dto.OrderDto(
            o.id,
            z.id,
            u.id,
            o.quantity,
            o.totalAmount,
            o.createdAt,
            z.price,
            z.name,
            e.eventName,
            p.paymentData
        )
        FROM Order o
        JOIN o.zone z
        JOIN z.event e
        JOIN o.owner u
        LEFT JOIN Payment p ON p.order = o
        WHERE e.id = :eventId
        AND o.orderStatus = :status
        """)
    Page<OrderDto> findOrderDtosByEventIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("status") OrderStatus status,
        Pageable pageable
    );

    /**
     * Lấy orders theo event, zone và status COMPLETED (có phân trang)
     */
    @Query("""
        SELECT new me.trihung.dto.OrderDto(
            o.id,
            z.id,
            u.id,
            o.quantity,
            o.totalAmount,
            o.createdAt,
            z.price,
            z.name,
            e.eventName,
            p.paymentData
        )
        FROM Order o
        JOIN o.zone z
        JOIN z.event e
        JOIN o.owner u
        LEFT JOIN Payment p ON p.order = o
        WHERE e.id = :eventId
        AND z.id = :zoneId
        AND o.orderStatus = :status
        """)
    Page<OrderDto> findOrderDtosByEventIdAndZoneIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("zoneId") UUID zoneId,
        @Param("status") OrderStatus status,
        Pageable pageable
    );

    /**
     * Đếm số lượng orders theo event và status
     */
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        JOIN o.zone z
        WHERE z.event.id = :eventId
        AND o.orderStatus = :status
        """)
    Long countByEventIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("status") OrderStatus status
    );

    /**
     * Đếm số lượng orders theo event, zone và status
     */
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.zone.event.id = :eventId
        AND o.zone.id = :zoneId
        AND o.orderStatus = :status
        """)
    Long countByEventIdAndZoneIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("zoneId") UUID zoneId,
        @Param("status") OrderStatus status
    );

    /**
     * Tính tổng doanh thu theo event và status COMPLETED
     */
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        JOIN o.zone z
        WHERE z.event.id = :eventId
        AND o.orderStatus = :status
        """)
    Long getTotalRevenueByEventIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("status") OrderStatus status
    );

    /**
     * Tính tổng số vé đã bán theo event và status COMPLETED
     */
    @Query("""
        SELECT COALESCE(SUM(o.quantity), 0)
        FROM Order o
        JOIN o.zone z
        WHERE z.event.id = :eventId
        AND o.orderStatus = :status
        """)
    Long getTotalTicketsSoldByEventIdAndStatus(
        @Param("eventId") UUID eventId,
        @Param("status") OrderStatus status
    );

    /**
     * Lấy doanh thu theo khoảng thời gian
     */
    @Query("""
        SELECT new me.trihung.dto.RevenueDataDto(
            CAST(FUNCTION('TO_CHAR', o.createdAt, 'YYYY-MM') AS string),
            SUM(o.totalAmount),
            COUNT(o.id),
            COUNT(DISTINCT o.zone.event.id)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        AND o.orderStatus = me.trihung.enums.OrderStatus.COMPLETED
        GROUP BY FUNCTION('TO_CHAR', o.createdAt, 'YYYY-MM')
        ORDER BY FUNCTION('TO_CHAR', o.createdAt, 'YYYY-MM') ASC
        """)
    List<RevenueDataDto> findRevenueDataByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Lấy doanh thu theo loại sự kiện
     */
    @Query("""
        SELECT new me.trihung.dto.EventTypeRevenueDto(
            o.zone.event.eventCategory,
            SUM(o.quantity),
            SUM(o.totalAmount)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        AND o.orderStatus = me.trihung.enums.OrderStatus.COMPLETED
        AND (:eventType IS NULL OR o.zone.event.eventCategory = :eventType)
        GROUP BY o.zone.event.eventCategory
        """)
    List<EventTypeRevenueDto> findEventTypeRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("eventType") String eventType
    );

    /**
     * Lấy các sự kiện hàng đầu theo doanh thu
     */
    @Query(value = """
        SELECT
            e.event_name as name,
            SUM(o.total_amount) as revenue,
            SUM(o.quantity) as tickets,
            CASE
                WHEN (e.start_date + e.start_time) < NOW() THEN 'completed'
                WHEN (e.start_date + e.start_time) > NOW() THEN 'upcoming'
                ELSE 'active'
            END as status
        FROM orders o
        JOIN zones z ON o.zone_id = z.id
        JOIN events e ON z.event_id = e.id
        WHERE o.created_at BETWEEN :startDate AND :endDate
        AND o.order_status = 'COMPLETED'
        GROUP BY e.event_name, e.start_date, e.start_time
        ORDER BY revenue DESC
        """, nativeQuery = true)
    List<TopEventDto> findTopEvents(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Cập nhật tất cả order có reservation hết hạn -> đổi trạng thái thành FAILED
     */
    @Transactional
    @Modifying
    @Query("""
        UPDATE Order o
        SET o.orderStatus = :status
        WHERE o.payment IS NOT NULL
          AND EXISTS (
              SELECT r FROM Reservation r
              WHERE r.order = o
                AND r.expiresAt < :now
          )
        """)
    int markOrdersAsFailedIfExpired(
        @Param("now") LocalDateTime now,
        @Param("status") OrderStatus status
    );
}