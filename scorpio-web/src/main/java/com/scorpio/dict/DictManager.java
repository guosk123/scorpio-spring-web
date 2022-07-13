package com.scorpio.dict;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class DictManager implements ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(DictManager.class);

  @Value("${file.dict.path}")
  private String baseDictPath;// 通用字典

  private DictDefinition baseDict;

  private ApplicationContext applicationContext;

  @PostConstruct
  public void initDict() throws IOException {

    Resource baseDictResource = null;
    if (baseDictPath.startsWith("classpath")) {
      baseDictResource = applicationContext.getResource(baseDictPath);
    } else {
      baseDictResource = applicationContext.getResource("file://" + baseDictPath);
    }

    baseDict = parseDictDefinition(baseDictResource.getInputStream(), DictDefinition.class);
  }

  protected DictDefinition parseDictDefinition(InputStream is,
      Class<? extends DictDefinition> targetType) {
    try {
      JAXBContext context = JAXBContext.newInstance(targetType);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      DictDefinition dictDefinition = (DictDefinition) unmarshaller
          .unmarshal(new InputStreamReader(is, StandardCharsets.UTF_8));
      List<Dict> dicts = dictDefinition.getDicts();
      Map<String, Dict> dictMap = Maps.newHashMapWithExpectedSize(dicts.size());
      for (Dict dict : dicts) {
        dictMap.put(dict.getKey(), dict);
      }
      dictDefinition.setDictMap(dictMap);
      return dictDefinition;
    } catch (JAXBException e) {
      LOGGER.warn("Parse dict definition failed.", e);
    }
    return null;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    synchronized (this) {
      this.applicationContext = applicationContext;
    }
  }

  public DictDefinition getBaseDict() {
    return baseDict;
  }

}
