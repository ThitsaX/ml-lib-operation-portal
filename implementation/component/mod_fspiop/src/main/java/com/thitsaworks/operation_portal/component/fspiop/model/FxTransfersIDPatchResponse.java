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
 * PATCH /fxTransfers/{ID} object
 **/

@JsonTypeName("FxTransfersIDPatchResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-04T01:16:26.838346+06:30[Asia/Rangoon]")
public class FxTransfersIDPatchResponse   {
  private @Valid String completedTimestamp;
  private @Valid TransferState conversionState;
  private @Valid ExtensionList extensionList;

  /**
   **/
  public FxTransfersIDPatchResponse completedTimestamp(String completedTimestamp) {
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
  public FxTransfersIDPatchResponse conversionState(TransferState conversionState) {
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
  public FxTransfersIDPatchResponse extensionList(ExtensionList extensionList) {
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
    FxTransfersIDPatchResponse fxTransfersIDPatchResponse = (FxTransfersIDPatchResponse) o;
    return Objects.equals(this.completedTimestamp, fxTransfersIDPatchResponse.completedTimestamp) &&
        Objects.equals(this.conversionState, fxTransfersIDPatchResponse.conversionState) &&
        Objects.equals(this.extensionList, fxTransfersIDPatchResponse.extensionList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(completedTimestamp, conversionState, extensionList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FxTransfersIDPatchResponse {\n");
    
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

