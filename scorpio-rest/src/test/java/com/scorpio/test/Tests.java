package com.scorpio.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.scorpio.rest.dos.TestI;

/**
 * @author guosk
 *
 * create at 2021年2月7日, alpha-zurich-rest
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Tests {

  @Autowired
  private RestTemplate restTemplate;
  
  @Autowired
  @Qualifier("testB")
  private TestI test;
  
  @Test
  public void say() {
    System.out.println(test.say());
  }

  @Test
  public void post() {
    // get
    /*String urlget = "http://10.0.1.148:41113/fpc-fs/v1/packets/refines?queryId=60dd7f10-3884-11ec-a976-31a97478a917&X-Machloop-Date=2021-10-29T14%3A48%3A58%2B08%3A00&X-Machloop-Credential=wNbPynwBg6qZWdKkCoF4&X-Machloop-Signature=10763e164ca2a1da89b7a7f9942659e5a679e136d41124aa4eca70283d7544ea2f6c057ad3ec7211235b6d035277c16db04ba077b67d7302f7719066f6665bc1";
    String resultGet = null;
    try {
      resultGet = restTemplate.getForObject(urlget, String.class);
    } catch (RestClientException e) {
      e.printStackTrace();
    } finally {
      System.out.println("resultGet: " + resultGet);
    }*/

    // stop
    String urlpost = "http://10.0.1.148:41113/fpc-fs/v1/packets/refines/stop?queryId=b246f830-3899-11ec-ac2b-3b0153bc911d&X-Machloop-Date=2021-10-29T17%3A21%3A30%2B08%3A00&X-Machloop-Credential=dshay3wBWG2-86-ssBh-&X-Machloop-Signature=d48dae31faf65231455ba1ff2f6ace2c76f33ab202be69b5089a2eb7744881ab317698705563d131d76365caa048f736604ad6bf74042d61eaf1110ec9bbaaed";
    String resultPost = null;
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Connection", "keep-alive");
      headers.set("Keep-Alive", "timeout=5");
      resultPost = restTemplate.postForObject(urlpost, new HttpEntity<String>(headers),
          String.class);
    } catch (RestClientException e) {
      e.printStackTrace();
    } finally {
      System.out.println("resultPost: " + resultPost);
    }
  }

}
