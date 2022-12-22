package com.machloop.fpc.manager.metadata.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/25
 */
public interface ProtocolRtpLogDao {
  List<Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO);
}
