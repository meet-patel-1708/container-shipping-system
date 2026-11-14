package com.shipping.payment_service.gateway;

import com.shipping.payment_service.exception.PaymentGatewayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class RazorpayGateway {
    private final RestClient client;
    private final String keyId;
    private final String secret;

    public RazorpayGateway(@Value("${razorpay.key-id}")String keyId,
                           @Value("${razorpay.key-secret}")String secret){
        this.keyId = keyId;
        this.secret = secret;
        this.client = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(h -> h.setBasicAuth(keyId, secret))
                .build();
    }
    public String keyId(){
        return keyId;
    }
    @SuppressWarnings("unchecked")
    public String createOrder(long amountPaise,String receipt){
        try{
            Map<String,Object> res = client.post().uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amountPaise, "currency", "INR", "receipt", receipt))
                    .retrieve().body(Map.class);
            return (String) res.get("id");
        }catch(RestClientException e){
            throw new PaymentGatewayException("Could not reach Razorpay: " + e.getMessage());
        }
    }
    public boolean verify(String orderId, String paymentId, String signature){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(digest);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
