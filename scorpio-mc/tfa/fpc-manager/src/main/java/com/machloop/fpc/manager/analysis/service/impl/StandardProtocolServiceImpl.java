package com.machloop.fpc.manager.analysis.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.bo.StandardProtocolBO;
import com.machloop.fpc.manager.analysis.dao.StandardProtocolDao;
import com.machloop.fpc.manager.analysis.data.StandardProtocolDO;
import com.machloop.fpc.manager.analysis.service.StandardProtocolService;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
@Service
public class StandardProtocolServiceImpl implements StandardProtocolService {

  @Autowired
  private DictManager dictManager;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private StandardProtocolDao standardProtocolDao;

  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#queryStandardProtocols(com.machloop.alpha.common.base.page.PageRequest, com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO)
   */
  @Override
  public Page<StandardProtocolBO> queryStandardProtocols(Pageable page,
      StandardProtocolQueryVO queryVO) {

    Map<String, String> sourceDict = dictManager.getBaseDict()
        .getItemMap("analysis_standard_protocol_source");

    Page<StandardProtocolDO> standardProtocolDOPage = standardProtocolDao
        .queryStandardProtocols(page, queryVO);
    long totalElem = standardProtocolDOPage.getTotalElements();

    List<StandardProtocolBO> standardProtocolBOList = Lists
        .newArrayListWithCapacity(standardProtocolDOPage.getSize());
    for (StandardProtocolDO standardProtocolDO : standardProtocolDOPage) {
      StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
      BeanUtils.copyProperties(standardProtocolDO, standardProtocolBO);

      standardProtocolBO
          .setSourceText(MapUtils.getString(sourceDict, standardProtocolDO.getSource(), ""));
      standardProtocolBO
          .setCreateTime(DateUtils.toStringISO8601(standardProtocolDO.getCreateTime()));
      standardProtocolBO
          .setUpdateTime(DateUtils.toStringISO8601(standardProtocolDO.getUpdateTime()));

      standardProtocolBOList.add(standardProtocolBO);
    }

    return new PageImpl<>(standardProtocolBOList, page, totalElem);
  }


  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#queryStandardProtocols(com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO)
   */
  @Override
  public List<StandardProtocolBO> queryStandardProtocols(StandardProtocolQueryVO queryVO) {

    Map<String, String> sourceDict = dictManager.getBaseDict()
        .getItemMap("analysis_standard_protocol_source");

    List<StandardProtocolDO> standardProtocolDOList = standardProtocolDao
        .queryStandardProtocols(queryVO);

    List<StandardProtocolBO> standardProtocolBOList = Lists
        .newArrayListWithCapacity(standardProtocolDOList.size());
    for (StandardProtocolDO standardProtocolDO : standardProtocolDOList) {
      StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
      BeanUtils.copyProperties(standardProtocolDO, standardProtocolBO);

      standardProtocolBO
          .setSourceText(MapUtils.getString(sourceDict, standardProtocolDO.getSource(), ""));
      standardProtocolBO
          .setCreateTime(DateUtils.toStringISO8601(standardProtocolDO.getCreateTime()));
      standardProtocolBO
          .setUpdateTime(DateUtils.toStringISO8601(standardProtocolDO.getUpdateTime()));

      standardProtocolBOList.add(standardProtocolBO);
    }

    return standardProtocolBOList;
  }
  
