package com.machloop.fpc.cms.center.appliance.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.appliance.service.PacketAnalysisService;
import com.machloop.fpc.cms.center.appliance.vo.PacketAnalysisQueryVO;
import com.machloop.fpc.cms.center.appliance.vo.PacketDecodeVO;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年6月7日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class PacketAnalysisController {

  private static final String CHARSET_GB2312 = "GB2312";
  private static final String CHARSET_UTF8 = "UTF-8";
  private static final String CHARSET_UTF16 = "UTF-16";
  private static final String CHARSET_UTF32 = "UTF-32";
  private static final String CHARSET_UTF16BE = "UTF-16BE";
  private static final String CHARSET_ASCII = "ASCII";
  private static final String CHARSET_URLDECODE = "URLDECODE";
  private static final String CHARSET_HEX = "HEX";
  private static final String CHARSET_BASE64 = "BASE64";

  @Autowired
  private PacketAnalysisService packetAnalysisService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private SensorLogicalSubnetService sensorLogicalSubnetService;

  @GetMapping("/analyzable-object")
  @Secured({"PERM_USER"})
  public Map<String, List<String>> queryAnalyzableObject(
      @RequestParam(required = false, defaultValue = "") String sourceType,
      @RequestParam(required = false) String networkGroupId,
      @RequestParam(required = false) String networkId,
      @RequestParam(required = false) String serviceId) {
    Map<String, List<String>> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Map<String, String> physicsNetworkMap = fpcNetworkService.queryAllNetworks().stream()
        .collect(Collectors.toMap(FpcNetworkBO::getFpcNetworkId, FpcNetworkBO::getFpcSerialNumber));
    Map<String,
        String> subnetMap = sensorLogicalSubnetService.querySensorLogicalSubnets().stream()
            .collect(Collectors.toMap(SensorLogicalSubnetBO::getId,
                SensorLogicalSubnetBO::getNetworkInSensorIds));
    switch (sourceType) {
      case FpcCmsConstants.SOURCE_TYPE_NETWORK:
        getNetworkInSensor(physicsNetworkMap, subnetMap, networkId, result);
        break;
      case FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP:
        SensorNetworkGroupBO networkGroup = sensorNetworkGroupService
            .querySensorNetworkGroup(networkGroupId);
        CsvUtils.convertCSVToList(networkGroup.getNetworkInSensorIds()).forEach(oneNetworkId -> {
          getNetworkInSensor(physicsNetworkMap, subnetMap, oneNetworkId, result);
        });
        break;
      case FpcCmsConstants.SOURCE_TYPE_SERVICE:
        List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (StringUtils.isNotBlank(networkGroupId)) {
          // 网络组
          SensorNetworkGroupBO networkGroupTemp = sensorNetworkGroupService
              .querySensorNetworkGroup(networkGroupId);
          networkIds.addAll(CsvUtils.convertCSVToList(networkGroupTemp.getNetworkInSensorIds()));
        } else {
          // 单个网络
          networkIds.add(networkId);
        }
        networkIds.forEach(
            oneNetworkId -> getNetworkInSensor(physicsNetworkMap, subnetMap, oneNetworkId, result));
        break;
      default:
        // 主网
        physicsNetworkMap.keySet().forEach(physicsNetworkId -> getNetworkInSensor(physicsNetworkMap,
            subnetMap, physicsNetworkId, result));
        // 子网
        subnetMap.keySet().forEach(
            subnetId -> getNetworkInSensor(physicsNetworkMap, subnetMap, subnetId, result));
        break;
    }

    Map<String, String> fpcStats = fpcService.queryAllFpc().stream()
        .collect(Collectors.toMap(FpcBO::getSerialNumber, FpcBO::getConnectStatus));

    return result.entrySet().stream()
        .filter(entry -> fpcStats.containsKey(entry.getKey()) && StringUtils
            .equals(fpcStats.get(entry.getKey()), FpcCmsConstants.CONNECT_STATUS_NORMAL))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private void getNetworkInSensor(Map<String, String> physicsNetworkMap,
      Map<String, String> subnetMap, String networkId, Map<String, List<String>> result) {
    if (physicsNetworkMap.containsKey(networkId)) {
      List<String> list = result.getOrDefault(physicsNetworkMap.get(networkId),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      list.add(networkId);

      result.put(physicsNetworkMap.get(networkId), list);
    } else {
      CsvUtils.convertCSVToList(subnetMap.get(networkId)).stream()
          .filter(physicsNetworkId -> physicsNetworkMap.containsKey(physicsNetworkId))
          .forEach(physicsNetworkId -> {
            String sensorSerialNumber = physicsNetworkMap.get(physicsNetworkId);

            List<String> list = result.getOrDefault(sensorSerialNumber,
                Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
            list.add(networkId);

            result.put(sensorSerialNumber, list);
          });
    }
  }

  @GetMapping("/packets")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFlowPackets(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {

    Map<String, Object> result = packetAnalysisService.queryFlowPackets(queryVO, request);
    int count = (Arrays.asList(result.get("result")).size()) - 1;
    String appendContent = "查询条目数=" + count + "。";

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_QUERY, queryVO, appendContent);
    return result;
  }

  @GetMapping("/packets/as-refines")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFlowPacketRefines(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {

    return packetAnalysisService.queryFlowPacketRefines(queryVO, request);
  }

  @GetMapping("/packets/file-urls")
  @Secured({"PERM_USER"})
  public String queryFlowPacketDownloadUrl(PacketAnalysisQueryVO queryVO,
      HttpServletRequest request) {
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DOWNLOAD, queryVO);
    return packetAnalysisService.queryFlowPacketDownloadUrl(queryVO, request);
  }

  @GetMapping("/packets/analysis")
  @Secured({"PERM_USER"})
  public void analysisFlowPacket(PacketAnalysisQueryVO queryVO, @RequestParam String type,
      @RequestParam String parameter, HttpServletRequest request, HttpServletResponse response) {

    packetAnalysisService.analyzeFlowPacket(queryVO, type, parameter, request, response);
  }

  @PostMapping("/packets/analysis/transcoding")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> transcoding(
      @RequestBody @Validated PacketDecodeVO packetDecodeVO) {
    try {
      List<Map<String, Object>> result = packetDecodeVO.getPayloads().stream().map(item -> {
        String d = MapUtils.getString(item, "d");
        switch (packetDecodeVO.getType()) {
          case CHARSET_GB2312:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_GB2312)));
            break;
          case CHARSET_ASCII:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_ASCII)));
            break;
          case CHARSET_UTF8:
            item.put("d", new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF8)));
            break;
          case CHARSET_UTF16:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF16)));
            break;
          case CHARSET_UTF32:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF32)));
            break;
          case CHARSET_UTF16BE:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF16BE)));
            break;
          case CHARSET_BASE64:
            item.put("d", new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF8)));
            break;
          case CHARSET_URLDECODE:
            d = new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_UTF8));
            try {
              item.put("d", new String(URLDecoder.decode(d, CHARSET_UTF8)));
            } catch (UnsupportedEncodingException e) {
              throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "解码异常");
            } catch (IllegalArgumentException e) {
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的格式，解码失败");
            }
            break;
          case CHARSET_HEX:
            item.put("d", HexUtils.toHexString(Base64.getDecoder().decode(d)));
            break;
          default:
            item.put("d",
                new String(Base64.getDecoder().decode(d), Charset.forName(CHARSET_ASCII)));
            break;
        }
        return item;
      }).collect(Collectors.toList());

      return result;
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "解码异常");
    }
  }

  @PostMapping("/packets/stop")
  @Secured({"PERM_USER"})
  public Map<String, Object> stopSearchFlowPackets(@RequestParam String fpcSerialNumber,
      @RequestParam String queryId, HttpServletRequest request) {
    return packetAnalysisService.stopSearchFlowPackets(fpcSerialNumber, queryId, request);
  }

  @PostMapping("/packets/as-refines/stop")
  @Secured({"PERM_USER"})
  public Map<String, Object> stopFlowPacketRefines(@RequestParam String fpcSerialNumber,
      @RequestParam String queryId, HttpServletRequest request) {
    return packetAnalysisService.stopFlowPacketRefines(fpcSerialNumber, queryId, request);
  }

}