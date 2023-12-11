package org.acme.services;

import org.acme.Customer;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgeRestrictionValidatorService {

    //create a new method that takes in a Customer and verify if he is older than 21

    public boolean validateCustomerAge(Customer customer){
        boolean underage;

        if (customer.age < 20){
            underage = true;
        }else {
           underage = false;
        }
        return underage;
    }

    public boolean validateMaxAge(Customer customer){
        if (customer.age > 80){
            return true;
        }
        return false;
    }

}
