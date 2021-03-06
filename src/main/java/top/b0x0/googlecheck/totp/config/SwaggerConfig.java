package top.b0x0.googlecheck.totp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口文档地址: http://localhost:8081/doc.html
 * swagger json: http://127.0.0.1:8081/v2/api-docs
 * @author TANG
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        //可以添加多个header或参数
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        //参数类型支持header, cookie, body, query etc
        parameterBuilder.parameterType("header")
                //参数名
                .name("token")
                //默认值
                .defaultValue("")
                .description("用户token")
                //指定参数值的类型
                .modelRef(new ModelRef("string"))
                //非必需，这里是全局配置，然而在登陆的时候是不用验证的
                .required(false)
                .build();
        List<Parameter> parameterList = new ArrayList<>();
        parameterList.add(parameterBuilder.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(productApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("top.b0x0.googlecheck.totp.controller"))
                .build()
                // 全局参数
                .globalOperationParameters(parameterList);
    }

    /**
     * name:开发者姓名
     * url:开发者网址
     * email:开发者邮箱
     *
     * @return /
     */
    private ApiInfo productApiInfo() {
        return new ApiInfoBuilder()
                .title("google check")
                // 文档接口的描述
                .description("springboot集成谷歌验证器")
                .contact(new Contact("TANG", "", ""))
                // 版本号
                .version("1.0.0")
                .build();
    }


    /**
     * 防止@EnableMvc把默认的静态资源路径覆盖了，手动设置的方式
     *
     * @param registry /
     */
/*    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath*:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath*:/META-INF/resources/webjars/");
    }*/
}
