package com.machloop.fpc.cms.center.system.service;

import java.nio.file.Path;

import com.machloop.fpc.cms.center.system.bo.LicenseBO;

/**
 * @author guosk
 *
 * create at 2021年11月4日, fpc-cms-center
 */
public interface LicenseService {

  LicenseBO queryLatestLicense();

  String queryDeviceSerialNumber();

  LicenseBO importLicense(Path licenseTmpPath, String fileName, String operatorId);
}
