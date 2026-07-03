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
package com.thitsaworks.operation_portal.component.misc.util;

import java.util.Random;

public class RandomNumber {

    public static String generate(int length) {

        String numbers = "1234567890";
        Random random = new Random();

        char[] otp = new char[length];

        for (int i = 0; i < length; i++) {

            otp[i] = numbers.charAt(random.nextInt(numbers.length()));

        }

        return String.valueOf(otp);

    }

}