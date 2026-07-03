/*
 * Copyright (c) 2024-2026 ThitsaWorks Pte. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thitsaworks.operation_portal.component.fspiop.model;

import com.thitsaworks.operation_portal.component.fspiop.model.FxQuotesPostRequestConversionTerms;
import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The object sent in the POST /fxQuotes request.
 **/

@JsonTypeName("FxQuotesPostRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-04T01:16:26.838346+06:30[Asia/Rangoon]")
public class FxQuotesPostRequest   {
  private @Valid String conversionRequestId;
  private @Valid FxQuotesPostRequestConversionTerms conversionTerms;

  /**
   **/
  public FxQuotesPostRequest conversionRequestId(String conversionRequestId) {
    this.conversionRequestId = conversionRequestId;
    return this;
  }

  
  @JsonProperty("conversionRequestId")
  @NotNull
  public String getConversionRequestId() {
    return conversionRequestId;
  }

  @JsonProperty("conversionRequestId")
  public void setConversionRequestId(String conversionRequestId) {
    this.conversionRequestId = conversionRequestId;
  }

  /**
   **/
  public FxQuotesPostRequest conversionTerms(FxQuotesPostRequestConversionTerms conversionTerms) {
    this.conversionTerms = conversionTerms;
    return this;
  }

  
  @JsonProperty("conversionTerms")
  @NotNull
  public FxQuotesPostRequestConversionTerms getConversionTerms() {
    return conversionTerms;
  }

  @JsonProperty("conversionTerms")
  public void setConversionTerms(FxQuotesPostRequestConversionTerms conversionTerms) {
    this.conversionTerms = conversionTerms;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FxQuotesPostRequest fxQuotesPostRequest = (FxQuotesPostRequest) o;
    return Objects.equals(this.conversionRequestId, fxQuotesPostRequest.conversionRequestId) &&
        Objects.equals(this.conversionTerms, fxQuotesPostRequest.conversionTerms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conversionRequestId, conversionTerms);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FxQuotesPostRequest {\n");
    
    sb.append("    conversionRequestId: ").append(toIndentedString(conversionRequestId)).append("\n");
    sb.append("    conversionTerms: ").append(toIndentedString(conversionTerms)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

