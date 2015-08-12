package com.innerdot.azure.rest.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import mjson.Json;

public class JsonUtils {
    public static Json jsonFromUri(String uri) {
        Json result = null;
        String body = null;
        
		try {
			final HttpClient httpClient = new DefaultHttpClient();
			HttpConnectionParams
					.setConnectionTimeout(httpClient.getParams(), 10000);
			HttpGet httpGet = new HttpGet(uri);
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			body = read(instream);
		} catch(IOException ex) {
			body = null;
		}
		
		if(body != null) {
		    result = Json.read(body);
		}
		
		return result;
    }
    
    public static Json jsonFromFile(String path) {
        Json result = null;
		String body = null;
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			body = sb.toString();
		} catch(IOException ex) {
			body = null;
		}
		
		if(body != null) {
			result = Json.read(body);
		}
		
		return result;
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
}