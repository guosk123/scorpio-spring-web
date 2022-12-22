package com.machloop.fpc.manager.restapi;

import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.global.library.LicenseLibrary;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.service.MonitorMetricDataService;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * @author guosk
 *
 * create at 2021年6月26日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/system")
public class SystemRestAPIController {

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private MonitorMetricDataService monitorMetricService;

  @GetMapping("/product-infos")
  @RestApiSecured
  public RestAPIResultVO queryProductInfos() {
    Map<String, String> infoMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 调用so获取序列号
    IntByReference lenghtReference = new IntByReference();
    Pointer esnPointer = new Memory(128);
    LicenseLibrary license = LicenseLibrary.INSTANCE;
    license.license_get_esn(esnPointer, lenghtReference);
    String serialNumber = esnPointer.getString(0);
    // 释放内存
    long peer = Pointer.nativeValue(esnPointer);
    Native.free(peer);
    // 避免Memory对象被GC时重复执行Nativ.free()方法
    Pointer.nativeValue(esnPointer, 0);
    infoMap.put("serialNumber", serialNumber);

    infoMap.put("series", HotPropertiesHelper.getProperty("product.series"));
    String productVersion = HotPropertiesHelper.getProperty("product.version");
    if (StringUtils.isBlank(productVersion)) {
      infoMap.put("version", buildProperties.get("machloop.prod.version"));
    } else {
      infoMap.put("version", productVersion);
    }

    return RestAPIResultVO.resultSuccess(infoMap);
  }

  /**
   * 提供系统状态-运行状态restapi接口
   * @param startTime restapi中为可为空
   * @param endTime restapi中可为空
   * @param interval 时间间隔
   * @return
   */
  @GetMapping("/monitor-metrics/as-histogram")
  @RestApiSecured
  public RestAPIResultVO statMonitorMetric(@RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false, defaultValue = "60") int interval) {

    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      try {
        DateUtils.parseISO8601Date(startTime);
        DateUtils.parseISO8601Date(endTime);
      } catch (DateTimeParseException e) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
            .build();
      }
      List<Map<String, Object>> statMonitorMetricList = monitorMetricService
          .statMonitorMetricData(startTime, endTime, interval);
      // 只留存使用率，去掉剩余容量
      for (Map<String, Object> statMonitorMetricDatum : statMonitorMetricList) {
        Iterator<String> iterator = statMonitorMetricDatum.keySet().iterator();
        while (iterator.hasNext()) {
          String key = iterator.next();
          if (StringUtils.equalsAny(key, "systemFsFree", "indexFsFree", "metadataFsFree",
              "metadataHotFsFree", "packetFsFree")) {
            iterator.remove();
          }
        }
      }
      return RestAPIResultVO.resultSuccess(statMonitorMetricList);
    } else {
      List<Map<String, Object>> statMonitorMetricList = monitorMetricService
          .queryLatestStatMonitorMetricData();
      return RestAPIResultVO.resultSuccess(statMonitorMetricList);
    }
  }
}
