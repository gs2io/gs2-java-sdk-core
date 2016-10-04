/*
 * Copyright 2016 Game Server Services, Inc. or its affiliates. All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.gs2;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import io.gs2.exception.BadGatewayException;
import io.gs2.exception.BadRequestException;
import io.gs2.exception.ConflictException;
import io.gs2.exception.InternalServerErrorException;
import io.gs2.exception.NotFoundException;
import io.gs2.exception.QuotaExceedException;
import io.gs2.exception.RequestTimeoutException;
import io.gs2.exception.ServiceUnavailableException;
import io.gs2.exception.UnauthorizedException;
import io.gs2.model.IGs2Credential;
import io.gs2.model.Region;
import io.gs2.util.SignUtil;

abstract public class AbstractGs2Client<T extends AbstractGs2Client<?>> {

	/** 認証情報 */
	protected IGs2Credential credential;
	/** アクセス先リージョン */
	protected Region region;
	
	public AbstractGs2Client(IGs2Credential credential) {
		this.credential = credential;
		this.region = Region.AP_NORTHEAST_1;
	}
	
	/**
	 * アクセス先リージョンを取得
	 * 
	 * @return アクセス先リージョン
	 */
	public Region getRegion() {
		return region;
	}
	
	/**
	 * アクセス先リージョンを設定
	 * 
	 * @param region アクセス先リージョン
	 */
	public void setRegion(Region region) {
		this.region = region;
	}
	
	/**
	 * アクセス先リージョンを設定
	 * 
	 * @param region アクセス先リージョン
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public T withRegion(Region region) {
		setRegion(region);
		return (T)this;
	}

	/**
	 * POSTリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param credential 認証情報
	 * @param service アクセス先サービス
	 * @param module アクセス先モジュール
	 * @param function アクセス先ファンクション
	 * @param body リクエストボディ
	 * @return リクエストオブジェクト
	 */
	protected HttpPost createHttpPost(String url, IGs2Credential credential, String service, String module, String function, String body) {
		if(credential.getClientId()== null || credential.getClientSecret() == null) {
			throw new UnauthorizedException("invalid credential");
		}
		Long timestamp = System.currentTimeMillis()/1000;
		String sign = new Base64().encodeAsString(SignUtil.sign(credential.getClientSecret(), module, function, timestamp));
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("X-GS2-CLIENT-ID", credential.getClientId());
		post.setHeader("X-GS2-REQUEST-TIMESTAMP", String.valueOf(timestamp));
		post.setHeader("X-GS2-REQUEST-SIGN", sign);
		post.setEntity(new StringEntity(body, "UTF-8"));
		
//		String curl = "curl ";
//		curl += "-X POST ";
//		curl += "-H 'Content-Type: application/json' ";
//		curl += "-H 'X-GS2-CLIENT-ID: " + credential.getClientId() + "' ";
//		curl += "-H 'X-GS2-REQUEST-TIMESTAMP: " + String.valueOf(timestamp) + "' ";
//		curl += "-H 'X-GS2-REQUEST-SIGN: " + sign + "' ";
//		curl += "--data '" + body + "' ";
//		curl += url;
//		System.out.println(curl);
		
		return post;
	}

	/**
	 * POSTリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param credential 認証情報
	 * @param service アクセス先サービス
	 * @param module アクセス先モジュール
	 * @param function アクセス先ファンクション
	 * @param body リクエストボディ
	 * @return リクエストオブジェクト
	 */
	protected HttpPut createHttpPut(String url, IGs2Credential credential, String service, String module, String function, String body) {
		if(credential.getClientId()== null || credential.getClientSecret() == null) {
			throw new UnauthorizedException("invalid credential");
		}
		Long timestamp = System.currentTimeMillis()/1000;
		String sign = new Base64().encodeAsString(SignUtil.sign(credential.getClientSecret(), module, function, timestamp));
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpPut put = new HttpPut(url);
		put.setHeader("Content-Type", "application/json");
		put.setHeader("X-GS2-CLIENT-ID", credential.getClientId());
		put.setHeader("X-GS2-REQUEST-TIMESTAMP", String.valueOf(timestamp));
		put.setHeader("X-GS2-REQUEST-SIGN", sign);
		put.setEntity(new StringEntity(body, "UTF-8"));

//		String curl = "curl ";
//		curl += "-X PUT ";
//		curl += "-H 'Content-Type: application/json' ";
//		curl += "-H 'X-GS2-CLIENT-ID: " + credential.getClientId() + "' ";
//		curl += "-H 'X-GS2-REQUEST-TIMESTAMP: " + String.valueOf(timestamp) + "' ";
//		curl += "-H 'X-GS2-REQUEST-SIGN: " + sign + "' ";
//		curl += "--data '" + body + "' ";
//		curl += url;
//		System.out.println(curl);
		
		return put;
	}

	/**
	 * GETリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param credential 認証情報
	 * @param service アクセス先サービス
	 * @param module アクセス先モジュール
	 * @param function アクセス先ファンクション
	 * @return リクエストオブジェクト
	 */
	protected HttpGet createHttpGet(String url, IGs2Credential credential, String service, String module, String function) {
		if(credential.getClientId()== null || credential.getClientSecret() == null) {
			throw new UnauthorizedException("invalid credential");
		}
		Long timestamp = System.currentTimeMillis()/1000;
		String sign = new Base64().encodeAsString(SignUtil.sign(credential.getClientSecret(), module, function, timestamp));
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpGet get = new HttpGet(url);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("X-GS2-CLIENT-ID", credential.getClientId());
		get.setHeader("X-GS2-REQUEST-TIMESTAMP", String.valueOf(timestamp));
		get.setHeader("X-GS2-REQUEST-SIGN", sign);

//		String curl = "curl ";
//		curl += "-X GET ";
//		curl += "-H 'Content-Type: application/json' ";
//		curl += "-H 'X-GS2-CLIENT-ID: " + credential.getClientId() + "' ";
//		curl += "-H 'X-GS2-REQUEST-TIMESTAMP: " + String.valueOf(timestamp) + "' ";
//		curl += "-H 'X-GS2-REQUEST-SIGN: " + sign + "' ";
//		curl += url;
//		System.out.println(curl);
		
		return get;
	}

	/**
	 * DELETEリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param credential 認証情報
	 * @param service アクセス先サービス
	 * @param module アクセス先モジュール
	 * @param function アクセス先ファンクション
	 * @return リクエストオブジェクト
	 */
	protected HttpDelete createHttpDelete(String url, IGs2Credential credential, String service, String module, String function) {
		if(credential.getClientId()== null || credential.getClientSecret() == null) {
			throw new UnauthorizedException("invalid credential");
		}
		Long timestamp = System.currentTimeMillis()/1000;
		String sign = new Base64().encodeAsString(SignUtil.sign(credential.getClientSecret(), module, function, timestamp));
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpDelete delete = new HttpDelete(url);
		delete.setHeader("Content-Type", "application/json");
		delete.setHeader("X-GS2-CLIENT-ID", credential.getClientId());
		delete.setHeader("X-GS2-REQUEST-TIMESTAMP", String.valueOf(timestamp));
		delete.setHeader("X-GS2-REQUEST-SIGN", sign);

//		String curl = "curl ";
//		curl += "-X DELETE ";
//		curl += "-H 'Content-Type: application/json' ";
//		curl += "-H 'X-GS2-CLIENT-ID: " + credential.getClientId() + "' ";
//		curl += "-H 'X-GS2-REQUEST-TIMESTAMP: " + String.valueOf(timestamp) + "' ";
//		curl += "-H 'X-GS2-REQUEST-SIGN: " + sign + "' ";
//		curl += url;
//		System.out.println(curl);
		
		return delete;
	}

	/**
	 * リクエストを実行する
	 * 
	 * @param <U> レスポンスの型
	 * @param request リクエスト
	 * @param clazz レスポンスのクラス
	 * @return レスポンス
	 * @throws BadRequestException リクエストパラメータに誤りがある場合にスローされます
	 * @throws UnauthorizedException 認証に失敗した場合にスローされます
	 * @throws NotFoundException リソースが存在しない場合にスローされます
	 * @throws InternalServerErrorException 未知のサーバエラーが発生した場合にスローされます
	 */
	protected <U> U doRequest(HttpUriRequest request, Class<U> clazz) throws BadRequestException, UnauthorizedException, NotFoundException, InternalServerErrorException {
		long begin = System.currentTimeMillis();
		try {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(1000 * 30)
					.setConnectTimeout(1000 * 30)
					.setSocketTimeout(1000 * 30)
					.build();
			HttpClient client = HttpClientBuilder.create()
					.setDefaultRequestConfig(requestConfig)
					.build();
			ObjectMapper mapper = new ObjectMapper();
			
			int statusCode = 200;
			String message = null;
			int retryCount = 0;
			for(; retryCount<Gs2Constant.RETRY_NUM; retryCount++) {
				
				boolean timeout = false;
				try {
					HttpResponse response = client.execute(request);
					
					statusCode = response.getStatusLine().getStatusCode();
					if(statusCode == 200) {
						if(clazz == null) return null;
						try (InputStream in = response.getEntity().getContent()) {
//							byte[] b = new byte[in.available()];
//							in.read(b);
//							System.out.println(new String(b));
//							return mapper.readValue(new String(b), clazz);
							return mapper.readValue(in, clazz);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					
					try {
						JsonNode json = mapper.readTree(response.getEntity().getContent());
						message = ((TextNode)json.get("message")).asText();
					} catch (Exception e) {}
					
					if(statusCode == 504) {
						timeout = true;
					}
				} catch (SocketTimeoutException e) {
					timeout = true;
				}
				if(timeout) {
					try {
						Thread.sleep(Gs2Constant.RETRY_WAIT);
					} catch (InterruptedException e) { }
					continue;
				}
				break;
			}
			
			if(retryCount > 0 && request.getMethod().equals("DELETE") && statusCode == 404) {
				return null;
			}
			
			switch(statusCode) {
			case 400: throw new BadRequestException(message);
			case 401: throw new UnauthorizedException(message);
			case 402: throw new QuotaExceedException(message);
			case 404: throw new NotFoundException(message);
			case 409: throw new ConflictException(message);
			case 500: throw new InternalServerErrorException(message);
			case 502: throw new BadGatewayException(message);
			case 503: throw new ServiceUnavailableException(new ArrayList<>());
			case 504: throw new RequestTimeoutException(message);
			}
			throw new RuntimeException("[" + statusCode + "] " + (message == null ? "unknown" : message));
		} catch(IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.out.println("communication time: " + (System.currentTimeMillis() - begin));
		}
	}
}
