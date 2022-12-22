package com.machloop.fpc.cms.center.metadata.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.metadata.dao.FileRestoreInfoDao;
import com.machloop.fpc.cms.center.metadata.data.FileRestoreInfoDO;
import com.machloop.fpc.cms.center.metadata.service.FileRestoreInfoService;
import com.machloop.fpc.cms.center.metadata.vo.FileRestoreInfoVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

import reactor.util.function.Tuple2;

/**
 * @author ChenXiao
 * create at 2022/11/15
 */
@Service
public class FileRestoreInfoServiceImpl implements FileRestoreInfoService {

  @Autowired
  private FileRestoreInfoDao fileRestoreInfoDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private SensorNetworkService networkService;

  @Autowired
  private SensorLogicalSubnetService logicalSubnetService;


  @Autowired
  private ServletContext servletContext;

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);


  static {
    fields.put("timestamp", "还原时间");
    fields.put("flowId", "流ID");
    fields.put("networkId", "网络ID");
    fields.put("srcIp", "源IP");
    fields.put("srcPort", "源端口");
    fields.put("destIp", "目的IP");
    fields.put("destPort", "目的端口");
    fields.put("md5", "文件md5值");
    fields.put("sha1", "文件sha1值");
    fields.put("sha256", "文件sha256值");
    fields.put("name", "文件名称");
    fields.put("size", "文件大小");
    fields.put("magic", "文件类型");
    fields.put("l7Protocol", "应用层协议");
    fields.put("state", "还原状态");
  }

  @Override
  public Page<FileRestoreInfoVO> queryFileRestoreInfos(LogRecordQueryVO queryVO, PageRequest page) {

    List<String> ids = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    Page<FileRestoreInfoDO> fileRestoreInfoDOS = fileRestoreInfoDao.queryFileRestoreInfos(queryVO,
        ids, page);
    List<FileRestoreInfoVO> fileRestoreInfoVOS = Lists
        .newArrayListWithCapacity(fileRestoreInfoDOS.getSize());
    for (FileRestoreInfoDO fileRestoreInfoDO : fileRestoreInfoDOS) {
      FileRestoreInfoVO fileRestoreInfoVO = new FileRestoreInfoVO();
      BeanUtils.copyProperties(fileRestoreInfoDO, fileRestoreInfoVO);
      fileRestoreInfoVOS.add(fileRestoreInfoVO);
    }

    return new PageImpl<>(fileRestoreInfoVOS, page, fileRestoreInfoDOS.getTotalElements());
  }

  @Override
  public Map<String, Object> queryFileRestoreInfoStatistics(LogRecordQueryVO queryVO) {
    List<String> flowIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    long total = fileRestoreInfoDao.countLogRecords(queryVO, flowIds);

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", total);
    return result;
  }

  @Override
  public FileRestoreInfoVO queryFileRestoreInfo(LogRecordQueryVO queryVO, String id) {
    queryVO.setColumns(columnMapping(queryVO.getColumns()));
    FileRestoreInfoDO fileRestoreInfoDO = fileRestoreInfoDao.queryFileRestoreInfo(queryVO, id);
    FileRestoreInfoVO fileRestoreInfoVO = new FileRestoreInfoVO();
    BeanUtils.copyProperties(fileRestoreInfoDO, fileRestoreInfoVO);
    return fileRestoreInfoVO;
  }

  @Override
  public void exportLogRecords(LogRecordQueryVO queryVO, Sort sort, String fileType, int count,
      OutputStream out) throws IOException {

    List<String> analysisResultIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    // 前端传递的驼峰字段
    String columns = queryVO.getColumns();

    // 标题
    List<String> titles = convertLogDOList2LineList(Lists.newArrayListWithCapacity(0), columns)
        .get(0);

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
    FileUtils.touch(tempFile);

    // 获取id集合
    int maxCount = Integer
        .parseInt(HotPropertiesHelper.getProperty("export.protocol.log.max.count"));
    count = (count <= 0 || count > maxCount) ? maxCount : count;
    Tuple2<String, List<String>> idTuples = fileRestoreInfoDao.queryLogRecords(queryVO,
        analysisResultIds, sort, count);
    String tableName = idTuples.getT1();
    List<String> ids = idTuples.getT2();

    // 单次查询数量
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;
      private String dataColumns = columnMapping(columns);

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        // 获取数据
        List<String> tmpIds = ids.stream().skip(offset).limit(batchSize)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tmpIds)) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<FileRestoreInfoDO> tmp = fileRestoreInfoDao.queryFileRestoreInfosByIds(tableName,
            dataColumns, tmpIds, sort);
        List<List<String>> dataset = convertLogDOList2LineList(tmp, columns);
        // 去除标题
        dataset.remove(0);

        // 避免死循环
        if (dataset.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        offset += dataset.size();

        return dataset;
      }

    };

    // 导出数据
    ExportUtils.export(titles, fetchData, tempFile, fileType, out);


  }


  private String columnMapping(String columns) {

    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("network_id");

    CsvUtils.convertCSVToList(columns).forEach(item -> {
      columnSets.add(TextUtils.camelToUnderLine(item));
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  private List<List<String>> convertLogDOList2LineList(List<FileRestoreInfoDO> logDOList,
      String columns) {

    Map<String, String> fileRestoreStateDict = dictManager.getBaseDict()
        .getItemMap("file_restore_info_state");
    Map<String, String> networkDict = networkService.querySensorNetworks().stream()
        .collect(Collectors.toMap(SensorNetworkBO::getNetworkInSensorId, SensorNetworkBO::getName));
    networkDict.putAll(logicalSubnetService.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetBO::getId, SensorLogicalSubnetBO::getName)));

    List<List<String>> lines = Lists.newArrayListWithCapacity(logDOList.size() + 1);

    // title
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(columns, "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(columns);
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }
    lines.add(titles);

    // content
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    for (FileRestoreInfoDO fileRestoreInfoDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);
        String value = "";
        switch (field) {
          case "flowId":
            value = fileRestoreInfoDO.getFlowId();
            break;
          case "networkId":
            value = MapUtils.getString(networkDict, fileRestoreInfoDO.getNetworkId(), "");
            break;
          case "timestamp":
            value = fileRestoreInfoDO.getTimestamp();
            break;
          case "srcIp":
            value = fileRestoreInfoDO.getSrcIp();
            break;
          case "srcPort":
            value = String.valueOf(fileRestoreInfoDO.getSrcPort());
            break;
          case "destIp":
            value = fileRestoreInfoDO.getDestIp();
            break;
          case "destPort":
            value = String.valueOf(fileRestoreInfoDO.getDestPort());
            break;
          case "md5":
            value = fileRestoreInfoDO.getMd5();
            break;
          case "sha1":
            value = fileRestoreInfoDO.getSha1();
            break;
          case "sha256":
            value = fileRestoreInfoDO.getSha256();
            break;
          case "name":
            value = fileRestoreInfoDO.getName();
            break;
          case "size":
            long size = fileRestoreInfoDO.getSize();
            if (size == 0) {
              value = "0B";
            } else {
              value = readableFileSize(size);
            }
            break;
          case "magic":
            value = fileRestoreInfoDO.getMagic();
            break;
          case "l7Protocol":
            value = fileRestoreInfoDO.getL7Protocol();
            break;
          case "state":
            value = fileRestoreStateDict.getOrDefault(String.valueOf(fileRestoreInfoDO.getState()),
                "其他");
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

  private String readableFileSize(long size) {

    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
    return new DecimalFormat("#,###.##").format(size / Math.pow(1000, digitGroups)) + " "
        + units[digitGroups];
  }
}
