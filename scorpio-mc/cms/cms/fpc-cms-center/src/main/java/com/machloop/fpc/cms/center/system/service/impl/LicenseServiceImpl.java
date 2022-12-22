package com.machloop.fpc.cms.center.system.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.system.dao.LicenseDao;
import com.machloop.alpha.webapp.system.data.LicenseDO;
import com.machloop.fpc.cms.center.global.library.LicenseLibrary;
import com.machloop.fpc.cms.center.system.bo.LicenseBO;
import com.machloop.fpc.cms.center.system.bo.LicenseItemBO;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.center.system.service.SystemMetricService;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * @author guosk
 *
 * create at 2021年11月4日, fpc-cms-center
 */
@Service
public class LicenseServiceImpl implements LicenseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseServiceImpl.class);

  private static String serialNumber = "";

  @Autowired
  private LicenseDao licenseDao;

  @Autowired
  private SystemMetricService systemMetricService;

  /**
   * @see com.machloop.fpc.cms.center.system.service.LicenseService#queryLatestLicense()
   */
  @Override
  public LicenseBO queryLatestLicense() {
    LicenseBO licenseBO = new LicenseBO();

    // 系统默认部署在linux
    if (!SystemUtils.IS_OS_LINUX) {
      return licenseBO;
    }

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

    licenseBO.setLocalSerialNo(serialNumber);

    // 构建license对象
    LicenseItemBO licenseItem = new LicenseItemBO();
    licenseItem.setSerialNo(serialNumber);
    licenseBO.setLicenseItemList(Lists.newArrayList(licenseItem));

    LicenseDO licenseDO = licenseDao.queryLatestLicense();
    // 将license内容解密
    String content = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), licenseDO.getContent());
    if (StringUtils.isBlank(content)) {
      licenseBO.setLicenseItemList(Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      return licenseBO;
    }

    // 解析license文件内容
    Properties properties = new Properties();
    try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
      properties.load(stream);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "License文件内容格式错误");
    }

    // 获取license文件内容
    String collectTime = properties.getProperty("collectTime");
    String signTime = properties.getProperty("signTime");
    String licenseType = properties.getProperty("licenseType");
    String expiryTime = properties.getProperty("expiryTime");

    // 构造返回对象
    licenseBO.setCollectTime(collectTime);
    licenseBO.setSignTime(signTime);
    licenseBO.setLicenseType(licenseType);
    licenseBO.setExpiryTime(expiryTime);
    licenseBO.setVersion(licenseDO.getVersion());
    licenseBO.setFileName(licenseDO.getFileName());

    return licenseBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.system.service.LicenseService#queryDeviceSerialNumber()
   */
  @Override
  public String queryDeviceSerialNumber() {
    if (StringUtils.isBlank(serialNumber)) {
      /*serialNo = StringUtils.defaultIfBlank(queryLatestLicense().getLocalSerialNo(),
          MonitorSystemHelper.fetchMainboardSerialNumber());*/

      // TODO license暂未支持，设备序列号采用随机生成的设备ID
      serialNumber = MapUtils.getString(systemMetricService.queryDeviceCustomInfo(),
          WebappConstants.GLOBAL_SETTING_DEVICE_ID);
    }

    return serialNumber;
  }

  /**
   * @see com.machloop.fpc.cms.center.system.service.LicenseService#importLicense(java.nio.file.Path, java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public LicenseBO importLicense(Path licenseTmpPath, String fileName, String operatorId) {

    // 验证license签名
    LicenseLibrary license = LicenseLibrary.INSTANCE;
    int resultCode = license.license_verify(licenseTmpPath.toFile().getAbsolutePath());

    // 获取上传license文件的SHA512
    String licenseFileSha512 = null;
    try {
      byte[] fileBytes = Files.readAllBytes(licenseTmpPath);
      licenseFileSha512 = DigestUtils.sha512Hex(fileBytes);
    } catch (IOException e) {
      LOGGER.warn("failed to license file generate SHA512.", e);
    }
    LOGGER.info("check license signature result is {}, license file SHA512 is {}.", resultCode,
        licenseFileSha512);

    // 验签失败删除该文件
    if (resultCode != Integer.parseInt(Constants.RES_OK)) {
      FileUtils.deleteQuietly(licenseTmpPath.toFile());
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "License文件校验未通过");
    }

    // 获取license文件内容
    String content = "";
    try {
      content = new String(Files.readAllBytes(licenseTmpPath), StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOGGER.warn("failed to read btyes from license", e);
      FileUtils.deleteQuietly(licenseTmpPath.toFile());
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "License文件保存失败");
    }

    // 构造license
    LicenseDO licenseDO = new LicenseDO();

    // 将文件内容加密
    licenseDO.setContent(KeyEncUtils
        .encrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), content));
    licenseDO.setFileName(fileName);

    // 将数据库license版本号加1
    synchronized (this) {
      LicenseBO licenseBO = queryLatestLicense();
      licenseDO.setVersion(licenseBO.getVersion() + 1);

      // 存入数据库
      licenseDao.saveLicense(licenseDO, operatorId);
    }

    File licenseFile = Paths.get(HotPropertiesHelper.getProperty("file.license.path")).toFile();
    try {
      com.google.common.io.Files.move(licenseTmpPath.toFile(), licenseFile);
    } catch (IOException e) {
      LOGGER.warn("fail to move license tmp file from: [{}] to: [{}]", licenseTmpPath.toString(),
          licenseFile.getAbsolutePath());
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "上传License失败");
    }
    FileUtils.deleteQuietly(licenseTmpPath.toFile());

    return queryLatestLicense();
  }
}
