package com.machloop.fpc.cms.center.central.service;

import java.util.List;

import com.machloop.fpc.cms.center.central.bo.CmsBO;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;

public interface CmsService {

  List<CmsBO> queryCms(CmsQueryVO query);

  List<CmsBO> queryCmsBySerialNumbers(List<String> serialNumbers, boolean isSimple);

  List<CmsBO> queryCmsBySuperior(String superiorCmsSerialNumber);

  int queryMaxCmsHierarchy(String serialNumber);

  CmsBO queryCmsById(String id);

  CmsBO queryCmsByIp(String ip);

  CmsBO queryCmsBySerialNumber(String serialNumber);

  String queryCmsLoginUrl(String serialNumber);

  CmsBO saveCms(CmsBO cmsBO);

  void updateCmsStatus(CmsBO cmsBO);

  CmsBO deleteCms(String id, String operatorId);

  void deleteCmsBySerialNumbers(List<String> serialNumbers, String operatorId);

  String exportCmsMessage();

  String exportCmsSerialNumber();

}
