package com.machloop.fpc.manager.asset.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.clickhouse.client.internal.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.manager.asset.bo.OSNameBO;
import com.machloop.fpc.manager.asset.dao.AssetAlarmDao;
import com.machloop.fpc.manager.asset.dao.AssetDeviceDao;
import com.machloop.fpc.manager.asset.dao.AssetInformationDao;
import com.machloop.fpc.manager.asset.dao.AssetOSDao;
import com.machloop.fpc.manager.asset.data.OSNameDO;
import com.machloop.fpc.manager.asset.service.AssetConfigurationService;
import com.machloop.fpc.manager.asset.service.AssetInformationService;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;
import com.machloop.alpha.common.util.ExportUtils.FetchData;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月2日, fpc-manager
 */
@Service
public class AssetInformationServcieImpl implements AssetInformationService {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
  static {
    fields.put("ipAddress", "IP地址");
    fields.put("deviceType", "设备类型");
    fields.put("os", "操作系统");
    fields.put("port", "开放端口");
    fields.put("label", "业务标签");
    fields.put("firstTime", "上报时间");
    fields.put("timestamp", "最后更新时间");
  }

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private AssetInformationDao assetInformationDao;

  @Autowired
  private AssetConfigurationService assetConfigurationService;

  @Autowired
  private AssetAlarmDao assetAlarmDao;

  @Autowired
  private AssetDeviceDao assetDeviceDao;

  @Autowired
  private AssetOSDao assetOSDao;

  private static final Map<String, Object> typeMap = Maps.newHashMapWithExpectedSize(4);

  static {
    typeMap.put("1", "deviceType");// 设备类型
    typeMap.put("2", "port");// 监听端口
    typeMap.put("3", "label");// 服务标签
    typeMap.put("4", "os");// 操作系统
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetInformationService#queryAssetInformation(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.lang.String, java.lang.String, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<Map<String, Object>> queryAssetInformation(AssetInformationQueryVO queryVO,
      String sortProperty, String sortDirection, Pageable page) {

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(queryVO.getIpAddress())) {
      queryVO.setIp(queryVO.getIpAddress());
    }

    // 是否有资产告警
    List<String> alarmIpList = StringUtils.equals(queryVO.getAlarm(), Constants.BOOL_YES)
        ? assetAlarmDao.queryAssetAlarms(null, 0).stream().map(item -> item.getIpAddress())
            .distinct().collect(Collectors.toList())
        : Lists.newArrayListWithExpectedSize(0);

    if (CollectionUtils.isEmpty(alarmIpList)
        && StringUtils.equals(queryVO.getAlarm(), Constants.BOOL_YES)) {
      return new PageImpl<>(result, page, 0);
    }

