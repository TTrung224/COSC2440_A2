package com.assignment.taxiCom.service;

import com.assignment.taxiCom.entity.Customer;
import com.assignment.taxiCom.entity.Invoice;
import com.assignment.taxiCom.repository.CustomerRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Service
@Transactional
public class CustomerService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CustomerRepository customerRepository;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory(){
        return sessionFactory;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Page<Customer> getAllCustomers(int page, int pageSize){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by("dateCreated").ascending());
        return (Page<Customer>) customerRepository.findAll(pageable);
    }

    public ResponseEntity<?> addCustomer(Customer customer){
        sessionFactory.getCurrentSession().saveOrUpdate(customer);
        return new ResponseEntity<>(String.format("Customer with ID %1$s is added (%2$s)", customer.getId(), customer.getDateCreated()), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCustomer(Customer customer){
        sessionFactory.getCurrentSession().update(customer);
        return new ResponseEntity<>(String.format("Customer with ID %s has been updated", customer.getId()), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteCustomer(long customerId){
        Customer customer = getCustomerByID(customerId);
        if(customer == null){
            return new ResponseEntity<>("Customer not found", HttpStatus.BAD_REQUEST);
        }
        if (customer.getInvoice() != null){
            for(Invoice invoice : customer.getInvoice()){
                invoice.getBooking().setInvoice(null);
                sessionFactory.getCurrentSession().delete(invoice);
            }
        }
        sessionFactory.getCurrentSession().delete(customer);
        return new ResponseEntity<>("Customer has been deleted with all associated invoices", HttpStatus.OK);
    }

    public Customer getCustomerByID(long ID){
        return sessionFactory.getCurrentSession().get(Customer.class,ID);
    }

    public Customer getCustomerByPhone(String phone){
        return customerRepository.findCustomerByPhone(phone);
    }

    public Page<Customer> filterCustomerByCreatedTime(String strStart, String strEnd, int page, int pageSize){
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by("dateCreated").ascending());
        ZonedDateTime start = ZonedDateTime.parse(strStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        ZonedDateTime end = ZonedDateTime.parse(strEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        Page<Customer> customers = customerRepository.filterCustomerByCreatedTime(start, end, pageable);
        return customers;
    }

    public Page<Customer> filterCustomerByName(String name, int page, int pageSize){
        name = name.toUpperCase();
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by("name").ascending());
        Page<Customer> customers = customerRepository.filterCustomerByName(name, pageable);
        return customers;
    }

    public Page<Customer> filterCustomerByAddress(String address, int page, int pageSize){
        address = address.toUpperCase();
        Pageable pageable = PageRequest.of(page,pageSize, Sort.by("address").ascending());
        Page<Customer> customers = customerRepository.filterCustomerByAddress(address, pageable);
        return customers;
    }

}
