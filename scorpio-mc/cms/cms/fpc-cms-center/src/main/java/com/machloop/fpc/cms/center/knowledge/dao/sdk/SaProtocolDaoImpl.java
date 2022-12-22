package com.machloop.fpc.cms.center.knowledge.dao.sdk;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.machloop.fpc.cms.center.helper.SaParseLibraryHelper;
import com.machloop.fpc.cms.center.knowledge.dao.SaProtocolDao;

/**
 * @author guosk
 *
 * create at 2020年12月5日, fpc-manager
 */
@Repository
public class SaProtocolDaoImpl implements SaProtocolDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaKnowledgeDaoImpl.class);

  private List<Map<String, String>> protocolsCache;
  private List<Map<String, String>> protocolLabelsCache;

  private long lastLoadTime;

  @Autowired
  private SaParseLibraryHelper saParseLibraryHelper;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaProtocolDao#queryProtocols(java.lang.String)
   */
  @Override
  public List<Map<String, String>> queryProtocols(String filePath) {
    // 检查当前文件时间与上次加载文件时间是否一致, 不一致则更新规则库协议cache
    long currentLoadTime = Paths.get(filePath).toFile().lastModified();
    if (protocolsCache == null || this.lastLoadTime != currentLoadTime) {
      LOGGER.debug("reload knowledge file.");
      this.protocolsCache = saParseLibraryHelper.parseProtocols(filePath);
      this.lastLoadTime = currentLoadTime;
    }
    return protocolsCache;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaProtocolDao#queryProtocolLabels(java.lang.String)
   */
  @Override
  public List<Map<String, String>> queryProtocolLabels(String filePath) {
    // 检查当前文件时间与上次加载文件时间是否一致, 不一致则更新规则库标签cache
    long currentLoadTime = Paths.get(filePath).toFile().lastModified();
    if (protocolLabelsCache == null || this.lastLoadTime != currentLoadTime) {
      LOGGER.debug("reload knowledge file.");
      this.protocolLabelsCache = saParseLibraryHelper.parseProtocolLabels(filePath);
      this.lastLoadTime = currentLoadTime;
    }
    return protocolLabelsCache;
  }

}
