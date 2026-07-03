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

import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * U2F challenge-response, where payer FSP verifies if the response provided by end-user device matches the previously registered key. 
 **/

@JsonTypeName("U2FPinValue")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-07-04T01:16:26.838346+06:30[Asia/Rangoon]")
public class U2FPinValue   {
  private @Valid String pinValue;
  private @Valid String counter;

  /**
   * U2F challenge-response, where payer FSP verifies if the response provided by end-user device matches the previously registered key. 
   **/
  public U2FPinValue pinValue(String pinValue) {
    this.pinValue = pinValue;
    return this;
  }

  
  @JsonProperty("pinValue")
  @NotNull
 @Pattern(regexp="^\\S{1,64}$") @Size(min=1,max=64)  public String getPinValue() {
    return pinValue;
  }

  @JsonProperty("pinValue")
  public void setPinValue(String pinValue) {
    this.pinValue = pinValue;
  }

  /**
   * The API data type Integer is a JSON String consisting of digits only. Negative numbers and leading zeroes are not allowed. The data type is always limited to a specific number of digits.
   **/
  public U2FPinValue counter(String counter) {
    this.counter = counter;
    return this;
  }

  
  @JsonProperty("counter")
  @NotNull
 @Pattern(regexp="^[1-9]\\d*$")  public String getCounter() {
    return counter;
  }

  @JsonProperty("counter")
  public void setCounter(String counter) {
    this.counter = counter;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    U2FPinValue u2FPinValue = (U2FPinValue) o;
    return Objects.equals(this.pinValue, u2FPinValue.pinValue) &&
        Objects.equals(this.counter, u2FPinValue.counter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pinValue, counter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class U2FPinValue {\n");
    
    sb.append("    pinValue: ").append(toIndentedString(pinValue)).append("\n");
    sb.append("    counter: ").append(toIndentedString(counter)).append("\n");
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

