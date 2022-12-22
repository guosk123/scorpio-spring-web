package com.machloop.fpc.manager.metadata.service;

import java.util.Map;

import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/25
 */
public interface ProtocolRtpLogService {

    Map<String, Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO);
}
