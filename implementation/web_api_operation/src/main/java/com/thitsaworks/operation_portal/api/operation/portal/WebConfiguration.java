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
package com.thitsaworks.operation_portal.api.operation.portal;

import com.thitsaworks.operation_portal.core.reporting.download.generator.ReportGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(
        Settings settings) {

        return factory -> factory.setPort(settings.getPortNo());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
            .addMapping("/**")
            .allowedOrigins(settings().getUrl().split(","))
            .allowedMethods("*");
    }

    @Bean
    public Settings settings() {

        String port = System.getProperty("OPERATION_PORTAL_PORT_NO");
        String frontendUrls = System.getProperty("OPERATION_PORTAL_FRONTEND_ENDPOINT");
        return new Settings(Integer.parseInt(port), frontendUrls);
    }

    @Bean
    public ReportGenerator.Settings reportSettings() {

        return new ReportGenerator.Settings(
            Integer.parseInt(System.getProperty("REPORT_PAGE_SIZE", "50000")));
    }

    @Bean
    public OperationPortalApiConfiguration.SupportCenterSettings supportCenterSettings() {

        return new OperationPortalApiConfiguration.SupportCenterSettings(
            System.getProperty("DISPUTE_URL", ""), System.getProperty("SERVICE_REQUEST_URL", ""));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Settings {

        private int portNo;

        private String url;

    }

}
