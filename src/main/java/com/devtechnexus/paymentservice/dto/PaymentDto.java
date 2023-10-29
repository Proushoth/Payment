package com.devtechnexus.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDto {
    private int oid;
    private String user;
    private double price;
    private String currency;
    private String method;
    private String intent;
    private String description;


}
