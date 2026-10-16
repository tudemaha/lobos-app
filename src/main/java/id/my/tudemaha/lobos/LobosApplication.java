package id.my.tudemaha.lobos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LobosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LobosApplication.class, args);
    }

}
