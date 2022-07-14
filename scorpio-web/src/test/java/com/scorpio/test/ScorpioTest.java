package com.scorpio.test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.lang3.time.DateUtils;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.scorpio.boot.WebApplication;

import java.util.Date;

@SpringBootTest(classes = WebApplication.class)
public class ScorpioTest {

  @Autowired
  private StringEncryptor encryptor;

  @Test
  public void passwordEncode() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    System.out.println(encoder.encode("scorpio@123"));
  }

  @Test
  public void encryptor() {
    System.setProperty("jasypt.encryptor.password", "scorpio@123");

    String encrypt = encryptor.encrypt("scorpio@123");
    System.out.println(encrypt);

    System.out.println(encryptor.decrypt(encrypt));
  }

  @Test
  public void jwt() {
    String secret = "abcd";
    String username = "scorpio";
    Date expire = DateUtils.addDays(new Date(), 1);

    Algorithm algorithm = Algorithm.HMAC256(secret);

    String jwt = JWT.create().withClaim("username", username).withClaim("token", secret)
            .withExpiresAt(expire)
            .withIssuedAt(new Date(946656000)).withNotBefore(new Date(946656000)).sign(algorithm);

    System.out.println(jwt);
  }

}
