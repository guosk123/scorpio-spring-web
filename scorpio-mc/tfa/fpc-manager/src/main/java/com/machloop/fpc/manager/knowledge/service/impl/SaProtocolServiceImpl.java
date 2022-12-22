package com.machloop.fpc.manager.knowledge.service.impl;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.manager.knowledge.dao.SaProtocolDao;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;

/**
 * @author guosk
 *
 * create at 2020年12月5日, fpc-manager
 */
@Service
public class SaProtocolServiceImpl implements SaProtocolService {

  @Autowired
  private SaProtocolDao saProtocolDao;

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaProtocolService#queryProtocols(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryProtocols(String protocolName, String standard,
      String labelId) {
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 协议
    List<Map<String, String>> protocols = saProtocolDao.queryProtocols(knowledgeFilePath);
    // 标签
    List<Map<String, String>> protocolLabels = saProtocolDao.queryProtocolLabels(knowledgeFilePath);
    Map<String, String> labelDict = protocolLabels.stream().collect(Collectors.toMap(
        item -> MapUtils.getString(item, "labelId"), item -> MapUtils.getString(item, "nameText")));

    // 过滤是否标准
    if (StringUtils.isNotBlank(standard)) {
      protocols = protocols.stream()
          .filter(item -> StringUtils.equals(standard, MapUtils.getString(item, "standard")))
          .collect(Collectors.toList());
    }

    // 过滤协议名称
    if (StringUtils.isNotBlank(protocolName)) {
      protocols = protocols
          .stream().filter(item -> StringUtils
              .containsIgnoreCase(MapUtils.getString(item, "nameText"), protocolName))
          .collect(Collectors.toList());
    }

    // 过滤标签id
    if (StringUtils.isNotBlank(labelId)) {
      protocols = protocols.stream().filter(
          item -> CsvUtils.convertCSVToList(MapUtils.getString(item, "label")).contains(labelId))
          .collect(Collectors.toList());
    }

    return protocols.stream().map(protocol -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.putAll(protocol);
      List<String> label = CsvUtils.convertCSVToList(MapUtils.getString(protocol, "label")).stream()
          .map(id -> labelDict.get(id)).collect(Collectors.toList());
      map.put("label", label);

      return map;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaProtocolService#queryProtocols()
   */
  @Override
  public List<Map<String, String>> queryProtocols() {
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return saProtocolDao.queryProtocols(knowledgeFilePath);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaProtocolService#queryLabels()
   */
  @Override
  public List<Map<String, String>> queryLabels() {
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return saProtocolDao.queryProtocolLabels(knowledgeFilePath);
  }

}
