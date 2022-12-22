package com.machloop.fpc.cms.center.metadata.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
public interface ProtocolRtpLogDao {

  List<Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO);
}
