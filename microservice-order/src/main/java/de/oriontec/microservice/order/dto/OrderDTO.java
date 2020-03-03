package de.oriontec.microservice.order.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = false)
public class OrderDTO {

  private long customerId;
  private AddressDTO shippingAddress;
  private AddressDTO billingAddress;
  private List<OrderLineDTO> orderLines;
}
