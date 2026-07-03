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
package com.thitsaworks.operation_portal.component.misc.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContext implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContext.class);

    private static ApplicationContext applicationContext;

    public static <T> T getBean(Class<T> clazz) {

        return SpringContext.applicationContext.getBean(clazz);

    }

    public static <T> T getBean(Class<T> clazz, String qualifier) {

        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(applicationContext.getAutowireCapableBeanFactory(), clazz,
                qualifier);

    }

    public static Object getBean(String beanName) {

        return SpringContext.applicationContext.getBean(beanName);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        LOG.info("applicationContext is set : {}", applicationContext);

        SpringContext.applicationContext = applicationContext;

    }

}