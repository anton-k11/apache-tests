/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.examples.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * An example of HttpClient can be customized to authenticate
 * preemptively using BASIC scheme.
 * <b/>
 * Generally, preemptive authentication can be considered less
 * secure than a response to an authentication challenge
 * and therefore discouraged.
 */
public class ClientTest {

	static HttpPost posts[];
	static CloseableHttpClient httpclient;
	static HttpHost target = new HttpHost("localhost", 9090, "http");


	public static void main(String[] args) throws Exception {
 
	    setupHttpClient();
	    
		try {

			posts = new HttpPost[2];
			posts[1] = new HttpPost("/tikva");
			posts[0] = new HttpPost("/");
			
			HttpEntity http_en = new StringEntity("Alabala portokala 123456");
			posts[0].setEntity(http_en);

			

			Thread [] threads = new Thread[2];
			for (int i=0; i< threads.length; i++ ) {
				threads[i] = new Thread(){
					public void run(){
						HttpClientContext localContext = HttpClientContext.create();
						// Add AuthCache to the execution context

						CloseableHttpResponse response = null;
						for(int j=0; j< 4; j++){
							try {
								HttpPost http_req = (HttpPost) posts[j % 2].clone();
								System.out.println("Executing request " + http_req.getRequestLine() + " to target " + target);

								response = httpclient.execute(target, http_req, localContext);

								System.out.println(Thread.currentThread().getName()+"----------------------------------------");
								System.out.println(Thread.currentThread().getName() +" "+ 
										response.getStatusLine().getStatusCode()+" "+
										response.getStatusLine().getReasonPhrase()
										);
								response.getEntity().writeTo(System.out);
								//EntityUtils.consume(response.getEntity());
							}catch(Exception ioe){
								ioe.printStackTrace();
							} 
							finally {
								try {
									if(response != null) response.close(); 								
									Thread.sleep(1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				};
				threads[i].start();
			}

			for (Thread t : threads) {
				t.join();
			}
		} finally {
			Thread.sleep(180000);
			// Wait before closing the client, this will keep connections open, some more requests might happen :)
			httpclient.close();
		}
	}



	static HttpClient setupHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		RequestConfig requestConfig = RequestConfig.DEFAULT;
		// RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000)
		//         .setSocketTimeout(2000).build();
		System.out.println("RequestConfig: "+ RequestConfig.DEFAULT);

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();    

		HttpClientBuilder builder = HttpClients.custom();

		builder
		.setConnectionManager(cm)   	
		.setRedirectStrategy(new LaxRedirectStrategy())
		.setDefaultRequestConfig(requestConfig)
		;

		return httpclient = builder.build(); 
	}


}
