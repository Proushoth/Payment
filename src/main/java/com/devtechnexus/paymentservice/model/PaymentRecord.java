package com.devtechnexus.paymentservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.ZoneId;

@Data
@Entity
@Table(name="payment_record")
public class PaymentRecord {

    public PaymentRecord() {
    }

    public PaymentRecord(String uid, int oid, double amount, Timestamp datetime, String status, String payment_id, String currency, String description) {
        this.user = uid;
        this.orderId = oid;
        this.amount = amount;
        this.datetime = datetime;
        this.status = status;
        this.paymentId = payment_id;
        this.currency = currency;
        this.description = description;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="user_id")
    private String user;

    @Column(name="order_id")
    private int orderId;

    @Column(name="amount")
    private double amount;

    @Column(name="datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp datetime;

    @Column(name="status")
    private String status;

    @Column(name="payment_id")
    private String paymentId;

    @Column(name="currency")
    private String currency;

    @Column(name="description")
    private String description;

    public String parseTimestamp() {
        return datetime.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime() + " UTC";

    }
}
