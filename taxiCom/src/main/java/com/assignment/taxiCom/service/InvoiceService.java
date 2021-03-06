package com.assignment.taxiCom.service;

import com.assignment.taxiCom.entity.*;
import com.assignment.taxiCom.repository.InvoiceRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.PreUpdate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class InvoiceService {
    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarService carService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    public InvoiceRepository getInvoiceRepository() {
        return invoiceRepository;
    }

    public void setInvoiceRepository(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public DriverService getDriverService() {
        return driverService;
    }

    public void setDriverService(DriverService driverService) {
        this.driverService = driverService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public CarService getCarService() {
        return carService;
    }

    public void setCarService(CarService carService) {
        this.carService = carService;
    }

    public ResponseEntity<?> addInvoice(Invoice invoice, long bookingID, long customerID, long carID){
        Booking booking = bookingService.getBookingById(bookingID);
        Customer customer = customerService.getCustomerByID(customerID);
        Car car = carService.getCarById(carID);
        if (booking == null){return new ResponseEntity<>("Booking does not exist", HttpStatus.BAD_REQUEST);}
        if(bookingService.getBookingById(bookingID).getInvoice()!=null){
            return new ResponseEntity<>("Booking has already had invoice", HttpStatus.FORBIDDEN);
        }
        if (customer == null){return new ResponseEntity<>("Customer does not exist", HttpStatus.BAD_REQUEST);}
        if (car == null){return new ResponseEntity<>("Car does not exist", HttpStatus.BAD_REQUEST);}
        Driver driver = car.getDriver();
        if (driver == null){return new ResponseEntity<>("Car does not have a driver", HttpStatus.BAD_REQUEST);}
        booking.setInvoice(invoice);
        invoice.setBooking(booking);
        invoice.setCustomer(customer);
        invoice.setDriver(driver);
        invoice.setTotalCharge(booking.getDistance() * invoice.getDriver().getCar().getRatePerKilometer());

        sessionFactory.getCurrentSession().save(invoice);
        return new ResponseEntity<>(String.format("Invoice with ID %1$s is added (%2$s)", invoice.getId(), invoice.getDateCreated()), HttpStatus.OK);
    }

    public ResponseEntity<?> updateInvoice(Invoice invoice, long customerID, long driverID, long bookingID){
        Invoice unupdatedInvoice = getInvoiceByID(invoice.getId());
        unupdatedInvoice.getBooking().setInvoice(null);
        Customer customer = customerService.getCustomerByID(customerID);
        Driver driver = driverService.getDriverById(driverID);
        Booking booking = bookingService.getBookingById(bookingID);
        if(customer == null){
            return new ResponseEntity<>("Customer does not exist", HttpStatus.BAD_REQUEST);
        }
        if(booking == null){
            return new ResponseEntity<>("Booking does not exist", HttpStatus.BAD_REQUEST);
        }
        if(driver == null){
            return new ResponseEntity<>("Driver does not exist", HttpStatus.BAD_REQUEST);
        }
        unupdatedInvoice.setCustomer(customer);
        unupdatedInvoice.setDriver(driver);
        unupdatedInvoice.setBooking(booking);
        unupdatedInvoice.setTotalCharge(unupdatedInvoice.getBooking().getDistance() * unupdatedInvoice.getDriver().getCar().getRatePerKilometer());

        unupdatedInvoice.getBooking().setInvoice(unupdatedInvoice);
        sessionFactory.getCurrentSession().update(unupdatedInvoice);
        return new ResponseEntity<>(String.format("Invoice with ID %s has been updated", unupdatedInvoice.getId()), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteInvoice(long invoiceId){
        Invoice invoice = getInvoiceByID(invoiceId);
        if(invoice == null){
            return new ResponseEntity<>("Invoice does not exist",HttpStatus.BAD_REQUEST);
        }
        invoice.getBooking().setInvoice(null);
        sessionFactory.getCurrentSession().delete(invoice);
        return new ResponseEntity<>("Invoice has been deleted", HttpStatus.OK);
    }

    public Page<Invoice> getAllInvoice(int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Invoice> invoices = invoiceRepository.findAll(pageable);
        return invoices;
    }

    public Invoice getInvoiceByID(long id){
        return sessionFactory.getCurrentSession().get(Invoice.class, id);
    }

    public Page<Invoice> filterInvoiceByPeriod(String strStart,String strEnd, int page, int pageSize){
        ZonedDateTime startDay = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime enDay = ZonedDateTime.parse(strEnd,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Invoice> invoices = invoiceRepository.filterInvoiceByPeriod(startDay,enDay,pageable);
        return invoices;
    }

    public double getCustomerRevenueByPeriod(long customerId,String strStart,String strEnd){
        ZonedDateTime startDay = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime enDay = ZonedDateTime.parse(strEnd,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        return invoiceRepository.getCustomerRevenueByPeriod(customerId,startDay,enDay);
    }

    public double getDriverRevenueByPeriod(long driverId,String strStart,String strEnd){
        ZonedDateTime startDay = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime enDay = ZonedDateTime.parse(strEnd,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        return invoiceRepository.getDriverRevenueByPeriod(driverId,startDay,enDay);
    }

    public Page<Invoice> getCustomerInvoiceByPeriod(long customerId,String strStart,String strEnd, int page, int pageSize){
        ZonedDateTime startDay = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime enDay = ZonedDateTime.parse(strEnd,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Invoice> invoices = invoiceRepository.getCustomerInvoiceByPeriod(customerId,startDay,enDay,pageable);
        return invoices;
    }

    public Page<Invoice> getDriverInvoiceByPeriod(long driverId,String strStart,String strEnd, int page, int pageSize){
        ZonedDateTime startDay = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime enDay = ZonedDateTime.parse(strEnd,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
        Page<Invoice> invoices = invoiceRepository.getDriverInvoiceByPeriod(driverId,startDay,enDay,pageable);
        return invoices;
    }


}
