package com.assignment.taxiCom.repository;

import com.assignment.taxiCom.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Repository
public interface BookingRepository extends PagingAndSortingRepository<Booking, Integer> {

    Page<Booking> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE b.dateCreated >= ?1 AND b.dateCreated <= ?2", nativeQuery = true)
    Page<Booking> filterBookingByCreatedTime(ZonedDateTime startTime, ZonedDateTime endTime, Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE b.pickUpTime >= ?1 AND b.pickUpTime <= ?2", nativeQuery = true)
    Page<Booking> filterBookingByPickUpTime(ZonedDateTime startTime, ZonedDateTime endTime, Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE b.dropOffTime >= ?1 AND b.dropOffTime <= ?2", nativeQuery = true)
    Page<Booking> filterBookingByDropOffTime(ZonedDateTime startTime, ZonedDateTime endTime, Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE b.distance >= ?1 AND b.distance <= ?2", nativeQuery = true)
    Page<Booking> filterBookingByDistance(double min, double max, Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE b.invoiceId = ?1", nativeQuery = true)
    Booking findBookingByInvoiceId(long invoiceId);

    @Query(value = "SELECT * FROM booking b WHERE UPPER(b.startingLocation) LIKE %?1%", nativeQuery = true)
    Page<Booking> findBookingByStartLocation(String startingLocation, Pageable pageable);

    @Query(value = "SELECT * FROM booking b WHERE UPPER(b.endLocation) LIKE %?1%", nativeQuery = true)
    Page<Booking> findBookingByEndLocation(String endLocation, Pageable pageable);
}
