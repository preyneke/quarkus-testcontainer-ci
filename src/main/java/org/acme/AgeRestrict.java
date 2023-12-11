package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;


@ApplicationScoped
public class AgeRestrict {

    private final Logger logger = Logger.getLogger(AgeRestrict.class);

    private final static String FAILED = "failed";
    private final static String PASSED = "passed";

    @Inject
    @Channel("underage")
    Emitter<Customer> underageEmitter;

    @Incoming("customers")
    public void underage(Customer customer) {
        //todo see if user exists in DB

        //update or create new customer
        logger.info("Filtering a customer: " + customer);
        if (customer.age < 20){
            underageEmitter.send(customer);
        }else {
            customer.setStatus(PASSED);
        }
        //persist customer to Postgres DB

    }

    @Incoming("underagein")
    public void underage_in(Customer customer) {
        customer.setStatus(FAILED);
        logger.info("!!Got an underage customer: " + customer);
        //todo update customer in DB

    }


}