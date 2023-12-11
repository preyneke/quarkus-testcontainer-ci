package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.enums.AgeStatus;
import org.acme.services.AgeRestrictionValidatorService;
import org.acme.services.CustomerService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;


@ApplicationScoped
public class AgeRestrict {

    private final Logger logger = Logger.getLogger(AgeRestrict.class);



    @Inject
    @Channel("underage")
    Emitter<Customer> underageEmitter;
    @Inject
    CustomerService customerService;

    @Incoming("customers")
    public void underage(Customer customer) {
        //todo see if user exists in DB

        //update or create new customer
        logger.info("Filtering a customer: " + customer);
        customer = customerService.validateCustomerAgeStatus(customer);
        if(customer.status.equals(AgeStatus.FAILED)){
            underageEmitter.send(customer);
        }

        logger.info("customer age status: {}" +  customer.status);


        //persist customer to Postgres DB

    }

    @Incoming("underagein")
    public void underage_in(Customer customer) {
       customerService.setCustomerStatusAsUnderAge(customer);
        logger.info("!!Got an underage customer: " + customer);
        //todo update customer in DB

    }


}