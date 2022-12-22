package com.machloop.fpc.cms.center.central.service;

import java.util.List;

import com.machloop.fpc.cms.center.central.bo.CentralDeviceBO;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;

public interface FpcService {

  List<FpcBO> queryFpcs(FpcQueryVO query);
  
  List<FpcBO> queryAllFpc();

  List<FpcBO> queryFpcBySerialNumbers(List<String> serialNumbers, boolean isSimple);

  List<FpcBO> queryFpcByCms(String cmsSerialNumber, boolean drilldown);

  FpcBO queryFpcById(String id);

  FpcBO queryFpcByIp(String ip);

  FpcBO queryFpcBySerialNumber(String serialNumber);

  CentralDeviceBO queryCentralDevices();

  String queryFpcLoginUrl(String serialNumber);

  FpcBO saveFpc(FpcBO fpcBO);

  void updateFpcStatus(FpcBO fpcBO);

  FpcBO deleteFpc(String id, String operatorId);

  String exportFpcMessage();

  String exportFpcSerialNumber();

}
