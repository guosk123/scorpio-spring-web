package com.machloop.fpc.manager.system.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.system.service.SystemUpgradeService;

/**
 * @author guosk
 *
 * create at 2021年1月14日, fpc-manager
 */
@Service
public class SystemUpgradeServiceImpl implements SystemUpgradeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemUpgradeServiceImpl.class);

  private Map<String, String> versionCache;
  private long lastLoadTime;

  private static final int UPGRADE_TTL_SECONDS = 5 * 60;

  private static final String UPGRADE_START_TIME = "upgrade_start_time";
  private static final String UPGRADE_END_TIME = "upgrade_end_time";
  private static final String UPGRADE_STATE = "upgrade_state";

  private static final String UPGRADE_STATE_RUNNING = "running";
  private static final String UPGRADE_STATE_EXCEPTION = "exception";

  private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

  /**
   * @see com.machloop.fpc.manager.system.service.SystemUpgradeService#queryCurrentUpgradeVersion()
   */
  @Override
  public Map<String, String> queryCurrentUpgradeVersion() {
    String versionPath = HotPropertiesHelper.getProperty("file.upgrade.version.path");
    long currentLoadTime = Paths.get(versionPath).toFile().lastModified();
    if (versionCache == null || this.lastLoadTime != currentLoadTime) {
      synchronized (this) {
        this.versionCache = readVersionProperties(versionPath);
        this.lastLoadTime = currentLoadTime;
      }
    }

    // 判断升级是否超时
    if (StringUtils.equals(versionCache.get("upgradeState"), UPGRADE_STATE_RUNNING)) {
      Date now = DateUtils.now();
      String upgradeStartTimeStr = versionCache.get("upgradeStartTime");
      Date upgradeStartTime = DateUtils.parseISO8601Date(upgradeStartTimeStr);
      String updatePath = HotPropertiesHelper.getProperty("file.upgrade.script.path");
      if (((now.getTime() - upgradeStartTime.getTime()) > UPGRADE_TTL_SECONDS * 1000)
          && !isExistTaskProcess(updatePath)) {
        Map<String,
            String> properties = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        properties.put(UPGRADE_STATE, UPGRADE_STATE_EXCEPTION);
        updateVersionProperties(properties);
      }
    }

    return versionCache;
  }

  private Map<String, String> readVersionProperties(String versionPath) {
    Map<String, String> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Properties properties = new Properties();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new FileReader(versionPath));
      properties.load(bufferedReader);

      result.put("installVersion", properties.getProperty("install_version"));
      result.put("installFileName", properties.getProperty("install_file_name"));
      result.put("installReleaseTime", properties.getProperty("install_release_time"));
      result.put("upgradeVersion", properties.getProperty("upgrade_version"));
      result.put("upgradeFileName", properties.getProperty("upgrade_file_name"));
      result.put("upgradeReleaseTime", properties.getProperty("upgrade_release_time"));
      result.put("upgradeStartTime", properties.getProperty("upgrade_start_time"));
      result.put("upgradeEndTime", properties.getProperty("upgrade_end_time"));
      result.put("upgradeState", properties.getProperty("upgrade_state"));
    } catch (IOException e) {
      LOGGER.warn("read file error:" + versionPath);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "查询失败，系统出现异常");
    } finally {
      try {
        if (bufferedReader != null) {
          bufferedReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemUpgradeService#queryUpgradeLogs()
   */
  @Override
  public Map<String, Object> queryUpgradeLogs(long cursor) {
    Map<String, Object> result = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> logs = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    result.put("logs", logs);
    result.put("cursor", cursor);

    // 查询升级状态
    queryCurrentUpgradeVersion();
    result.put("state", versionCache.get("upgradeState"));

    // 首次安装或未找到升级日志
    String logPath = HotPropertiesHelper.getProperty("file.upgrade.log.path");
    File logFile = Paths.get(logPath).toFile();
    if (StringUtils.isBlank(versionCache.get("upgradeState")) || !logFile.exists()) {
      return result;
    }

    RandomAccessFile randomFile = null;
    try {
      long len = logFile.length();
      if (len < cursor) {
        LOGGER.warn("upgrade file reset or cursor illegal.");
      } else {
        randomFile = new RandomAccessFile(logFile, "rw");
        randomFile.seek(cursor);

        String tmp = "";
        while ((tmp = randomFile.readLine()) != null) {
          logs.add(StringUtils.toEncodedString(tmp.getBytes(), Charset.forName("utf-8")));
          cursor = randomFile.getFilePointer();
        }
      }
    } catch (IOException e) {
      LOGGER.warn("read the upgeade log file failed.", e);
    } finally {
      try {
        if (randomFile != null) {
          randomFile.close();
        }
      } catch (IOException e) {
        LOGGER.warn("randomAccessFile close failed.", e);
      }
    }

    result.put("cursor", cursor);
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemUpgradeService#systemUpgrade(org.springframework.web.multipart.MultipartFile)
   */
  @Override
  public void systemUpgrade(MultipartFile file) {
    LOGGER.info("detect when uploading a file and start processing.");
    String packageRootPath = HotPropertiesHelper.getProperty("file.upgrade.software.path");
    String updateScriptPath = HotPropertiesHelper.getProperty("file.upgrade.script.path");

    // 检查是否有升级脚本正在执行
    if (isExistTaskProcess(updateScriptPath)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "正在升级，请稍后");
    }

    try {
      // 将升级包存放到指定位置
      File packageRootDirectory = Paths.get(packageRootPath).toFile();
      if (!packageRootDirectory.exists()) {
        packageRootDirectory.mkdirs();
      }
      Path encryptPackagePath = Paths
          .get(packageRootPath + File.separator + file.getOriginalFilename());
      file.transferTo(encryptPackagePath);
      LOGGER.info("copy the file [{}] to the specified location :[{}].", file.getOriginalFilename(),
          encryptPackagePath.toString());

      // 解压升级包前先删除原有的升级包解压目录
      String updateDirectory = StringUtils.substringBeforeLast(updateScriptPath, File.separator);
      FileUtils.deleteDirectory(Paths.get(updateDirectory).toFile());

      // 升级包解密解压
      LOGGER.info("decrypt and uncompression upgrade package file: {},target parent path: {}.",
          encryptPackagePath.toString(), packageRootPath);
      if (!decryptAndUncompression(encryptPackagePath.toString(), packageRootPath)) {
        FileUtils.deleteQuietly(encryptPackagePath.toFile());
        LOGGER.warn("upgrade failed, decrypt or uncompression file failed.");
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "升级出现异常，升级包解密解压失败");
      }

      if (!Paths.get(updateScriptPath).toFile().exists()) {
        FileUtils.deleteQuietly(encryptPackagePath.toFile());
        LOGGER.warn(
            "upgrade failed, decrypt or uncompression file failed, can not found upgrade script.");
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "升级出现异常，升级包解密解压失败，未找到升级脚本");
      }

      // 更新version信息
      Map<String, String> properties = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      properties.put(UPGRADE_START_TIME, DateUtils.toStringISO8601(DateUtils.now()));
      properties.put(UPGRADE_END_TIME, null);
      properties.put(UPGRADE_STATE, UPGRADE_STATE_RUNNING);
      updateVersionProperties(properties);
      LOGGER.info("updated version information: {}", JsonHelper.serialize(properties));

      // 添加定时任务异步执行升级脚本
      singleThreadExecutor.execute(new Runnable() {

        @Override
        public void run() {
          // 执行脚本
          LOGGER.info("create the AT task to execute the upgrade script.");
          Process process = null;
          try {
            ProcessBuilder builder = new ProcessBuilder("sh", "-c",
                "echo 'sh " + updateScriptPath + "' | at now +1 minutes");
            builder.redirectErrorStream(true);

            process = builder.start();

            process.waitFor();
          } catch (InterruptedException e) {
            LOGGER.warn("execution at task failed.", e);
            Thread.currentThread().interrupt();
          } catch (IOException e) {
            LOGGER.warn("execution at task failed.", e);
          } finally {
            if (process != null) {
              process.destroy();
            }
          }
        }

      });

    } catch (IllegalStateException | IOException e) {
      LOGGER.warn("upload upgrade file failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，升级包写入失败");
    }
  }

  private boolean isExistTaskProcess(String keyword) {
    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("sh", "-c", "ps aux|grep " + keyword);
      builder.redirectErrorStream(true);

      process = builder.start();
      // 获取输出信息
      StringBuilder result = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          result.append(line);
        }
      } catch (IOException e) {
        LOGGER.warn("failed to read result from update process.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_COMMAND_RUN_ERROR, "系统出现异常");
      }

      process.waitFor();

      if (result.toString().contains("/bin/bash " + keyword)) {
        return true;
      }
    } catch (InterruptedException e) {
      LOGGER.warn("execution cmd failed.", e);
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("execution cmd failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return false;
  }

  private void updateVersionProperties(Map<String, String> properties) {
    String versionPath = HotPropertiesHelper.getProperty("file.upgrade.version.path");

    BufferedReader reader = null;
    BufferedWriter write = null;
    try {
      List<String> lines = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

      File file = Paths.get(versionPath).toFile();
      reader = new BufferedReader(new FileReader(file));
      String line = "";
      while ((line = reader.readLine()) != null) {
        for (Entry<String, String> entry : properties.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          if (line.startsWith(key)) {
            line = key + "=";
            if (StringUtils.isNotBlank(value)) {
              line += value;
            }
          }
        }
        lines.add(line + "\n");
      }
      write = new BufferedWriter(new FileWriter(file));
      for (String lineContent : lines) {
        write.write(lineContent);
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "查询失败，系统出现异常");
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
        if (write != null) {
          write.close();
        }
      } catch (IOException e) {
      }
    }
  }

  private boolean decryptAndUncompression(String inputFilePath, String outFilePath) {
    boolean success = true;
    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("sh", "-c", "dd if='" + inputFilePath
          + "' | openssl des3 -md sha256 -d -k tfa@123 | tar zxf - -C " + outFilePath);
      builder.redirectErrorStream(true);

      process = builder.start();
      process.waitFor();

    } catch (InterruptedException e) {
      success = false;
      LOGGER.warn("execution cmd failed.", e);
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      success = false;
      LOGGER.warn("execution cmd failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return success;
  }

  @SuppressWarnings("unused")
  private void uncompression(String sourcePath, String targetPath) throws IOException {
    Path source = Paths.get(sourcePath);
    Path target = Paths.get(targetPath);

    try (InputStream is = Files.newInputStream(source);
        BufferedInputStream bi = new BufferedInputStream(is);
        GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
        TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

      ArchiveEntry entry;
      while ((entry = ti.getNextEntry()) != null) {
        // 获取解压文件目录，并判断文件是否损坏
        Path innerPath = zipSlipProtect(entry, target);

        if (entry.isDirectory()) {
          // 创建解压文件目录
          Files.createDirectories(innerPath);
        } else {
          // 再次校验解压文件目录是否存在
          Path parent = innerPath.getParent();
          if (parent != null) {
            if (Files.notExists(parent)) {
              Files.createDirectories(parent);
            }
          }

          // 将解压的文件写入到指定位置
          Files.copy(ti, innerPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private Path zipSlipProtect(ArchiveEntry entry, Path targetDir) {
    Path normalizePath = targetDir.resolve(entry.getName()).normalize();

    if (!normalizePath.startsWith(targetDir)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          String.format("升级包内文件 [%s] 已被损坏", entry.getName()));
    }

    return normalizePath;
  }

}
