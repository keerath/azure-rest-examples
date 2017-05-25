package com.innerdot.azuresdk.rest.example;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.aad.adal4j.AuthenticationResult;

public class ApplicationAuthExample {
    public static void main(String[] args) throws Exception {
    	if((!args[0].equals("service-principal") && !args[0].equals("user")) || 
    			(args[0].equals("user") && args.length != 6) ||
    			(args[0].equals("service-principal") && args.length != 5)) {
    		System.out.println("Usage:");
    		System.out.println(" user <username> <password> <client id> <tenant id> <subscription id>");
    		System.out.println(" service-principal <password> <client id> <tenant id> <subscription id>");
    		System.exit(1);
    	}

    	String username = null;
    	String credential = null;
    	String tenantId = null;
    	String clientId = null;
    	String subscriptionId = null;
    	
    	int idx = 1;
    	if(args[0].equals("user")) {
		    username = args[idx++];
            System.out.println("username: " + username);
		}
    	credential = args[idx++];
    	clientId = args[idx++];
    	tenantId = args[idx++];
    	subscriptionId = args[idx++];
        System.out.println("credential: " + credential);
        System.out.println("clientId: " + clientId);
    	System.out.println("tenantId: " + tenantId);
    	System.out.println("subscriptionId: " + subscriptionId);
    	
		try {
            AzureTokenCredentials credentials = null;
            if(username == null) {
                credentials = new ApplicationTokenCredentials(clientId, tenantId, credential, AzureEnvironment.AZURE);
            } else {
                credentials = new UserTokenCredentials(clientId, tenantId, username, credential, AzureEnvironment.AZURE);
            }
            String token = credentials.getToken(AzureEnvironment.AZURE.graphEndpoint());
            if(token == null) {
                System.out.println("Authentication failed.");
            } else {
                System.out.println("Authentication succeeded.");
            }
        } catch (Exception ex) {
        	System.out.println("Exception occurred:");
	        ex.printStackTrace();
            System.exit(1);
        }
	}
}
