package com.machloop.fpc.cms.center.central.dao;

import java.util.List;

import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;

public interface FpcDao {

  List<FpcDO> queryFpcs(FpcQueryVO query);

  List<FpcDO> queryFpcsBySerialNumbers(List<String> serialNumbers);

  List<FpcDO> queryFpcByCms(List<String> cmsSerialNumbers);

  List<FpcDO> queryFpcByReportState(String reportState);

  FpcDO queryFpcById(String id);

  FpcDO queryFpcByIpOrName(String ip, String name);

  FpcDO queryFpcBySerialNumber(String serialNumber);

  List<FpcDO> queryOnlineFpcs();

  FpcDO saveFpc(FpcDO fpcDO);

  int batchSaveFpcs(List<FpcDO> fpcs);

  int updateFpcStatus(FpcDO fpcDO);

  int updateFpcReportState(List<String> serialNumbers, String reportState);

  int deleteFpc(String id, String operatorId);

  int deleteFpcBySerialNumbers(List<String> serialNumbers, String operatorId);

}
