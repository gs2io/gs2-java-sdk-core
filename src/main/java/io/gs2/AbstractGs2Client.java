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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.gs2.exception.*;
import io.gs2.model.IGs2Credential;
import io.gs2.model.LoginResult;
import io.gs2.model.Region;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

abstract public class AbstractGs2Client<T extends AbstractGs2Client<?>> {

	/** 認証情報 */
	protected IGs2Credential credential;
	/** アクセス先リージョン */
	protected Region region;

	public AbstractGs2Client(IGs2Credential credential) {
		this.credential = credential;
		this.region = Region.AP_NORTHEAST_1;

		this.login();
	}

	public AbstractGs2Client(IGs2Credential credential, Region region) {
		this.credential = credential;
		this.region = region;

		this.login();
	}

	public AbstractGs2Client(IGs2Credential credential, String region) {
		this.credential = credential;
		this.region = Region.prettyValueOf(region);

		this.login();
	}

	private void login() {

		String url = Gs2Constant.ENDPOINT_HOST + "/identifier-handler?handler=gs2_identifier%2Fhandler%2FProjectTokenFunctionHandler.login";

		ObjectNode _body = JsonNodeFactory.instance.objectNode();
		if(this.credential.getClientId() != null) {
			_body.put("clientId", this.credential.getClientId());
		}
		if(this.credential.getClientSecret() != null) {
			_body.put("clientSecret", this.credential.getClientSecret());
		}
		HttpPost http = createHttpPost(
				url,
				"identifier",
				_body.toString()
		);
		LoginResult result = doRequest(http, LoginResult.class);
		this.credential.setProjectToken(result.getItem().getToken());
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
	 * @param service アクセス先サービス
	 * @param body リクエストボディ
	 * @return リクエストオブジェクト
	 */
	protected HttpPost createHttpPost(String url, String service, String body) {
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "application/json");
		credential.authorized(post);
		post.setEntity(new StringEntity(body, "UTF-8"));
		return post;
	}

	/**
	 * POSTリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param service アクセス先サービス
	 * @param body リクエストボディ
	 * @return リクエストオブジェクト
	 */
	protected HttpPut createHttpPut(String url, String service, String body) {
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpPut put = new HttpPut(url);
		put.setHeader("Content-Type", "application/json");
		credential.authorized(put);
		put.setEntity(new StringEntity(body, "UTF-8"));
		return put;
	}

	/**
	 * GETリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param service アクセス先サービス
	 * @return リクエストオブジェクト
	 */
	protected HttpGet createHttpGet(String url, String service) {
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpGet get = new HttpGet(url);
		get.setHeader("Content-Type", "application/json");
		credential.authorized(get);
		return get;
	}

	/**
	 * DELETEリクエストを生成
	 * 
	 * @param url アクセス先URL
	 * @param service アクセス先サービス
	 * @return リクエストオブジェクト
	 */
	protected HttpDelete createHttpDelete(String url, String service) {
		url = StringUtils.replace(url, "{service}", service);
		url = StringUtils.replace(url, "{region}", region.getName());
		HttpDelete delete = new HttpDelete(url);
		delete.setHeader("Content-Type", "application/json");
		credential.authorized(delete);
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
							byte[] b;
							try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
								while (true) {
									int read = in.read();
									if (read == -1) break;
									bout.write(read);
								}
								b = bout.toByteArray();
							}
//							System.out.println(new String(b));
							return mapper.readValue(b, clazz);
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
		}
	}
}
