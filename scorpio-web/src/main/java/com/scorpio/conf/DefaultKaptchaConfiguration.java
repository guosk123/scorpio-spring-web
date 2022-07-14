package com.scorpio.conf;

import java.security.SecureRandom;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.text.TextProducer;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.Configurable;

@Configuration
public class DefaultKaptchaConfiguration {

  @Bean
  public DefaultKaptcha configDefaultKaptcha() {
    DefaultKaptcha kaptcha = new DefaultKaptcha();
    Properties properties = new Properties();
    properties.setProperty("kaptcha.border", "yes");
    properties.setProperty("kaptcha.border.color", "105,179,90");
    properties.setProperty("kaptcha.textproducer.font.color", "blue");
    properties.setProperty("kaptcha.image.width", "110");
    properties.setProperty("kaptcha.image.height", "40");
    properties.setProperty("kaptcha.textproducer.font.size", "30");
    properties.setProperty("kaptcha.session.key", "code");
    properties.setProperty("kaptcha.textproducer.char.length", "4");
    properties.setProperty("kaptcha.textproducer.impl", SecureRandomTextCreator.class.getName());
    Config config = new Config(properties);
    kaptcha.setConfig(config);

    return kaptcha;
  }

  class SecureRandomTextCreator extends Configurable implements TextProducer {
    /**
     * @return the SecureRandom text
     */
    public String getText() {
      int length = getConfig().getTextProducerCharLength();
      char[] chars = getConfig().getTextProducerCharString();
      SecureRandom rand = new SecureRandom();
      StringBuffer text = new StringBuffer();
      for (int i = 0; i < length; i++) {
        text.append(chars[rand.nextInt(chars.length)]);
      }

      return text.toString();
    }
  }

}