  @Override
  public List<StandardProtocolBO> queryStandardProtocols() {

    Map<String, String> sourceDict = dictManager.getBaseDict()
        .getItemMap("analysis_standard_protocol_source");

    List<StandardProtocolDO> standardProtocolDOList = standardProtocolDao
        .queryStandardProtocols();

    List<StandardProtocolBO> standardProtocolBOList = Lists
        .newArrayListWithCapacity(standardProtocolDOList.size());
    for (StandardProtocolDO standardProtocolDO : standardProtocolDOList) {
      StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
      BeanUtils.copyProperties(standardProtocolDO, standardProtocolBO);

      standardProtocolBO
          .setSourceText(MapUtils.getString(sourceDict, standardProtocolDO.getSource(), ""));
      standardProtocolBO
          .setCreateTime(DateUtils.toStringISO8601(standardProtocolDO.getCreateTime()));
      standardProtocolBO
          .setUpdateTime(DateUtils.toStringISO8601(standardProtocolDO.getUpdateTime()));

      standardProtocolBOList.add(standardProtocolBO);
    }

    return standardProtocolBOList;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#queryStandardProtocol(java.lang.String)
   */
  @Override
  public StandardProtocolBO queryStandardProtocol(String id) {
    StandardProtocolDO standardProtocolDO = standardProtocolDao.queryStandardProtocol(id);
    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    BeanUtils.copyProperties(standardProtocolDO, standardProtocolBO);

    standardProtocolBO.setCreateTime(DateUtils.toStringISO8601(standardProtocolDO.getCreateTime()));
    standardProtocolBO.setUpdateTime(DateUtils.toStringISO8601(standardProtocolDO.getUpdateTime()));
    return standardProtocolBO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#saveStandardProtocol(com.machloop.fpc.manager.analysis.bo.StandardProtocolBO, java.lang.String)
   */
  @Override
  @Transactional
  public StandardProtocolBO saveStandardProtocol(StandardProtocolBO standardProtocolBO,
      String operatorId) {
    // 判断是否重复
    StandardProtocolDO exist = standardProtocolDao
        .queryStandardProtocol(standardProtocolBO.getL7ProtocolId(), standardProtocolBO.getPort());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "协议端口已存在，请勿重复添加");
    }

    // 写入数据库
    StandardProtocolDO standardProtocolDO = new StandardProtocolDO();
    BeanUtils.copyProperties(standardProtocolBO, standardProtocolDO);
    standardProtocolDO.setOperatorId(operatorId);
    standardProtocolDO = standardProtocolDao.saveStandardProtocol(standardProtocolDO);
    freshVersion();

    return queryStandardProtocol(standardProtocolDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#updateStandardProtocol(java.lang.String, com.machloop.fpc.manager.analysis.bo.StandardProtocolBO, java.lang.String)
   */
  @Override
  @Transactional
  public StandardProtocolBO updateStandardProtocol(String id, StandardProtocolBO standardProtocolBO,
      String operatorId) {
    StandardProtocolDO existStandardProtocol = standardProtocolDao.queryStandardProtocol(id);
    if (StringUtils.isBlank(existStandardProtocol.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }
    if (!StringUtils.equals(existStandardProtocol.getSource(),
        FpcConstants.ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "预置配置不支持修改");
    }
    // 判断是否重复
    StandardProtocolDO exist = standardProtocolDao
        .queryStandardProtocol(standardProtocolBO.getL7ProtocolId(), standardProtocolBO.getPort());
    if (StringUtils.isNotBlank(exist.getId()) && !StringUtils.equals(id, exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "协议端口已存在");
    }

    StandardProtocolDO standardProtocolDO = new StandardProtocolDO();
    BeanUtils.copyProperties(standardProtocolBO, standardProtocolDO);
    standardProtocolDO.setId(id);
    standardProtocolDO.setOperatorId(operatorId);

    standardProtocolDao.updateStandardProtocol(standardProtocolDO);
    freshVersion();

    return queryStandardProtocol(id);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.StandardProtocolService#deleteStandardProtocol(java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public StandardProtocolBO deleteStandardProtocol(String id, String operatorId) {
    StandardProtocolBO standardProtocolBO = queryStandardProtocol(id);

    if (!StringUtils.equals(standardProtocolBO.getSource(),
        FpcConstants.ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "预置配置不支持修改");
    }

    if (StringUtils.isNotBlank(standardProtocolBO.getId())) {
      standardProtocolDao.deleteStandardProtocol(id, operatorId);
      freshVersion();
    }

    return standardProtocolBO;
  }

  private void freshVersion() {
    globalSettingService.setValue(
        ManagerConstants.GLOBAL_SETTING_ANALYSIS_STANDARD_PROTOCOL_VERSION,
        IdGenerator.generateUUID());
  }
}
