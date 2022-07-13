package com.scorpio.helper;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 读取一个属性文件里的配置，且属性文件改变时，不用重启应用，自动读取新的值
 *
 */
@Component
@NotThreadSafe
public class HotPropertiesHelper implements ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(HotPropertiesHelper.class);

  private static final long RELOAD_TASK_PERIOD_SEC = 10;
  private static final long RELOAD_TASK_DELAY_SEC = 60;

  private static final String ENV_CONFIG = System.getenv("ENV_CONFIG");
  private static FileBasedConfiguration configurationCommon;
  private static FileBasedConfiguration configuration;
  private static FileBasedConfiguration configurationDev;

  @Value("${spring.config.location}")
  private String propertyFileLocation;

  private ApplicationContext applicationContext;

  private ScheduledExecutorService executorService;

  /**
   * 配置文件内容加密（ENC(****************)）
   */
  private static StringEncryptor encryptor;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    synchronized (this) {
      this.applicationContext = applicationContext;
    }
  }

  /**
   * 注入静态实例需要在方法上加@Autowired
   * 
   * @param encryptor
   */
  @Autowired
  public void setEncryptor(StringEncryptor encryptor) {
    HotPropertiesHelper.encryptor = encryptor;
  }

  @PostConstruct
  public void initConfigurationIfnessary() throws ConfigurationException, IOException {
    synchronized (this) {
      if (configuration != null) {
        return;
      }

      // application-dev > application > application-common

      String[] properties = propertyFileLocation.split(",");
      if (properties.length < 2) {
        // 多个配置文件，并且有优先级
        return;
      }

      Resource propertyFileCommon = null;
      Resource propertyFile = null;
      Resource propertyFileDev = null;
      if (propertyFileLocation.startsWith("classpath")) {
        propertyFileCommon = applicationContext.getResource(properties[0]);
        propertyFile = applicationContext.getResource(properties[1]);
        if (properties.length > 2) { // 解析application-dev
          propertyFileDev = applicationContext.getResource(properties[2]);
        }
      } else {
        propertyFileCommon = applicationContext.getResource("file://" + properties[0]);
        propertyFile = applicationContext.getResource("file://" + properties[1]);
      }

      LOGGER.info("Initialize apache common PropertiesConfiguration with resource {},{}",
          propertyFileCommon, propertyFile);

      // 初始化配置
      ReloadingFileBasedConfigurationBuilder<
          FileBasedConfiguration> builderFileCommon = new ReloadingFileBasedConfigurationBuilder<
              FileBasedConfiguration>(PropertiesConfiguration.class)
                  .configure(new Parameters().fileBased().setFile(propertyFileCommon.getFile()));
      configurationCommon = builderFileCommon.getConfiguration();

      ReloadingFileBasedConfigurationBuilder<
          FileBasedConfiguration> builderFile = new ReloadingFileBasedConfigurationBuilder<
              FileBasedConfiguration>(PropertiesConfiguration.class)
                  .configure(new Parameters().fileBased().setFile(propertyFile.getFile()));
      configuration = builderFile.getConfiguration();

      ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFileDev = null;
      if (propertyFileDev != null) {
        builderFileDev = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class)
                .configure(new Parameters().fileBased().setFile(propertyFileDev.getFile()));
        configurationDev = builderFileDev.getConfiguration();
      }

      // 设置定时更新
      if (executorService == null) {
        ThreadFactory factory = new BasicThreadFactory.Builder()
            .namingPattern("ReloadingTrigger-%s").daemon(true).build();
        executorService = Executors.newScheduledThreadPool(1, factory);
      }
      executorService.scheduleAtFixedRate(
          new ReloadingConfigurationThread(builderFileCommon, builderFile, builderFileDev),
          RELOAD_TASK_DELAY_SEC, RELOAD_TASK_PERIOD_SEC, TimeUnit.SECONDS);
    }
  }

  /**
   * 得到一个key对应的值 如果key在properties文件中配置，那么返回null
   * 
   * @param key
   * @return
   */
  public static String getProperty(String key) {
    if (configuration == null) {
      return "";
    }

    String devProperty = "";
    if (configurationDev != null) {
      devProperty = configurationDev.getString(key);
      if (StringUtils.isNotBlank(devProperty)) {
        // 如果获得的value值是密文，解密之后返回
        if (StringUtils.indexOf(devProperty, "ENC(") == 0) {
          devProperty = encryptor
              .decrypt(StringUtils.removeEnd(StringUtils.removeStart(devProperty, "ENC("), ")"));
        }

        return devProperty;
      }
    }

    String value = StringUtils.defaultIfBlank(configuration.getString(key),
        configurationCommon.getString(key, ""));
    if (StringUtils.indexOf(value, "ENC(") == 0) {
      value = encryptor.decrypt(StringUtils.removeEnd(StringUtils.removeStart(value, "ENC("), ")"));
    }

    value = value.replace("${ENV_CONFIG}", ENV_CONFIG);

    return value;
  }

  private static class ReloadingConfigurationThread implements Runnable {

    private final ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFileCommon;
    private final ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFile;
    private final ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFileDev;

    public ReloadingConfigurationThread(
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFileCommon,
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFile,
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builderFileDev) {
      super();
      this.builderFileCommon = builderFileCommon;
      this.builderFile = builderFile;
      this.builderFileDev = builderFileDev;
    }

    @Override
    public void run() {

      try {
        if (builderFileCommon.getReloadingController().checkForReloading(null)) {
          setConfigurationCommon(builderFileCommon.getConfiguration());
        }
        if (builderFile.getReloadingController().checkForReloading(null)) {
          setConfiguration(builderFile.getConfiguration());
        }
        if (builderFileDev != null
            && builderFileDev.getReloadingController().checkForReloading(null)) {
          setConfigurationDev(builderFileDev.getConfiguration());
        }
      } catch (ConfigurationException e) {
        LOGGER.warn("reload configuration files failed.", e);
      }
    }

  }

  public static void setConfigurationCommon(FileBasedConfiguration configurationCommon) {
    HotPropertiesHelper.configurationCommon = configurationCommon;
  }

  public static void setConfiguration(FileBasedConfiguration configuration) {
    HotPropertiesHelper.configuration = configuration;
  }

  public static void setConfigurationDev(FileBasedConfiguration configurationDev) {
    HotPropertiesHelper.configurationDev = configurationDev;
  }

}
