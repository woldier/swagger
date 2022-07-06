package com.woldier.swagger.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import springfox.documentation.service.Contact;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * swagger的属性类，用于封装相关接口，可以从配置文件读入
 */
/**========================code=====================================
 * 传统的配置方式为如下（多分组）
 * 我们现在要做的就是把这些配置信息仅需提取形成一个配置属性类
 * =============================================================
 * @EnableSwagger2 //开启swagger
 * @Configuration //定义为配置类
 * public class SwaggerAutoConfiguration {
 *     @Bean
 *     public Docket createRestApi1() {
 *         Docket docket = new Docket(DocumentationType.SWAGGER_2)
 *                 .apiInfo(apiInfo()).groupName("用户接口组")
 *                 .select()
 *                 //为当前包路径
 *                 .apis(RequestHandlerSelectors.basePackage("com.woldier.swagger.controller.user"))
 *                 .build();
 *         return docket;
 *     }
 *
 *     @Bean
 *     public Docket createRestApi2() {
 *         Docket docket = new Docket(DocumentationType.SWAGGER_2)
 *                 .apiInfo(apiInfo()).groupName("菜单接口组")
 *                 .select()
 *                 //为当前包路径
 *                 .apis(RequestHandlerSelectors.basePackage("com.woldier.swagger.controller.menu"))
 *                 .build();
 *         return docket;
 *     }
 *
 *     //构建 api文档的详细信息
 *     private ApiInfo apiInfo() {
 *         return new ApiInfoBuilder()
 *                 //页面标题
 *                 .title("API接口文档")
 *                 //创建人
 *                 .contact(new Contact("woldier", "http://www.woldier.com", ""))
 *                 //版本号
 *                 .version("1.0")
 *                 //描述
 *                 .description("API 描述")
 *                 .build();
 *     }
 * }
 * ========================code=====================================
 */
@Data //set get method
@ConfigurationProperties(prefix = "wd-auth.swagger") //前缀
public class SwaggerProperties {
    /*自动配置的默认值*/
    private String title = "在线文档"; //标题
    private String group = ""; //自定义组名
    private String description = "在线文档"; //描述
    private String version = "1.0"; //版本
    private Contact contact = new Contact(); //联系人
    private String basePackage = "com.woldier.auth"; //swagger会解析的包路径
    private List<String> basePath = new ArrayList<>(); //swagger会解析的url规则
    private List<String> excludePath = new ArrayList<>();//在basePath基础上需要排除的url规则

    /*此为分组时的文档
    * 如不分组的话会加载前面的通用配置 只有一个docket
    * 需要分组则需要给docket相应的信息 有多个docket
    * */
    private Map<String, DocketInfo> docket = new LinkedHashMap<>(); //分组文档


    /*内部类 用于封装分组文档的信息*/
    @Data
    public static class DocketInfo {
        private String title = "在线文档"; //标题
        private String group = ""; //自定义组名
        private String description = "在线文档"; //描述
        private String version = "1.0"; //版本
        private Contact contact = new Contact(); //联系人
        private String basePackage = ""; //swagger会解析的包路径
        private List<String> basePath = new ArrayList<>(); //swagger会解析的url规则
        private List<String> excludePath = new ArrayList<>();//在basePath基础上需要排除的url
        public String getGroup() {
            if (group == null || "".equals(group)) {
                return title;
            }
            return group;
        }
    }
    /*内部类 用于封装联系方式的信息*/
    @Data
    public static class Contact {
        private String name = "pinda"; //联系人
        private String url = ""; //联系人url
        private String email = ""; //联系人email
    }
}
