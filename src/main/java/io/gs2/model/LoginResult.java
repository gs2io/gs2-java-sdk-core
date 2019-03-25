/*
 * Copyright 2016 Game Server Services, Inc. or its affiliates. All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.gs2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * プロジェクトトークン を取得します のレスポンスモデル
 *
 * @author Game Server Services, Inc.
 */
@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown=true)
public class LoginResult implements Serializable {
	/** プロジェクトトークン */
	private Gs2ProjectToken item;

	/**
	 * プロジェクトトークンを取得
	 *
	 * @return プロジェクトトークン を取得します
	 */
	public Gs2ProjectToken getItem() {
		return item;
	}

	/**
	 * プロジェクトトークンを設定
	 *
	 * @param item プロジェクトトークン を取得します
	 */
	public void setItem(Gs2ProjectToken item) {
		this.item = item;
	}
}