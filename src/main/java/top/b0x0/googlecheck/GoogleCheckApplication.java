package top.b0x0.googlecheck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author TANG
 */
@MapperScan("top.b0x0.googlecheck.totp.mapper")
@SpringBootApplication
public class GoogleCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleCheckApplication.class, args);
    }

}
