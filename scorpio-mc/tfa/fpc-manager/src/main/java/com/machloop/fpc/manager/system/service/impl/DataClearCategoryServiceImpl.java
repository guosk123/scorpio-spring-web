package com.machloop.fpc.manager.system.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Charsets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.security.bo.LoggedUser;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.fpc.manager.system.service.DataClearCategoryService;

/**
 * @author chenshimiao
 *
 * create at 2022/9/15 10:27 AM, fpc-manager
 * @version 1.0
 */
@Service
public class DataClearCategoryServiceImpl implements DataClearCategoryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataClearCategoryServiceImpl.class);

  String fileDataClearPath = HotPropertiesHelper.getProperty("file.data.clear.path");

  private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

  @Autowired
  private DictManager dictManager;

  @Override
  public Map<String, String> queryDataClearCategory() {

    Map<String, String> systemDataClear = dictManager.getBaseDict().getItemMap("system_data_clear");
    return systemDataClear;
  }

  @Override
  public void clearData(List<String> param, String id) {

    // 获取用户访问地址
    String address = acquireRemoteAddrFromRequest();
    // 异步执行
    singleThreadExecutor.execute(new Runnable() {
      @Override
      public void run() {
        Process process = null;
        try {
          String content = "";
          for (String s : param) {
            content = content + " " + s;
          }
          ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c",
              fileDataClearPath + content);
          LOGGER.info("开始执行命令");
          processBuilder.redirectErrorStream(true);

          process = processBuilder.start();

          // 将脚本日志输出到项目日志中
          StringBuilder result = new StringBuilder();
          try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
              result.append(line + "\n");
            }
            LOGGER.info(result.toString());
          } catch (IOException e) {
            LOGGER.warn("failed to read result from process.", e);
            throw new BusinessException(ErrorCode.COMMON_BASE_COMMAND_RUN_ERROR, "查询失败，系统出现异常");
          }

          process.waitFor();
        } catch (InterruptedException e) {
          LOGGER.warn("execution at task failed.", e);
          printLog(param, "清理数据失败: ", address);
          Thread.currentThread().interrupt();
        } catch (IOException e) {
          LOGGER.warn("execution at task failed.", e);
          printLog(param, "清理数据失败：", address);
        } finally {
          if (process != null) {
            process.destroy();
          }
          printLog(param, "清理的数据为：", address);
        }
      }
    });
  }

  private void printLog(List<String> param, String content, String address) {
    LoggedUser user = LoggedUserContext.getCurrentUser();

    LogDO logDO = new LogDO();
    logDO.setLevel(LogHelper.LEVEL_NOTICE);
    logDO.setCategory(LogHelper.CATEGORY_CLEAR);
    logDO.setComponent(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_COMPONENT));
    logDO.setAriseTime(DateUtils.now());
    Map<String, String> systemDataClear = dictManager.getBaseDict().getItemMap("system_data_clear");
    String info = "" + content;
    for (String s : param) {
      info = info + systemDataClear.get(s) + "，";
    }
    info = info.substring(0, info.length() - 1);
    logDO.setContent(info);
    logDO.setSource(user.getFullname() + "/" + user.getUsername() + "（" + address + "）");

    LogHelper.saveLog(logDO);
  }

  private static String acquireRemoteAddrFromRequest() {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getRequest();
    if (org.apache.commons.lang.StringUtils.isNotBlank(request.getHeader("x-forwarded-for"))
        && !org.apache.commons.lang.StringUtils.equalsIgnoreCase("unknown",
            request.getHeader("x-forwarded-for"))) {
      return org.apache.commons.lang.StringUtils
          .substringBefore(request.getHeader("x-forwarded-for"), ",");
    }
    return org.apache.commons.lang.StringUtils.defaultIfBlank(request.getRemoteAddr(), "");
  }
}
