package de.oriontec.microservice.invoicing.events;

import de.oriontec.microservice.invoicing.Invoice;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class InvoiceDeserializer extends JsonDeserializer<Invoice> {

	public InvoiceDeserializer() {
		super(Invoice.class);
	}

}
