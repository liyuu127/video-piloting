package com.liyu.piloting.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liyu
 * date 2021/5/7 15:51
 * description
 */

public class SpringContextUtils implements BeanFactoryPostProcessor {

    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        SpringContextUtils.beanFactory = configurableListableBeanFactory;
    }

    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    public static <T> T getBean(Class<T> clz) throws BeansException {
        T result = beanFactory.getBean(clz);
        return result;
    }

    public static <T> List<T> getBeansOfType(Class<T> type) {
        return beanFactory.getBeansOfType(type).entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    public static List<Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        Map<String, Object> beansWithAnnotation = beanFactory.getBeansWithAnnotation(annotationType);

        return beansWithAnnotation.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());

    }
}

