package com.machloop.fpc.cms.center.central.dao;

import java.util.List;

import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;

public interface CmsDao {

  List<CmsDO> queryCms(CmsQueryVO queryVO);

  List<CmsDO> queryCmsBySerialNumbers(List<String> serialNumbers);

  List<CmsDO> queryCmsBySuperior(String superiorCmsSerialNumber);

  List<CmsDO> queryCmsByReportState(String reportState);

  CmsDO queryCmsById(String id);

  CmsDO queryCmsByIpOrName(String ip, String name);

  CmsDO queryCmsBySerialNumber(String serialNumber);

  CmsDO saveCms(CmsDO cmsDO);

  int batchSaveCms(List<CmsDO> cmsList);

  int updateCmsStatus(CmsDO cmsDO);

  int updateCmsReportState(List<String> serialNumbers, String reportState);

  int deleteCms(String id, String operatorId);

  int deleteCmsBySerialNumbers(List<String> serialNumbers, String operatorId);

}
