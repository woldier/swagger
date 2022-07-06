package com.woldier.swagger.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;


/**
 * 自动配置类
 */


/**
 * ========================code=====================================
 * 传统的配置方式为如下（多分组）
 * 我们现在要做的就是把这些配置信息仅需提取形成一个配置属性类
 * =============================================================
 *
 * @EnableSwagger2 //开启swagger
 * @Configuration //定义为配置类
 * public class SwaggerAutoConfiguration {
 * @Bean public Docket createRestApi1() {
 * Docket docket = new Docket(DocumentationType.SWAGGER_2)
 * .apiInfo(apiInfo()).groupName("用户接口组")
 * .select()
 * //为当前包路径
 * .apis(RequestHandlerSelectors.basePackage("com.woldier.swagger.controller.user"))
 * .build();
 * return docket;
 * }
 * @Bean public Docket createRestApi2() {
 * Docket docket = new Docket(DocumentationType.SWAGGER_2)
 * .apiInfo(apiInfo()).groupName("菜单接口组")
 * .select()
 * //为当前包路径
 * .apis(RequestHandlerSelectors.basePackage("com.woldier.swagger.controller.menu"))
 * .build();
 * return docket;
 * }
 * <p>
 * //构建 api文档的详细信息
 * private ApiInfo apiInfo() {
 * return new ApiInfoBuilder()
 * //页面标题
 * .title("API接口文档")
 * //创建人
 * .contact(new Contact("woldier", "http://www.woldier.com", ""))
 * //版本号
 * .version("1.0")
 * //描述
 * .description("API 描述")
 * .build();
 * }
 * }
 * ========================code=====================================
 */


@Configuration //定义为配置类


@ConditionalOnProperty(name = "wd-auth.swagger.enabled", havingValue = "true",
        matchIfMissing = true)  //当wd-auth.swagger.enabled属性为ture时才启用该自动配置类，若不存在则匹配成功，
// 此注解加入的功能主要是为了能在非开发环境关闭该功能

@EnableSwagger2 //开启swagger
@EnableConfigurationProperties(SwaggerProperties.class) //启用属性类


public class SwaggerAutoConfiguration
        implements BeanFactoryAware { //实现了beanAware接口用于注入bean工厂 （ps需要实现对应接口方法，该方法的作用就是注入bean）


    private BeanFactory beanFactory; //BeanFactory用于注册 一个或者多个Docket bean

    @Autowired
    private SwaggerProperties swaggerProperties; //从配置文件读入属性


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    @Bean
    @ConditionalOnMissingBean //当不存在 List<Docket> bean的时候创建
    public List<Docket> createSingle() {
        ConfigurableBeanFactory configurableBeanFactory =
                (ConfigurableBeanFactory) beanFactory; //强制类型转化，其目的时为了可以进行bean注册功能。

        List<Docket> docketList = new ArrayList<>();

        /*当docket分组为空时（代表是单分组）*/
        if (swaggerProperties.getDocket().isEmpty()) {
            Docket docket = createDocket(swaggerProperties); //的搭配一个docket

            /*注册到beanFactory*/
            configurableBeanFactory.registerSingleton(swaggerProperties.getTitle(),docket); //第一个为bean的名字，第二个为bean

            docketList.add(docket);
        } else {
            /*否则代表通过docket分组配置*/
            /*遍历所有的docket*/
            for (String groupName : swaggerProperties.getDocket().keySet()) {
                /*取出分组的信息*/
                SwaggerProperties.DocketInfo docketInfo = swaggerProperties.getDocket().get(groupName);

                /*封装api info*/
                ApiInfo apiInfo = new ApiInfoBuilder()
                        //页面标题
                        .title(docketInfo.getTitle())
                        //创建人
                        .contact(new Contact(docketInfo.getContact().getName(),
                                docketInfo.getContact().getUrl(),
                                docketInfo.getContact().getEmail()))
                        //版本号
                        .version(docketInfo.getVersion())
                        //描述
                        .description(docketInfo.getDescription())
                        .build();

                // base-path处理
                // 当没有配置任何path的时候，解析/**
                if (docketInfo.getBasePath().isEmpty()) {
                    docketInfo.getBasePath().add("/**");
                }
                List<Predicate<String>> basePath = new ArrayList<>();
                for (String path : docketInfo.getBasePath()) {
                    basePath.add(PathSelectors.ant(path));
                }

                // exclude-path处理
                List<Predicate<String>> excludePath = new ArrayList<>();
                for (String path : docketInfo.getExcludePath()) {
                    excludePath.add(PathSelectors.ant(path));
                }

                Docket docket = new Docket(DocumentationType.SWAGGER_2)
                        .apiInfo(apiInfo).
                        groupName(docketInfo.getGroup())
                        .select()
                        //为当前包路径
                        .apis(RequestHandlerSelectors.basePackage(docketInfo.getBasePackage()))
                        .paths(Predicates.and(Predicates.not(Predicates.or(excludePath)),Predicates.or(basePath)))
                        .build();
                /*注册到beanFactory*/
                configurableBeanFactory.registerSingleton(docketInfo.getTitle(),docket); //第一个为bean的名字，第二个为bean
                docketList.add(docket);
            }

        }

        return  docketList;
    }


    /**
     * 创建一个单一的docket
     * @param swaggerProperties
     * @return
     */
    private Docket createDocket(SwaggerProperties swaggerProperties){
        // base-path处理
        // 当没有配置任何path的时候，解析/**
        if (swaggerProperties.getBasePath().isEmpty()) {
            swaggerProperties.getBasePath().add("/**");
        }
        List<Predicate<String>> basePath = new ArrayList<>();
        for (String path : swaggerProperties.getBasePath()) {
            basePath.add(PathSelectors.ant(path));
        }

        // exclude-path处理
        List<Predicate<String>> excludePath = new ArrayList<>();
        for (String path : swaggerProperties.getExcludePath()) {
            excludePath.add(PathSelectors.ant(path));
        }

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        //页面标题
                        .title(swaggerProperties.getTitle())
                        //创建人
                        .contact(
                                new Contact(swaggerProperties.getContact().getName(),
                                        swaggerProperties.getContact().getUrl(),
                                        swaggerProperties.getContact().getEmail())
                        )
                        //版本号
                        .version(swaggerProperties.getVersion())
                        //描述
                        .description(swaggerProperties.getDescription())
                        .build()
                ).groupName(swaggerProperties.getGroup())
                .select()
                //为当前包路径
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .paths(Predicates.and(Predicates.not(Predicates.or(excludePath)),Predicates.or(basePath)))
                .build();
        return docket;

    }


}
