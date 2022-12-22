package com.machloop.fpc.manager.metadata.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/21
 */
public interface IpDeviceDao {
    Page<Map<String, Object>> queryIpDeviceList(LogRecordQueryVO queryVO, PageRequest page);

    List<Map<String, Object>> queryRtpNetworkSegmentationHistograms(LogRecordQueryVO queryVO);
}
