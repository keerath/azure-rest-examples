package com.innerdot.azure.rest.example;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import org.apache.regexp.StreamCharacterIterator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mjson.Json;

public class ARMTemplateExample {
    private final static String AUTHORIZATION_ENDPOINT = "https://login.microsoftonline.com/";
    private final static String ARM_ENDPOINT = "https://management.azure.com/";
    
    // do the put or post (deploy or validate) ad simply return success or failure.
    // note that in the case of validate, the response body actually will contain
    // information about what is wrong with the template.  
    private static boolean handleDeployOrValidateTemplate(boolean isDeploy, String url, String authToken, String body) {
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(httpClient.getParams(), 10000);
            HttpEntityEnclosingRequestBase httpOp = null;
            if(isDeploy) {
                httpOp = new HttpPut(url);
            } else {
                httpOp = new HttpPost(url);
            } 
            httpOp.addHeader("Accept", "application/json");
            httpOp.addHeader("Content-Type", "application/json");
            httpOp.addHeader("Authorization", "Bearer " + authToken);
            StringEntity entity = new StringEntity(body, "UTF-8");
            entity.setContentType("application/json");
            httpOp.setEntity(entity);
            HttpResponse response = httpClient.execute(httpOp);
            int statusCode = response.getStatusLine().getStatusCode();
            return (statusCode / 100) == 2 ? true : false;
        } catch(IOException ex) {
            System.out.println("Deploy/validate failed.");
            ex.printStackTrace();
        }
        
        return false;
    }
    
    private static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
    
    private static Json checkStatus(String url, String authToken) {
        Json result = null;
        
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(httpClient.getParams(), 10000);
            HttpGet httpOp = null;
            httpOp = new HttpGet(url);
            httpOp.addHeader("Accept", "application/json");
            httpOp.addHeader("Content-Type", "application/json");
            httpOp.addHeader("Authorization", "Bearer " + authToken);
            HttpResponse response = httpClient.execute(httpOp);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if((statusCode / 100) == 2) {
                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();
                result = Json.read(read(instream));
            }
        } catch(IOException ex) {
            System.out.println("Check status failed.");
            ex.printStackTrace();
        }
        
        return result;
    }

    public static void main(String[] args) throws Exception {
    	if((!args[0].equals("deploy") && !args[0].equals("validate") && !args[0].equals("status")) || 
    			(args[0].equals("status") && args.length != 8) ||
    			((args[0].equals("deploy") || args[0].equals("validate")) && (args.length != 9 && args.length != 10))) {
    		System.out.println("Usage:");
    		System.out.println(" deploy/validate <username> <password> <client id> <tenant id> <subscription id> <resource group> <deployment name> <template file> [<parameters file>]");
    		System.out.println(" status <username> <password> <client id> <tenant id> <subscription id> <resource group> <deployment name>");
    		System.exit(1);
    	}

    	String username = null;
    	String credential = null;
    	String tenantId = null;
    	String clientId = null;
    	String subscriptionId = null;
    	String resourceGroup = null;
    	String resourceName = null;
    	String templateFile = null;
    	String parametersFile = null;
    	
    	int idx = 1;
	    username = args[idx++];
    	credential = args[idx++];
    	clientId = args[idx++];
    	tenantId = args[idx++];
    	subscriptionId = args[idx++];
    	resourceGroup = args[idx++];
    	resourceName = args[idx++];
    	if(args[0].equals("deploy") || args[0].equals("validate")) {
    	    templateFile = args[idx++];
    	    if(args.length == 10) {
    	        parametersFile = args[idx++];
    	    }
    	}
    	
        // use adal to Authenticate
        AuthenticationContext context = null;
        AuthenticationResult authResult = null;
        ExecutorService service = null;
		try {
            service = Executors.newFixedThreadPool(1);
            String url = AUTHORIZATION_ENDPOINT + tenantId + "/oauth2/authorize";
            context = new AuthenticationContext(url, 
                                                false, 
                                                service);
            Future<AuthenticationResult> future = null;
            future = context.acquireToken(ARM_ENDPOINT, clientId, username, credential, null);
            authResult = future.get();
        } catch (Exception ex) {
        	System.out.println("Exception occurred:");
	        ex.printStackTrace();
            System.exit(1);
        } finally {
            service.shutdown();
        }
        
        String apiVersion = "2015-01-01";
        final String basePath =  "/subscriptions/" + subscriptionId +
                                 "/resourcegroups/" + resourceGroup +
                                 "/deployments/" + resourceName;

        // handle deploy or validate
        if(templateFile != null) {
            // setup template
            ARMTemplate template = null;
            Json parameters = null;
            try {
                URL tmp = new URL(templateFile);
                template = ARMTemplate.fromUri(templateFile);
            } catch(MalformedURLException ex) {
                template = ARMTemplate.fromFile(templateFile);
            }
            if(template == null) {
                System.out.println("Unable to load template.  Template specified: " + templateFile);
                System.exit(1);
            }
            if(parametersFile != null) {
                try {
                    URL tmp = new URL(parametersFile);
                    parameters = JsonUtils.jsonFromUri(parametersFile);
                } catch(MalformedURLException ex) {
                    parameters = JsonUtils.jsonFromFile(parametersFile);
                }
                if(parameters == null) {
                    System.out.println("Unable to load parameters.  Parameters file specified: " + parametersFile);
                    System.exit(1);
                } else {
                    template.addParameters(parameters);
                }
            }
            
            String path = null;
            if(args[0].equals("deploy")) {
                path = basePath + "?api-version=" + apiVersion;
            } else {
                path = basePath + "/validate?api-version=" + apiVersion;
            }
            boolean r = handleDeployOrValidateTemplate(args[0].equals("deploy"), 
                                                       ARM_ENDPOINT + path,
                                                       authResult.getAccessToken(),
                                                       template.toString());

            if(r) {
                System.out.println("Template successfully " + (args[0].equals("deploy") ? "deployed" : "validated") + ".");
            } else {
                System.out.println("Template unsuccessfully " + (args[0].equals("deploy") ? "deployed" : "validated") + ".");                
            }
            System.out.println("Template: " + templateFile);
        } else {
            // let's check the status
            String path = basePath + "?api-version=" + apiVersion;
            Json status = checkStatus(ARM_ENDPOINT + path, authResult.getAccessToken());
            if(status != null) {
                System.out.println("Status for deployment: " + resourceName);
                System.out.println(status.toString());
            } else {
                System.out.println("Unable to find status for deployment: " + resourceName);
            }
        }
    }
}
