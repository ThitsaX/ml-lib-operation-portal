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

import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.fspiop.model.TransferState;
import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The object sent in the PUT /fxTransfers/{ID} callback.
 **/

@JsonTypeName("FxTransfersIDPutResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-04T01:16:26.838346+06:30[Asia/Rangoon]")
public class FxTransfersIDPutResponse   {
  private @Valid String fulfilment;
  private @Valid String completedTimestamp;
  private @Valid TransferState conversionState;
  private @Valid ExtensionList extensionList;

  /**
   **/
  public FxTransfersIDPutResponse fulfilment(String fulfilment) {
    this.fulfilment = fulfilment;
    return this;
  }

  
  @JsonProperty("fulfilment")
  public String getFulfilment() {
    return fulfilment;
  }

  @JsonProperty("fulfilment")
  public void setFulfilment(String fulfilment) {
    this.fulfilment = fulfilment;
  }

  /**
   **/
  public FxTransfersIDPutResponse completedTimestamp(String completedTimestamp) {
    this.completedTimestamp = completedTimestamp;
    return this;
  }

  
  @JsonProperty("completedTimestamp")
  public String getCompletedTimestamp() {
    return completedTimestamp;
  }

  @JsonProperty("completedTimestamp")
  public void setCompletedTimestamp(String completedTimestamp) {
    this.completedTimestamp = completedTimestamp;
  }

  /**
   **/
  public FxTransfersIDPutResponse conversionState(TransferState conversionState) {
    this.conversionState = conversionState;
    return this;
  }

  
  @JsonProperty("conversionState")
  @NotNull
  public TransferState getConversionState() {
    return conversionState;
  }

  @JsonProperty("conversionState")
  public void setConversionState(TransferState conversionState) {
    this.conversionState = conversionState;
  }

  /**
   **/
  public FxTransfersIDPutResponse extensionList(ExtensionList extensionList) {
    this.extensionList = extensionList;
    return this;
  }

  
  @JsonProperty("extensionList")
  public ExtensionList getExtensionList() {
    return extensionList;
  }

  @JsonProperty("extensionList")
  public void setExtensionList(ExtensionList extensionList) {
    this.extensionList = extensionList;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FxTransfersIDPutResponse fxTransfersIDPutResponse = (FxTransfersIDPutResponse) o;
    return Objects.equals(this.fulfilment, fxTransfersIDPutResponse.fulfilment) &&
        Objects.equals(this.completedTimestamp, fxTransfersIDPutResponse.completedTimestamp) &&
        Objects.equals(this.conversionState, fxTransfersIDPutResponse.conversionState) &&
        Objects.equals(this.extensionList, fxTransfersIDPutResponse.extensionList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fulfilment, completedTimestamp, conversionState, extensionList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FxTransfersIDPutResponse {\n");
    
    sb.append("    fulfilment: ").append(toIndentedString(fulfilment)).append("\n");
    sb.append("    completedTimestamp: ").append(toIndentedString(completedTimestamp)).append("\n");
    sb.append("    conversionState: ").append(toIndentedString(conversionState)).append("\n");
    sb.append("    extensionList: ").append(toIndentedString(extensionList)).append("\n");
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

