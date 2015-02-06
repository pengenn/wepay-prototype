package com.pst.infusionpay;

import com.lookfirst.wepay.WePayApi;
import com.lookfirst.wepay.WePayKey;
import com.lookfirst.wepay.api.*;
import com.lookfirst.wepay.api.req.*;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: peng.lim
 * Date: 2/5/2015
 * Time: 3:40 PM
 */
public class WePayClient {

    private static String CODE = "d1c0606f23e4f3e4e880a162ebac7da58d3346ea4266e4ef4b";
    private static String ACCESS_TOKEN = "STAGE_797a20629532af6a8c1abef6192cb37523475a11b09eee9e526d8ed9ab4f6721";
    private static String REDIRECT_URL = "http://localhost/wepay";
    private static long CLIENT_ID = 6111L;
    private WePayApi api;

    public WePayClient(Long clientId, String secret, boolean isProduction) {
         api =  new WePayApi(new WePayKey(isProduction, clientId, secret));
    }

    public static void main(String[] args) throws IOException{
     // Get an instance of the API. It is threadsafe.
        WePayClient client = new WePayClient(CLIENT_ID, "9d530fe465", false);
//        client.createCreditCardId();
//        ccid = 29698557
    // Generate a auth url
//    String url = api.getAuthorizationUri(WePayApi.Scope.getAll(),REDIRECT_URL, null);
//                                     System.out.println(url);
    // Use the code to generate a token.
//        Token token = api.getToken(CODE, REDIRECT_URL);
//      System.out.println(token.getAccessToken());
                       System.out.println(client.capture(1251642942));
//        System.out.println(client.getCheckoutDetails(1251642942));

    }


    public void viewUser() throws IOException {
        WePayUser user = api.execute(ACCESS_TOKEN, new UserRequest());
        System.out.println(user);

    }

    public String createCreditCardId() throws IOException {
        CreditCardCreateRequest ccRequest = new CreditCardCreateRequest();
        Checkout.ShippingAddress address = new Checkout.ShippingAddress();
        address.setAddress1("123 test st.");
        address.setCity("Gilbert");
        address.setCountry("US");
        address.setState("AZ");
        address.setZip("85233");

        ccRequest.setAddress(address);
        ccRequest.setCcNumber(4003830171874018L);
        ccRequest.setClientId(CLIENT_ID);
        ccRequest.setCvv(999);
        ccRequest.setEmail("testpayer@infusionhole.com");
        ccRequest.setExpirationMonth(12);
        ccRequest.setExpirationYear(2019);
        ccRequest.setUserName("TEST A. LOT");

        CreditCard creditCard = api.execute(ACCESS_TOKEN, ccRequest);

        return creditCard.getCreditCardId();
    }

    public void createCheckout() throws IOException {
        CheckoutCreateRequest createRequest = new CheckoutCreateRequest();
        createRequest.setAccountId(143865178L);
        createRequest.setShortDescription("A test charge");
        createRequest.setType(Constants.PaymentType.GOODS);
        createRequest.setAmount(new BigDecimal(10.50));
        createRequest.setPaymentMethodType("credit_card");
        createRequest.setPaymentMethodId(29698557L);
        CheckoutUri chargeResponse = api.execute(ACCESS_TOKEN, createRequest);
        System.out.println(chargeResponse);

    }

    public CheckoutState capture(long checkoutid) throws IOException {
        CheckoutCaptureRequest captureRequest = new CheckoutCaptureRequest();
        captureRequest.setCheckoutId(checkoutid);
        return api.execute(ACCESS_TOKEN, captureRequest);
    }

    public Checkout getCheckoutDetails(long checkoutId) throws IOException {

        CheckoutRequest request = new CheckoutRequest();
        request.setCheckoutId(checkoutId);
        return api.execute(ACCESS_TOKEN, request);
    }
}
