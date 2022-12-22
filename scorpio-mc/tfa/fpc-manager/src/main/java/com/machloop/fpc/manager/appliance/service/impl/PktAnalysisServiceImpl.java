package com.machloop.fpc.manager.appliance.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.PktAnalysisBO;
import com.machloop.fpc.manager.appliance.dao.PktAnalysisDao;
import com.machloop.fpc.manager.appliance.data.PktAnalysisDO;
import com.machloop.fpc.manager.appliance.service.PktAnalysisService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
@Service
public class PktAnalysisServiceImpl implements PktAnalysisService {

  @Autowired
  private PktAnalysisDao pktAnalysisDao;

  private static final int MAX_FILENAME_LENGTH = 64;

  private static final int MAX_PROTOCOL_LENGTH = 64;

  private static final int MAX_DESCRIPTION_LENGTH = 512;

  @Value("${file.pktAnalysis.plugins.path}")
  private String pktAnalysisPluginsPath;

  /**
   * @see com.machloop.fpc.manager.appliance.service.PktAnalysisService#queryPktAnalysises(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PktAnalysisBO> queryPktAnalysises(Pageable page) {

    Page<PktAnalysisDO> pktAnalysisDOPage = pktAnalysisDao.queryPktAnalysises(page);
    long totalElem = pktAnalysisDOPage.getTotalElements();

    List<PktAnalysisBO> pktAnalysisBOList = Lists
        .newArrayListWithCapacity(pktAnalysisDOPage.getSize());
    for (PktAnalysisDO pktAnalysisDO : pktAnalysisDOPage) {
      PktAnalysisBO pktAnalysisBO = new PktAnalysisBO();
      BeanUtils.copyProperties(pktAnalysisDO, pktAnalysisBO);

      pktAnalysisBO.setCreateTime(DateUtils.toStringISO8601(pktAnalysisDO.getCreateTime()));
      pktAnalysisBO.setUpdateTime(DateUtils.toStringISO8601(pktAnalysisDO.getUpdateTime()));

      pktAnalysisBOList.add(pktAnalysisBO);
    }

    return new PageImpl<>(pktAnalysisBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.PktAnalysisService#queryPktAnalysis(java.lang.String)
   */
  @Override
  public PktAnalysisBO queryPktAnalysis(String id) {
    PktAnalysisBO pktAnalysisBO = new PktAnalysisBO();

    PktAnalysisDO pktAnalysisDO = pktAnalysisDao.queryPktAnalysis(id);
    BeanUtils.copyProperties(pktAnalysisDO, pktAnalysisBO);

    pktAnalysisBO.setCreateTime(DateUtils.toStringISO8601(pktAnalysisDO.getCreateTime()));
    pktAnalysisBO.setUpdateTime(DateUtils.toStringISO8601(pktAnalysisDO.getUpdateTime()));

    return pktAnalysisBO;
  }

  /**
   * @throws IOException 
   * @see com.machloop.fpc.manager.appliance.service.PktAnalysisService#getPktAnalysisFile(java.lang.String)
   */
  @Override
  public String getPktAnalysisFile(String id) {
    PktAnalysisBO pktAnalysisBO = queryPktAnalysis(id);
    if (StringUtils.isBlank(pktAnalysisBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "在线分析脚本不存在。");
    }

    StringBuilder fileUrl = new StringBuilder();
    fileUrl.append(pktAnalysisPluginsPath);
    fileUrl.append(pktAnalysisBO.getFileName());

    String scriptContent;
    try {
      scriptContent = FileUtils.readFileToString(new File(fileUrl.toString()),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "在线分析脚本读取失败");
    }
    return scriptContent;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.PktAnalysisService#savePktAnalysis(org.springframework.web.multipart.MultipartFile, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int savePktAnalysis(MultipartFile file, String fileName, String protocol,
      String description, String operatorId) {

    if (fileName.length() > MAX_FILENAME_LENGTH || protocol.length() > MAX_PROTOCOL_LENGTH) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "协议或文件名称长度超过最大限制！");
    }

    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "描述长度超过最大限制！");
    }

    if (!file.getOriginalFilename().endsWith(".lua")) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "导入文件格式错误，请导入lua格式的文件！");
    }

    List<PktAnalysisDO> pktAnalysisList = pktAnalysisDao.queryPktAnalysises();
    for (PktAnalysisDO pktAnalysisDO : pktAnalysisList) {
      if (StringUtils.equals(pktAnalysisDO.getFileName(), fileName)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "文件名称已经存在！");
      }
    }

    InputStream inputStream = null;
    try {
      inputStream = file.getInputStream();
      byte[] buffer = IOUtils.toByteArray(inputStream);
      FileUtils.writeByteArrayToFile(new File(pktAnalysisPluginsPath + fileName), buffer);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "文件上传失败");
    }

    PktAnalysisDO pktAnalysisDO = new PktAnalysisDO();
    pktAnalysisDO.setFileName(fileName);
    pktAnalysisDO.setProtocol(protocol);
    pktAnalysisDO.setDescription(description);
    pktAnalysisDO.setOperatorId(operatorId);
    int result = pktAnalysisDao.savePktAnalysis(pktAnalysisDO);
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.PktAnalysisService#deletePktAnalysis(java.lang.String, java.lang.String)
   */
  @Override
  public PktAnalysisBO deletePktAnalysis(String id, String operatorId) {
    PktAnalysisDO exist = pktAnalysisDao.queryPktAnalysis(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "在线分析脚本不存在");
    }
    
    StringBuilder fileUrl = new StringBuilder();
    fileUrl.append(pktAnalysisPluginsPath);
    fileUrl.append(exist.getFileName());
    File file = new File(fileUrl.toString());
    if(!file.delete()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "在线分析脚本删除失败");
    }

    pktAnalysisDao.deletePktAnalysis(id, operatorId);

    PktAnalysisBO pktAnalysisBO = new PktAnalysisBO();
    BeanUtils.copyProperties(exist, pktAnalysisBO);

    return pktAnalysisBO;
  }

}