    List<Map<String, Object>> assetInformationList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getStartTime()) && StringUtils.isBlank(queryVO.getEndTime())
        && !StringUtils.equals(sortProperty, "firstTime")) {
      assetInformationList = assetInformationDao.queryAssetsWithValue(queryVO, null, alarmIpList,
          page, 0);
    } else if (StringUtils.isAllBlank(queryVO.getDeviceType(), queryVO.getOs(), queryVO.getPort(),
        queryVO.getIpAddress())
        && !StringUtils.equals(sortProperty, "timestamp")
        && (StringUtils.isNotBlank(queryVO.getStartTime())
            || StringUtils.isNotBlank(queryVO.getEndTime())
            || StringUtils.equals(sortProperty, "firstTime"))) {
      assetInformationList = assetInformationDao.queryAssetsWithFirstTime(queryVO, alarmIpList,
          page, 0);
    } else {
      assetInformationList = assetInformationDao
          .queryAssetsWithValueAndFirstTime(queryVO.getQueryId(), queryVO, alarmIpList, page, 0);
    }
    result = aggregateAssetInformationList(queryVO, assetInformationList);

    // 排序
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        if (StringUtils.equals(sortProperty, "timestamp")
            || StringUtils.equals(sortProperty, "firstTime")) {
          String o1Value = MapUtils.getString(o1, TextUtils.underLineToCamel(sortProperty));
          String o2Value = MapUtils.getString(o2, TextUtils.underLineToCamel(sortProperty));
          return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
              ? o1Value.compareTo(o2Value)
              : o2Value.compareTo(o1Value);
        } else {
          // ip地址排序
          BigDecimal o1Value = ip2Long(
              MapUtils.getString(o1, TextUtils.underLineToCamel(sortProperty)));
          BigDecimal o2Value = ip2Long(
              MapUtils.getString(o2, TextUtils.underLineToCamel(sortProperty)));
          return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
              ? o1Value.compareTo(o2Value)
              : o2Value.compareTo(o1Value);
        }
      }
    });

    result.forEach(item -> {
      item.put("ipAddress", MapUtils.getString(item, "ip"));
      item.remove("ip");
    });

    return new PageImpl<>(result, page, 0);
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetInformationService#queryAssetInformation(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.lang.String, java.lang.String, int, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public List<Map<String, Object>> queryAssetInformation(AssetInformationQueryVO queryVO,
      String sortProperty, String sortDirection, int count, Pageable page) {
    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(queryVO.getIpAddress())) {
      queryVO.setIp(queryVO.getIpAddress());
    }

    // 是否有资产告警
    List<String> alarmIpList = StringUtils.equals(queryVO.getAlarm(), Constants.BOOL_YES)
        ? assetAlarmDao.queryAssetAlarms(null, 0).stream().map(item -> item.getIpAddress())
            .distinct().collect(Collectors.toList())
        : Lists.newArrayListWithExpectedSize(0);

    if (CollectionUtils.isEmpty(alarmIpList)
        && StringUtils.equals(queryVO.getAlarm(), Constants.BOOL_YES)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    List<Map<String, Object>> assetInformationList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getStartTime()) && StringUtils.isBlank(queryVO.getEndTime())
        && !StringUtils.equals(sortProperty, "firstTime")) {
      assetInformationList = assetInformationDao.queryAssetsWithValue(queryVO, null, alarmIpList,
          page, count);
    } else if (StringUtils.isAllBlank(queryVO.getDeviceType(), queryVO.getOs(), queryVO.getPort(),
        queryVO.getIpAddress())
        && (StringUtils.isNotBlank(queryVO.getStartTime())
            || StringUtils.isNotBlank(queryVO.getEndTime())
            || StringUtils.equals(sortProperty, "firstTime"))) {
      assetInformationList = assetInformationDao.queryAssetsWithFirstTime(queryVO, alarmIpList,
          page, count);
    } else {
      assetInformationList = assetInformationDao.queryAssetsWithValueAndFirstTime(
          queryVO.getQueryId(), queryVO, alarmIpList, page, count);
    }
    result = aggregateAssetInformationList(queryVO, assetInformationList);

    // 排序
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        if (StringUtils.equals(sortProperty, "timestamp")
            || StringUtils.equals(sortProperty, "firstTime")) {
          String o1Value = MapUtils.getString(o1, TextUtils.underLineToCamel(sortProperty));
          String o2Value = MapUtils.getString(o2, TextUtils.underLineToCamel(sortProperty));
          return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
              ? o1Value.compareTo(o2Value)
              : o2Value.compareTo(o1Value);
        } else {
          // ip地址排序
          BigDecimal o1Value = ip2Long(
              MapUtils.getString(o1, TextUtils.underLineToCamel(sortProperty)));
          BigDecimal o2Value = ip2Long(
              MapUtils.getString(o2, TextUtils.underLineToCamel(sortProperty)));
          return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
              ? o1Value.compareTo(o2Value)
              : o2Value.compareTo(o1Value);
        }
      }
    });

    result.forEach(item -> {
      item.put("ipAddress", MapUtils.getString(item, "ip"));
      item.remove("ip");
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetInformationService#aggregateAssetInformationList(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> aggregateAssetInformationList(AssetInformationQueryVO queryVO,
      List<Map<String, Object>> assetInformationList) {

    Map<String, Object> OSMap = assetConfigurationService.queryAssetOS();

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    Map<String, List<Map<String, Object>>> ipAssetMap = assetInformationList.stream()
        .collect(Collectors.groupingBy(e -> MapUtils.getString(e, "ip")));

    for (String ip : ipAssetMap.keySet()) {
      List<Map<String, Object>> tempList = ipAssetMap.get(ip);
      List<String> deviceTypeList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      List<String> osList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      List<String> portList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      List<String> labelList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (Map<String, Object> temp : tempList) {
        if (!temp.containsKey("type")) {
          result.add(temp);
        } else {
          String type = MapUtils.getString(temp, "type");
          if (StringUtils.equals(type, "1")) {
            deviceTypeList.add(MapUtils.getString(temp, "value1"));
          } else if (StringUtils.equals(type, "2")) {
            portList.add(MapUtils.getString(temp, "value1"));
          } else if (StringUtils.equals(type, "3")) {
            labelList.add(MapUtils.getString(temp, "value1"));
          } else {
            osList.add(MapUtils.getString(OSMap, MapUtils.getString(temp, "value1")) + " "
                + MapUtils.getString(temp, "value2"));
          }
        }
      }
      String timestamp = MapUtils.getString(tempList.stream()
          .max(Comparator.comparing(e -> MapUtils.getString(e, "timestamp"))).get(), "timestamp");
      String firstTime = MapUtils.getString(tempList.stream()
          .max(Comparator.comparing(e -> MapUtils.getString(e, "firstTime"))).get(), "firstTime");
      Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("ip", ip);
      map.put("deviceType", CsvUtils.convertCollectionToCSV(deviceTypeList));
      map.put("os", CsvUtils.convertCollectionToCSV(osList));
      map.put("port", CsvUtils
          .convertCollectionToCSV(portList.stream().distinct().collect(Collectors.toList())));
      map.put("label", CsvUtils.convertCollectionToCSV(labelList));
      map.put("timestamp", timestamp);
      map.put("firstTime", firstTime);
      map.put("alarm", assetAlarmDao.queryAssetAlarms(ip, 0).size());
      result.add(map);
    }
    return result;

    //
    //
    // // 获取ip、type、value对应关系，示例：{10.0.0.19={1=[1, 2, 3], 2=[8000, 443], 4=[Linux 3.11 and newer]}
    // HashMap<String, HashMap<String, List<String>>> ipTypeValuesMap =
    // assetInformationList.stream()
    // .collect(Collectors.groupingBy(e -> MapUtils.getString(e, "ip"), HashMap::new,
    // Collectors.groupingBy(t -> MapUtils.getString(t, "type"), HashMap::new,
    // Collectors.mapping(v -> MapUtils.getString(v, "value1"), Collectors.toList()))));
    //
    // Map<String, List<Map<String, Object>>> ipAssetMap = assetInformationList.stream()
    // .collect(Collectors.groupingBy(e -> MapUtils.getString(e, "ip")));
    //
    // for (String ipAddress : ipAssetMap.keySet()) {
    // String timestamp = MapUtils.getString(
    // ipAssetMap.get(ipAddress).stream()
    // .max(Comparator.comparing(e -> MapUtils.getString(e, "timestamp"))).get(),
    // "timestamp");
    // String firstTime = MapUtils.getString(
    // ipAssetMap.get(ipAddress).stream()
    // .max(Comparator.comparing(e -> MapUtils.getString(e, "firstTime"))).get(),
    // "firstTime");
    // Map<String, List<String>> typeValuesMap = ipTypeValuesMap.get(ipAddress);
    // Map<String,
    // Object> assetInformation = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // assetInformation.put("ip", ipAddress);
    // assetInformation.put("timestamp", timestamp);
    // assetInformation.put("firstTime", firstTime);
    // assetInformation.put("alarm", assetAlarmDao.queryAssetAlarms(ipAddress, 0).size());
    // for (String type : typeValuesMap.keySet()) {
    // // 去重，针对type为端口号的情况
    // assetInformation.put(MapUtils.getString(typeMap, type), CsvUtils.convertCollectionToCSV(
    // typeValuesMap.get(type).stream().distinct().collect(Collectors.toList())));
    // Map<String,
    // Object> map = ipAssetMap.get(ipAddress).stream()
    // .filter(item -> StringUtils.equals(MapUtils.getString(item, "type"), type))
    // .collect(Collectors.toList()).get(0);
    // // 只返回操作系统类型的value2
    // if (StringUtils.equals(type, "4")) {
    // assetInformation.put(MapUtils.getString(typeMap, type) + "Value2", map.get("value2"));
    // }
    // }
    // result.add(assetInformation);
    // }
    // return result;
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetInformationService#exportAssetInformations(javax.servlet.ServletOutputStream, com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void exportAssetInformations(ServletOutputStream outputStream,
      AssetInformationQueryVO queryVO, String fileType, String sortProperty, String sortDirection)
      throws IOException {

    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    titles.addAll(fields.values());

    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
    FileUtils.touch(tempFile);

    int batchSize = 1000;

    final int total = (int) assetInformationDao.countAssetInformation(queryVO, null);
    FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0 && offset < total;
      }

      @Override
      public List<List<String>> next() {
        Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
            new Order(Sort.Direction.DESC, "ip"));
        int pageNum = offset / batchSize;
        int pageSize = total > offset && total < (offset + batchSize) ? (total - offset)
            : batchSize;
        PageRequest page = new PageRequest(pageNum, pageSize, sort);

        List<Map<String, Object>> assetInformationList = queryAssetInformation(queryVO,
            sortProperty, sortDirection, 0, page);

        // 避免死循环
        if (assetInformationList.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataset = convertAssetList2LineList(assetInformationList);

        offset += dataset.size();

        return dataset;
      }
    };
    // 导出数据
    ExportUtils.export(titles, fetchData, tempFile, fileType, outputStream);
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetInformationService#countAssetInformation(com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO)
   */
  @Override
  public long countAssetInformation(AssetInformationQueryVO queryVO) {

    long result = 0L;

    List<String> alarmIpList = StringUtils.equals(queryVO.getAlarm(), Constants.BOOL_YES)
        ? assetAlarmDao.queryAssetAlarms(null, 0).stream().map(item -> item.getIpAddress())
            .distinct().collect(Collectors.toList())
        : Lists.newArrayListWithExpectedSize(0);

    result = assetInformationDao.countAssetInformation(queryVO, alarmIpList);

    return result;
  }

  private List<List<String>> convertAssetList2LineList(List<Map<String, Object>> tempList) {

    Map<String,
        String> deviceTypeDict = assetDeviceDao.queryAssetDevices().stream()
            .collect(Collectors.toMap(item -> MapUtils.getString(item, "id"),
                item -> MapUtils.getString(item, "device_name")));
    Map<String, String> osNameDict = assetOSDao.queryAssetOS("").stream()
        .collect(Collectors.toMap(OSNameDO::getId, OSNameDO::getOs));

    List<List<String>> lines = Lists.newArrayListWithCapacity(tempList.size() + 1);

    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    titles.addAll(fields.values());
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    for (Map<String, Object> temp : tempList) {
      List<String> values = titles.stream().map(title -> {

        String value = "";
        String field = columnNameMap.get(title);

        switch (field) {
          case "ipAddress":
            value = MapUtils.getString(temp, field, "");
            break;
          case "deviceType":
            value = MapUtils.getString(deviceTypeDict, MapUtils.getString(temp, field));
            break;
          case "os":
            value = StringUtils.isBlank(MapUtils.getString(temp, "osValue2"))
                ? MapUtils.getString(osNameDict, MapUtils.getString(temp, "os"), "")
                : MapUtils.getString(osNameDict, MapUtils.getString(temp, "os")) + " "
                    + MapUtils.getString(temp, "osValue2");
            break;
          case "port":
            value = MapUtils.getString(temp, field, "");
            break;
          case "label":
            value = MapUtils.getString(temp, field, "");
            break;
          case "firstTime":
          case "timestamp":
            value = MapUtils.getString(temp, field, "");
            break;
          default:
            value = "";
            break;
        }

        return value;
      }).collect(Collectors.toList());

      lines.add(values);
    }
    return lines;
  }

  private BigDecimal ip2Long(String ipAddress) {

    String separator = "\\.";
    int radix = 10;
    int ipByteNumber = Constants.IPV4_ADDRESS_BYTES;
    int byteNumber = Constants.BYTE_BITS;
    if (NetworkUtils.isInetAddress(ipAddress, IpVersion.V6)) {
      separator = "\\:";
      ipAddress = getFullIPv6(ipAddress);
      ipByteNumber = 8;
      radix = 16;
      byteNumber = Constants.BYTE_BITS * 2;
    }

    BigDecimal result = new BigDecimal(0);

    if (StringUtils.isBlank(ipAddress)) {
      return result;
    }

    try {
      String[] ipAddressInArray = ipAddress.split(separator);
      for (int i = 0; i < ipByteNumber; i++) {
        // 乘数
        BigDecimal rawNumber = new BigDecimal(Integer.parseInt(ipAddressInArray[i], radix));
        // 被乘数
        BigDecimal multiplicand = new BigDecimal(Math.pow(2, (ipByteNumber - i - 1) * byteNumber));
        result = result.add(rawNumber.multiply(multiplicand));
      }
    } catch (NumberFormatException e) {
      result = new BigDecimal(0);
    }
    return result;
  }

  private String getFullIPv6(String ipv6) {
    if (StringUtils.isBlank(ipv6)) {
      return "";
    }
    // 入参为::时，此时全为0
    if (ipv6.equals("::")) {
      return "0000:0000:0000:0000:0000:0000:0000:0000";
    }
    // 入参以::结尾时，直接在后缀加0
    if (ipv6.endsWith("::")) {
      ipv6 += "0";
    }
    String[] arrs = ipv6.split(":");
    String symbol = "::";
    int arrleng = arrs.length;
    while (arrleng < 8) {
      symbol += ":";
      arrleng++;
    }
    ipv6 = ipv6.replace("::", symbol);
    String fullip = "";
    for (String ip : ipv6.split(":")) {
      while (ip.length() < 4) {
        ip = "0" + ip;
      }
      fullip += ip + ':';
    }
    return fullip.substring(0, fullip.length() - 1);
  }

}

