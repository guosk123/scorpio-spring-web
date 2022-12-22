package com.machloop.fpc.cms.center.metadata.service;

import java.util.Map;

import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
public interface ProtocolRtpLogService {

  Map<String, Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO);
}
