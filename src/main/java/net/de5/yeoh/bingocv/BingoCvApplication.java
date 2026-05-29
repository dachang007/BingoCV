package net.de5.yeoh.bingocv;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@MapperScan("net.de5.yeoh.bingocv.mapper")
@CrossOrigin("*")
public class BingoCvApplication {

	public static void main(String[] args) {
		SpringApplication.run(BingoCvApplication.class, args);
	}

}
