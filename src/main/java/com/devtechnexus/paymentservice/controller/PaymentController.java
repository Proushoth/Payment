package com.devtechnexus.paymentservice.controller;

import com.devtechnexus.paymentservice.dto.PaymentDto;
import com.devtechnexus.paymentservice.service.LedgerService;
import com.devtechnexus.paymentservice.service.PaymentService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping(path = "/payments")
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private LedgerService ledgerService;


    private static final String LOCAL = "http://localhost:8084/payments/";
    private static final String HOST = LOCAL;
    private static final String SUCCESS_URL = "success";
    private static final String CANCEL_URL = "cancel";

    @PostMapping(path = "/process")
    public String createPayment(@RequestBody PaymentDto payment) {


        try {
            //access paypal api to create a payment
            Payment paymentResponse = paymentService.createPayment(payment.getPrice(),
                    payment.getCurrency(),
                    payment.getMethod(),
                    payment.getIntent(),
                    payment.getDescription(),
                    HOST + CANCEL_URL,
                    HOST + SUCCESS_URL
            );

            System.out.println(paymentResponse.getId());
            //create ledger entry
            ledgerService.createLedgerEntry(paymentResponse.getId(), payment);


            for (Links link : paymentResponse.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    //sends user to PayPal login page
                    return
                            //"redirect:" +
                            link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "http://localhost:5000/cart";
    }


    @GetMapping(SUCCESS_URL)
    public String success(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerID, HttpServletResponse httpServletResponse) {
        System.out.println(paymentId);
        try {
            Payment payment = paymentService.executePayment(paymentId, payerID);

            if (payment.getState().equals("approved")) {
                //update ledger entry
                int orderId = ledgerService.successLedgerEntry(paymentId);

                //contact delivery service to update order status to PAID
                HttpRequest request = HttpRequest.newBuilder().
                        uri(new URI("http://localhost:8083/deliveries/" + orderId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(""))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.statusCode());
                System.out.println(response.body());

                return "success";

            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "error";
    }

    @GetMapping(CANCEL_URL)
    public String cancel() {
        return "cancel";
    }


}
