package com.machloop.fpc.manager.knowledge.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.global.library.CertVerifyLibrary;
import com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO;
import com.machloop.fpc.manager.knowledge.dao.DecryptDao;
import com.machloop.fpc.manager.knowledge.data.DecryptSettingDO;
import com.machloop.fpc.manager.knowledge.service.DecryptService;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
@Service
public class DecryptServiceImpl implements DecryptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DecryptServiceImpl.class);

  @Autowired
  private DecryptDao decryptDao;

  /**
   * @see com.machloop.fpc.manager.knowledge.service.DecryptService#queryDecryptSettings(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<DecryptSettingBO> queryDecryptSettings(String ipAddress, String port,
      String protocol) {

    List<DecryptSettingDO> decryptSettingDOList = decryptDao.queryDecryptSettings(ipAddress, port,
        protocol);
    List<DecryptSettingBO> decryptSettingBOList = Lists
        .newArrayListWithCapacity(decryptSettingDOList.size());
    for (DecryptSettingDO decryptSettingDO : decryptSettingDOList) {
      DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
      BeanUtils.copyProperties(decryptSettingDO, decryptSettingBO);
      decryptSettingBO.setCreateTime(DateUtils.toStringISO8601(decryptSettingDO.getCreateTime()));
      decryptSettingBO.setUpdateTime(DateUtils.toStringISO8601(decryptSettingDO.getUpdateTime()));
      decryptSettingBOList.add(decryptSettingBO);
    }

    return decryptSettingBOList;
  }
  
  @Override
  public List<DecryptSettingBO> queryDecryptSettings() {

    List<DecryptSettingDO> decryptSettingDOList = decryptDao.queryDecryptSettings();
    List<DecryptSettingBO> decryptSettingBOList = Lists
        .newArrayListWithCapacity(decryptSettingDOList.size());
    for (DecryptSettingDO decryptSettingDO : decryptSettingDOList) {
      DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
      BeanUtils.copyProperties(decryptSettingDO, decryptSettingBO);
      decryptSettingBO.setCreateTime(DateUtils.toStringISO8601(decryptSettingDO.getCreateTime()));
      decryptSettingBO.setUpdateTime(DateUtils.toStringISO8601(decryptSettingDO.getUpdateTime()));
      decryptSettingBOList.add(decryptSettingBO);
    }

    return decryptSettingBOList;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.DecryptService#queryDecryptSetting(java.lang.String)
   */
  @Override
  public DecryptSettingBO queryDecryptSetting(String id) {
    DecryptSettingDO decryptSettingDO = decryptDao.queryDecryptSetting(id);
    DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
    BeanUtils.copyProperties(decryptSettingDO, decryptSettingBO);
    decryptSettingBO.setCreateTime(DateUtils.toStringISO8601(decryptSettingDO.getCreateTime()));
    decryptSettingBO.setUpdateTime(DateUtils.toStringISO8601(decryptSettingDO.getUpdateTime()));
    return decryptSettingBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.DecryptService#saveDecryptSetting(com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO, org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Override
  public DecryptSettingBO saveDecryptSetting(DecryptSettingBO decryptSettingBO, Path filePath,
      String operatorId) {

    int ret = CertVerifyLibrary.INSTANCE.check_pri_key_file(filePath.toString());
    LOGGER.info("verify cert, return: [{}]", ret);
    if (ret != 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "证书校验失败");
    }

    String sha256 = null;
    String fileContent = null;
    try {
      byte[] fileBytes = Files.readAllBytes(filePath);
      // 后边标注hash算法，若不标注则默认为md5
      sha256 = DigestUtils.sha256Hex(fileBytes) + "/" + "SHA256";
      fileContent = Base64.encodeBase64String(fileBytes);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "读取文件失败");
    }
    if (StringUtils.isBlank(sha256) || StringUtils.isBlank(fileContent)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "读取文件失败");
    }

    // 写入数据库
    DecryptSettingDO decryptSettingDO = new DecryptSettingDO();
    BeanUtils.copyProperties(decryptSettingBO, decryptSettingDO);
    decryptSettingDO.setCertContent(fileContent);
    decryptSettingDO.setCertHash(sha256);
    decryptSettingDO.setOperatorId(operatorId);
    decryptSettingDO = decryptDao.saveDecryptSetting(decryptSettingDO);

    return queryDecryptSetting(decryptSettingDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.DecryptService#updateDecryptSetting(java.lang.String, com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO, org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Override
  public DecryptSettingBO updateDecryptSetting(String id, DecryptSettingBO decryptSettingBO,
      Path filePath, String operatorId) {

    DecryptSettingDO existDecryptSetting = decryptDao.queryDecryptSetting(id);
    if (StringUtils.isBlank(existDecryptSetting.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }

    String sha256 = null;
    String fileContent = null;

    // 不修改证书文件不校验
    if (filePath != null) {
      int ret = CertVerifyLibrary.INSTANCE.check_pri_key_file(filePath.toString());
      LOGGER.info("verify cert, return: [{}]", ret);
      if (ret != 0) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "证书校验失败");
      }
      try {
        byte[] fileBytes = Files.readAllBytes(filePath);
        // 后边标注hash算法，若不标注则默认为md5
        sha256 = DigestUtils.sha256Hex(fileBytes) + "/" + "SHA256";
        fileContent = Base64.encodeBase64String(fileBytes);
      } catch (IOException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "读取文件失败");
      }
    }

    // 写入数据库
    DecryptSettingDO decryptSettingDO = new DecryptSettingDO();
    BeanUtils.copyProperties(existDecryptSetting, decryptSettingDO);
    decryptSettingDO.setIpAddress(decryptSettingBO.getIpAddress());
    decryptSettingDO.setPort(decryptSettingBO.getPort());
    decryptSettingDO.setProtocol(decryptSettingBO.getProtocol());
    decryptSettingDO.setCertContent(fileContent);
    decryptSettingDO.setCertHash(sha256);
    decryptSettingDO.setOperatorId(operatorId);
    decryptDao.updateDecryptSetting(decryptSettingDO);

    return queryDecryptSetting(id);
  }

  @Override
  public DecryptSettingBO deleteDecryptSetting(String id, String operatorId) {
    DecryptSettingBO decryptSettingBO = queryDecryptSetting(id);
    if (StringUtils.isNotBlank(decryptSettingBO.getId())) {
      decryptDao.deleteDecryptSetting(id, operatorId);
    }
    return decryptSettingBO;
  }
}
