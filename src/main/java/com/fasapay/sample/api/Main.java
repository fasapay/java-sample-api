/*
 * 
 */
package com.fasapay.sample.api;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * This is simple Sample of FasaPay XML API 
 * note : This sample did not do any parsing hence did not have any error handling
 * 
 * @author Sakurai
 */
public class Main {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Change This to https://api.fasapay.com/ for live version
     */
    public String url = "https://sandbox.fasapay.com/xml";
    
    /**
     * Change this to your api_key
     */
    public String api_key = "75791f43ffeacf1bf7710e3ee0caa1c2";
    
    /**
     * Change this to your api_secretword
     */
    public String api_secret = "573ec9f8-d9ec-4a87-8d69-3e7cbe61af5b";

    public String fp_merchant = "FPX8947";
    public String fp_buyer = "FPX9561";

    public static void main(String[] args) throws Exception {

        Main obj = new Main();

        try {
            System.out.println("Get FasaPay Account : ");
            String account_request = "<fasa_request>" + obj.buildAuth() + "<account>" + obj.fp_buyer +"</account></fasa_request>";
            String account_response = obj.sendPost(account_request);
            System.out.println("SEND : \n" + account_request);
            System.out.println("RESPONDS : \n" + account_response);
            
            System.out.println("Get FasaPay Detail Transaction: ");
            String detail_request = "<fasa_request>" + obj.buildAuth() + "<detail>TU2021072337333</detail></fasa_request>";
            String detail_response = obj.sendPost(detail_request);
            System.out.println("SEND : \n" + detail_request);
            System.out.println("RESPONDS : \n" + detail_response);
            
            System.out.println("Get FasaPay Send Transaction: ");
            String ref = "WD" + (System.currentTimeMillis() / 1000L);
            String transfer = obj.buildTransfer(obj.fp_buyer, "1000.00", "IDR", "Test Tranfer API", ref);
            String transfer_request = "<fasa_request>" + obj.buildAuth() + transfer + "</fasa_request>";
            String transfer_response = obj.sendPost(transfer_request);
            System.out.println("SEND : \n" + transfer_request);
            System.out.println("RESPONDS : \n" + transfer_response);
            
            System.out.println("Get FasaPay Detail Transaction: ");
            String detail_wd_request = "<fasa_request>" + obj.buildAuth() + "<detail><ref>" + ref +"</ref></detail></fasa_request>";
            String detail_wd_response = obj.sendPost(detail_wd_request);
            System.out.println("SEND : \n" + detail_wd_request);
            System.out.println("RESPONDS : \n" + detail_wd_response);
            
            System.out.println("Get FasaPay Latest History: ");
            String history_request = "<fasa_request>" + obj.buildAuth() + "<history></history></fasa_request>";
            String history_response = obj.sendPost(history_request);
            System.out.println("SEND : \n" + history_request);
            System.out.println("RESPONDS : \n" + history_response);
            
            System.out.println("Get FasaPay Balance: ");
            String balance_request = "<fasa_request>" + obj.buildAuth() + "<balance>IDR</balance><balance>USD</balance></fasa_request>";
            String balance_response = obj.sendPost(balance_request);
            System.out.println("SEND : \n" + balance_request);
            System.out.println("RESPONDS : \n" + balance_response);
        } finally {
            obj.close();
        }

    }

    /**
     * Build Auth tag
     * @return 
     */
    public String buildAuth() {
        // Get Current UTC Date time 
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        
        // Token only need Year Month Day Hour
        DateTimeFormatter stamp = DateTimeFormatter.ofPattern("yyyyMMddHH");
        
        // Build Token String
        String token_string = this.api_key + ":" + this.api_secret + ":" + now.format(stamp);
        
        // Hash Token String to become Token
        String token = DigestUtils.sha256Hex(token_string);

        String auth = "<auth>"
                + "<api_key>" + this.api_key + "</api_key>"
                + "<token>" + token + "</token>"
                + "</auth>";

        return auth;
    }
    
    /**
     * Build Transfer XML tag
     * @param to
     * @param amount
     * @param currency
     * @param note
     * @param ref
     * @return 
     */
    public String buildTransfer(String to, String amount, String currency, String note, String ref) {
        String fee_mode = "FiR";
        
        String transfer = "<transfer>"
                +"<to>" + to + "</to>"
                +"<amount>" + amount + "</amount>"
                +"<currency>" + currency + "</currency>"
                +"<fee_mode>" + fee_mode + "</fee_mode>"
                +"<note>" + note + "</note>"
                +"<ref>" + ref + "</ref>"
                +"</transfer>";
        
        return transfer;
    }

    private void close() throws IOException {
        httpClient.close();
    }

    /**
     * POST the `xml` string on `req` 
     * The Return of this function can be fed to XML Parser to detect if 
     * the request is success or not.
     * 
     * @param xml
     * @return String
     * @throws Exception 
     */
    private String sendPost(String xml) throws Exception {
        HttpPost post = new HttpPost(this.url);

        List<NameValuePair> urlParameter = new ArrayList<>();
        urlParameter.add(new BasicNameValuePair("req", xml));
        post.setEntity(new UrlEncodedFormEntity(urlParameter));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

            String output = EntityUtils.toString(response.getEntity());           
            return output;
        }
    }

}
