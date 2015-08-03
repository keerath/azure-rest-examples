package com.innerdot.azure.rest.example;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.BufferedReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.message.AbstractHttpMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationAuthExample {
    private final static String AUTHORIZATION_ENDPOINT = "https://login.microsoftonline.com/";
    private final static String ARM_ENDPOINT = "https://management.azure.com/";

    public static void main(String[] args) throws Exception {
    	if(args.length != 5) {
    		System.out.println("Usage:");
    		System.out.println(" <username> <password> <tenant id> <client id> <subscription id>");
    		System.exit(1);
    	}

    	String username = null;
    	String credential = null;
    	String tenantId = null;
    	String clientId = null;
    	String subscriptionId = null;
    	
    	int idx = 0;
	    username = args[idx++];
    	credential = args[idx++];
    	tenantId = args[idx++];
    	clientId = args[idx++];
    	subscriptionId = args[idx++];

        // use adal to Authenticate
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;

		try {
            service = Executors.newFixedThreadPool(1);
            String url = AUTHORIZATION_ENDPOINT + tenantId + "/oauth2/authorize";
            context = new AuthenticationContext(url, 
                                                false, 
                                                service);
            Future<AuthenticationResult> future = null;
            future = context.acquireToken(ARM_ENDPOINT, clientId,
                                          username, credential, null);
            result = future.get();
        } catch (Exception ex) {
        	System.out.println("Exception occurred:");
	        ex.printStackTrace();
            System.exit(1);
        } finally {
            service.shutdown();
        }
        
        // make a request to list available providers
        String url = ARM_ENDPOINT + 
                     "subscriptions/" + subscriptionId +
                     "/providers" + 
                     "?api-version=2014-04-01-preview";
        String body = null;
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(httpClient.getParams(), 10000);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + result.getAccessToken());
            //httpGet.addHeader("User-Agent", "WindowsAzureXplatCLI/0.9.5");
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(instream), 1000);
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }
            instream.close();
            body = sb.toString();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        
        System.out.println(body);
	}
}
