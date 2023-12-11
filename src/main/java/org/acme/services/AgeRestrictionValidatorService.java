package org.acme.services;

import org.acme.Customer;

import javax.enterprise.context.ApplicationScoped;


import static org.acme.enums.AgeStatus.*;

@ApplicationScoped
public class AgeRestrictionValidatorService {


    //create a new method that takes in a Customer and verify if he is older than 21

    public Customer validateCustomerAge(Customer customer){
        boolean underage;

        if (customer.age < 20){
            customer.setStatus(FAILED);
        }else {
           customer.setStatus(PASSED);
        }
        return customer;
    }

}
