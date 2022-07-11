package com.scorpio.test;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * @author guosk
 *
 * create at 2021年3月2日, alpha-zurich-rest
 */
public class JwtTest {

  public static void main(String[] args) {
    String secret = "MMmisr24XRZks7bXTc3GyZZPKBn7wRZf";
    String platformId = "sso_test";
    String platformUserId = "sso_gsk";
    Date expire = DateUtils.addDays(new Date(), 1);

    Algorithm algorithm = Algorithm.HMAC256(secret);

    String token = JWT.create().withClaim("platform_id", platformId)
        .withClaim("platform_user_id", platformUserId).withExpiresAt(expire)
        .withIssuedAt(new Date(946656000)).withNotBefore(new Date(946656000)).sign(algorithm);

    System.out.println(token);
  }

}
