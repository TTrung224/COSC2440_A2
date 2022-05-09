package com.assignment.taxiCom.service;

import com.assignment.taxiCom.entity.Booking;
import com.assignment.taxiCom.repository.BookingRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class BookingService {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private BookingRepository bookingRepository;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public BookingRepository getBookingRepository() {
        return bookingRepository;
    }

    public void setBookingRepository(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public long addBooking(Booking booking){
        sessionFactory.getCurrentSession().saveOrUpdate(booking);
        return booking.getId();
    }

    public Booking updateBooking(Booking booking){
        sessionFactory.getCurrentSession().update(booking);
        return booking;
    }

    public long deleteBooking(Booking booking){
        sessionFactory.getCurrentSession().delete(booking);
        return booking.getId();
    }

    public Page<Booking> getAllBooking(int page, int pageSize){
        Page<Booking> bookings = bookingRepository.findAll(PageRequest.of(page, pageSize, Sort.by("id").ascending()));
        return bookings;
    }

    public Booking getBookingById(Long id){
        return sessionFactory.getCurrentSession().get(Booking.class, id);
    }

    public Booking getBookingByInvoiceId(Long invoiceId){
        return bookingRepository.findBookingByInvoiceId(invoiceId);
    }

    public Page<Booking> filterBookingByCreatedTime(String strStart, String strEnd, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("dateCreated").ascending());
        ZonedDateTime start = ZonedDateTime.parse(strStart, dateFormatter);
        ZonedDateTime end = ZonedDateTime.parse(strEnd, dateFormatter);
        Page<Booking> bookings = bookingRepository.filterBookingByCreatedTime(start, end, pageable);
        return bookings;
    }

    public Page<Booking> filterBookingByPickUpTime(String strStart, String strEnd, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("pickUpTime").ascending());
        ZonedDateTime start = ZonedDateTime.parse(strStart, dateFormatter);
        ZonedDateTime end = ZonedDateTime.parse(strEnd, dateFormatter);
        Page<Booking> bookings = bookingRepository.filterBookingByPickUpTime(start, end, pageable);
        return bookings;
    }

    public Page<Booking> filterBookingByDropOffTime(String strStart, String strEnd, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("dropOffTime").ascending());
        ZonedDateTime start = ZonedDateTime.parse(strStart, dateFormatter);
        ZonedDateTime end = ZonedDateTime.parse(strEnd, dateFormatter);
        Page<Booking> bookings = bookingRepository.filterBookingByDropOffTime(start, end, pageable);
        return bookings;
    }

    public Page<Booking> filterBookingByDistance(double min, double max, int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("distance").ascending());
        Page<Booking> bookings = bookingRepository.filterBookingByDistance(min, max, pageable);
        return bookings;
    }

    public Page<Booking> findBookingByStartLocation(String location, int page, int pageSize){
        location = location.toUpperCase();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Booking> bookings = bookingRepository.findBookingByStartLocation(location, pageable);
        return bookings;
    }

    public Page<Booking> findBookingByEndLocation(String location, int page, int pageSize){
        location = location.toUpperCase();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Booking> bookings = bookingRepository.findBookingByEndLocation(location, pageable);
        return bookings;
    }
}