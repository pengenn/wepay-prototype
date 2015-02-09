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
    private static long OAUTH_CLIENT_ID = 6111L;
    private static long OAUTH_SECRET = 6111L;
    private WePayApi api;

    public WePayClient(Long clientId, String secret, boolean isProduction) {
         api =  new WePayApi(new WePayKey(isProduction, clientId, secret));
    }

    public static void main(String[] args) throws IOException{

     // Get an instance of the API. It is threadsafe.
     WePayClient client = new WePayClient(OAUTH_CLIENT_ID, "9d530fe465", false);
//        CheckoutUri checkoutResponse =  client.createCheckout();
//        System.out.println(checkoutResponse);
//        CheckoutState state = client.refund(2028095370L, "Testing full refund of a pending payment", null);
        CheckoutState state = client.voidPayment(2028095370L, "Testing void");
        System.out.println(state.getCheckoutId() + ";" + state.getState());

    }

    public String registerMerchant() {
        WePayClient client = new WePayClient(OAUTH_CLIENT_ID, "9d530fe465", false);
        return api.getAuthorizationUri(WePayApi.Scope.getAll(),REDIRECT_URL, null);
    }

    public String getAccessToken(String code) throws IOException {
        // Use the code to generate a token.
        Token token = api.getToken(code, REDIRECT_URL);
        return token.getAccessToken();
    }

    public WePayUser viewUser(String accesToken) throws IOException {
        return api.execute(accesToken, new UserRequest());
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
        ccRequest.setClientId(OAUTH_CLIENT_ID);
        ccRequest.setCvv(999);
        ccRequest.setEmail("testpayer@infusionhole.com");
        ccRequest.setExpirationMonth(12);
        ccRequest.setExpirationYear(2019);
        ccRequest.setUserName("TEST A. LOT");

        CreditCard creditCard = api.execute(ACCESS_TOKEN, ccRequest);

        return creditCard.getCreditCardId();
    }

    public CheckoutState refund(Long checkoutId, String reason, BigDecimal amount) throws IOException {
        CheckoutRefundRequest refundRequest = new  CheckoutRefundRequest();
        refundRequest.setCheckoutId(checkoutId);
        refundRequest.setRefundReason(reason);
        if(amount != null) {
            refundRequest.setAmount(amount);
        }
        return api.execute(ACCESS_TOKEN, refundRequest);

        /*

        Refunds can only be made 180 days after the initial payment was captured.

        Successful atempt gets a state=captured.

         If attempting to refund after it is refunded, you get this exception messae:
         "invalid_request: Checkout object must be in state captured. Currently it is in state refunded"

          If attempting to partial refund more than available it is refunded, you get this exception message:
          "processing_error: You may not refund more than the non-refunded balance of the payment"


          If attempting to refund when payment is pending, you get this exception messae:
           "invalid_request: Checkout object must be in state captured. Currently it is in state reserved"
         */
    }

    public CheckoutUri createCheckout() throws IOException {
        CheckoutCreateRequest createRequest = new CheckoutCreateRequest();
        createRequest.setAccountId(143865178L);
        createRequest.setShortDescription("A test charge2");
        createRequest.setType(Constants.PaymentType.GOODS);
        createRequest.setAmount(new BigDecimal(1.00));
        createRequest.setPaymentMethodType("credit_card");
        createRequest.setPaymentMethodId(29698557L);
//        createRequest.setAutoCapture(true);


        return api.execute(ACCESS_TOKEN, createRequest);

        /*
        Checkouts expire 30 minutes after they are created if there is no activity on them (e.g. they were abandoned after creation).
         */


    }

    public CheckoutState capture(long checkoutid) throws IOException {
        CheckoutCaptureRequest captureRequest = new CheckoutCaptureRequest();
        captureRequest.setCheckoutId(checkoutid);
        return api.execute(ACCESS_TOKEN, captureRequest);

        /*
        If auto_capture was set to false when the checkout was created, you will need to make this call to release funds to the account.
        Until you make this call the money will be held by WePay and if you do not capture the funds within 14 days then the payment will be
        automatically cancelled or refunded. You can only make this call if the checkout is in state 'reserved'.
         */
    }


    public Checkout viewCheckout(long checkoutId) throws IOException {

        CheckoutRequest request = new CheckoutRequest();
        request.setCheckoutId(checkoutId);
        return api.execute(ACCESS_TOKEN, request);
    }

    public CheckoutState voidPayment(Long checkoutId, String reason) throws IOException {
     /*
     Checkout must be in "authorized" or "reserved" state.

          Successful atempt gets a state=cancelled.

          Voiding a checkout which has already been voided
          "invalid_request: this checkout has already been cancelled"
      */

        CheckoutCancelRequest cancelRequest = new CheckoutCancelRequest();
        cancelRequest.setCheckoutId(checkoutId);
        cancelRequest.setCancelReason(reason);
        return api.execute(ACCESS_TOKEN, cancelRequest);
    }
}
