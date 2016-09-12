package com.test.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchITunes {

	public static void main(String[] args) {
		SearchITunes st = new SearchITunes();
		st.fireRequest(st);
	}

	private String fireRequest(SearchITunes st) {
		SearchInput si = new SearchInput();
		String url = "https://itunes.apple.com/search?term=";
		String inputLoc, response = null; 
		try {
			inputLoc = FileUtils.readFileToString(new File("inputFile.json"));
			JSONObject jsonInput = null;
			try {
				jsonInput = new JSONObject(inputLoc);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			JSONObject testcases = null;
			try {
				int noOfTestCases = jsonInput.length();
				for(int i = 1; i <= noOfTestCases; i++) {
					System.out.println("Test Case: " + i);
					testcases = jsonInput.getJSONObject("testcase" + i);
	
					System.out.println("Test Case Description: " + testcases.getString("description"));
					si.setCountry(testcases.getString("searchTerm"));
					si.setCountry(testcases.getString("country"));
					si.setCountry(testcases.getString("media"));
					si.setCountry(testcases.getString("limit"));
					
					url += testcases.getString("searchTerm"); 
					if(!testcases.getString("country").equals(""))
						url += "&country=" + testcases.getString("country");
					if(!testcases.getString("media").equals(""))
						url += "&media=" + testcases.getString("media");
					if(!testcases.getString("limit").equals(""))
						url += "&limit=" + testcases.getString("limit");
					
					response = st.post(url, "", "json");
					st.parseResponse(response, testcases);
					url = "https://itunes.apple.com/search?term=";
					System.out.println("*******************************************");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return response;
	}

	private void parseResponse(String response, JSONObject testcases) {
		try {
			JSONObject jb = new JSONObject(response);
			int limit = 0;
			
			int resultsCount = (int) jb.get("resultCount");
			if(!testcases.get("limit").equals(""))
				limit = Integer.parseInt(testcases.get("limit").toString());
			
			System.out.println("Response:: " +jb);
			System.out.println("Result Count: " + resultsCount);
			
			//Performing validation only on the results count. Validations can be extended to all other fields.
			if(limit == 0 && (resultsCount <= 50))
				System.out.println("Success");
			if(limit != 0 && (resultsCount <= limit))
				System.out.println("Success");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String post(String uri, String requestBody, String bodyType) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();

		String responseMessage = null;

		System.out.println("Request:: " + uri);

		HttpPost httpPost = new HttpPost(uri);

		StringEntity se = null;

		se = new StringEntity(requestBody, "UTF-8");
		httpPost.addHeader("Content-type", "application/json");
		httpPost.addHeader("charset", "UTF-8");

		httpPost.setEntity(se);

		try {

			ResponseHandler<String> response = new BasicResponseHandler() {
				@Override
				public String handleResponse(HttpResponse response)
						throws HttpResponseException, IOException {
					StatusLine statusLine = response.getStatusLine();
					if (statusLine.getStatusCode() < 400) {
						return super.handleResponse(response);
					}
					HttpEntity entity = response.getEntity();
					return entity == null ? null : EntityUtils.toString(entity);
				}
			};

			HttpResponse httpResponse = httpclient.execute(httpPost,
					httpContext);

			BasicHttpResponse baseResponse = (BasicHttpResponse) httpContext
					.getAttribute("http.response");
			System.out.println("Response Code:: " +baseResponse.getStatusLine()
					.getStatusCode());
			responseMessage = EntityUtils.toString(httpResponse.getEntity());
		} catch (Throwable e) {
			System.out.println("Error" + e.getMessage());
			return null;
		}
		return responseMessage;
	}
}