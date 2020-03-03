package de.oriontec.microservice.order.exception;

public class CustomerNotFoundException extends Exception{

  public CustomerNotFoundException(String s) {
    super(s);
  }
}
