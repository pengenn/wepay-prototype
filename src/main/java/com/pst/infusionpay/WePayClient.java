package com.pst.infusionpay;

import com.lookfirst.wepay.WePayApi;
import com.lookfirst.wepay.WePayException;
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
    private static String ACCESS_TOKEN = "STAGE_7b5c544888bbd86bce83a7bc6a37e59b3eee50815349bb2229de2f1f939fad7d";
    private static String REDIRECT_URL = "http://localhost/wepay";
    private static long OAUTH_CLIENT_ID = 6111L;
    private static String  OAUTH_SECRET = "9d530fe465";
    private WePayApi api;

    public WePayClient(Long clientId, String secret, boolean isProduction) {
         api =  new WePayApi(new WePayKey(isProduction, clientId, secret));
    }

    public static void main(String[] args) {
                                             try {
                                                 // Get an instance of the API. It is threadsafe.
                                                 WePayClient client = new WePayClient(OAUTH_CLIENT_ID, OAUTH_SECRET, false);
//        System.out.println(client.getAccessToken("218af4253e53006829231904b0f1611f2ba43f466d875131ee"));
//        98373868L    <-- account Id


//        System.out.println(client.viewUser(ACCESS_TOKEN));

//        System.out.println(client.registerMerchant()) ;

        CheckoutUri checkoutResponse =  client.createCheckout(143865178L, 2981050165L);
        System.out.println("Checkout URI: " + checkoutResponse.getCheckoutUri() + "\nCheckoutId: " + checkoutResponse.getCheckoutId());

//        CheckoutState state = client.refund(2028095370L, "Testing full refund of a pending payment", null);
//        CheckoutState state = client.voidPayment(2028095370L, "Testing void");
//        String cc = client.createCreditCardId();
//
//        System.out.println(cc);
//                                                 client.deleteCreditCard(213213L);

//                                                 System.out.println(client.viewAccount(143865178L));
//                                                 System.out.println(client.viewCheckout(1292672920L));
//                                                 System.out.println(client.viewUser(ACCESS_TOKEN));
                                             } catch (WePayException wpe) {
                                                System.out.println("Error: " + wpe.getErrorCode() + "\nText: " + wpe.getErrorText() + "\nType: " + wpe.getErrorType());
                                             }
                                             catch (IOException e) {
                                                 e.printStackTrace();
                                             }

    }

    public String registerMerchant() {
        return api.getAuthorizationUri(WePayApi.Scope.getAll(),REDIRECT_URL, null, "Peng Lim", "blah@blah.com");
    }

    public Token getAccessToken(String code) throws IOException, WePayException {
        // Use the code to generate a token.
        return api.getToken(code, REDIRECT_URL);
    }

    public WePayUser viewUser(String accesToken) throws IOException, WePayException {
        return api.execute(accesToken, new UserRequest());
    }

    public String createCreditCardId() throws IOException, WePayException {
        /* Note that you will need to call the /checkout/create call or the /credit_card/authorize call
        within 30 minutes or the credit card object will expire.
        User contact information (especially e-mail) is key for risk analysis, so please make sure you provide the actual end-user contact information.
         */

        CreditCardCreateRequest ccRequest = new CreditCardCreateRequest();
        Checkout.ShippingAddress address = new Checkout.ShippingAddress();
        address.setAddress1("123 test st.");
//        address.setAddress2();
        address.setCity("Gilbert");
        address.setState("AZ");
        address.setZip("85233");
        address.setCountry("US");

        //For non-US address
//        address.setAddress1("100 Main St");
//        address.setRegion("ON");
//        address.setCity("Toronto");
//        address.setState("AZ");
//        address.setPostalcode("M4E 1Z5");
//        address.setCountry("CA");


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

    public CheckoutState refund(Long checkoutId, String reason, BigDecimal amount) throws IOException, WePayException {
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

    public CheckoutUri createCheckout(Long accountId, Long ccId) throws IOException, WePayException {
        CheckoutCreateRequest createRequest = new CheckoutCreateRequest();
        createRequest.setAccountId(accountId);
        createRequest.setShortDescription("A test charge4");
//        createRequest.setLongDescription(("");
//        createRequest.setReferenceId("");
        createRequest.setType(Constants.PaymentType.GOODS);
        createRequest.setAmount(new BigDecimal(12.00));
        createRequest.setPaymentMethodType("credit_card");
        createRequest.setPaymentMethodId(ccId);
        createRequest.setAutoCapture(true);   //In order to automatically capture the funds, instead of making a separate capture() call
//        createRequest.setAppFee();  //The dollar amount that the application will receive in fees. App fees go into the API applications WePay account. Limited to 20% of the checkout amount.
//        createRequest.setFeePayer(Constants.FeePayer.payee);      //Who will pay the fees (WePay's fees and any app fees).


        return api.execute(ACCESS_TOKEN, createRequest);

        /*
        Checkouts expire 30 minutes after they are created if there is no activity on them (e.g. they were abandoned after creation).
         */

    }

    public CheckoutState capture(long checkoutid) throws IOException, WePayException {
        CheckoutCaptureRequest captureRequest = new CheckoutCaptureRequest();
        captureRequest.setCheckoutId(checkoutid);
        return api.execute(ACCESS_TOKEN, captureRequest);

        /*
        If auto_capture was set to false when the checkout was created, you will need to make this call to release funds to the account.
        Until you make this call the money will be held by WePay and if you do not capture the funds within 14 days then the payment will be
        automatically cancelled or refunded. You can only make this call if the checkout is in state 'reserved'.
         */
    }


    public Checkout viewCheckout(long checkoutId) throws IOException, WePayException {

        CheckoutRequest request = new CheckoutRequest();
        request.setCheckoutId(checkoutId);
        return api.execute(ACCESS_TOKEN, request);
    }

    public WePayAccount viewAccount(long accountId) throws IOException, WePayException {

        AccountRequest request = new AccountRequest();
        request.setAccountId(accountId);
        return api.execute(ACCESS_TOKEN, request);
    }


    public CheckoutState voidPayment(Long checkoutId, String reason) throws IOException, WePayException {
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

    public CreditCard deleteCreditCard(Long ccId) throws IOException, WePayException {
        CreditCardDeleteRequest request = new CreditCardDeleteRequest();
        request.setClientId(OAUTH_CLIENT_ID);
        request.setClientSecret(OAUTH_SECRET);
        request.setCreditCardId(ccId);

        return api.execute(ACCESS_TOKEN, request);

        /*
            Successful attempt gets a state=deleted.

            Deleting an already deleted ccid
            "invalid_request: Cannot delete a deleted credit card."

            Deleting invalid id
            "access_denied: this credit card does not exist or does not belong to the app"

         */

    }


}
