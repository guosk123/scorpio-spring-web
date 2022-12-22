package com.machloop.fpc.manager.system.service;

import java.nio.file.Path;

import com.machloop.fpc.manager.system.bo.LicenseBO;

/**
 * @author liyongjun
 *
 * create at 2019年8月21日, iosp-center
 */
public interface LicenseService {

  LicenseBO queryLatestLicense();

  LicenseBO queryCacheLicense();

  String queryDeviceSerialNumber();

  LicenseBO importLicense(Path licenseTmpPath, String fileName, String operatorId);
}
