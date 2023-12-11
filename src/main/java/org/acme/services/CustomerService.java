package org.acme.services;

import org.acme.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CustomerService {

    @Inject
    AgeRestrictionValidatorService ageRestrictionValidatorService;

    public Customer validateCustomerAgeStatus(Customer customer){

        return ageRestrictionValidatorService.validateCustomerAge(customer);
    }

    public void setCustomerStatusAsUnderAge(Customer customer) {
        //todo
    }
}
