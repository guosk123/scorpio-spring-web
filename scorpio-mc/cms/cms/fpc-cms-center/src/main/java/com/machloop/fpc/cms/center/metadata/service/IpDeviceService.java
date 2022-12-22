package com.machloop.fpc.cms.center.metadata.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
public interface IpDeviceService {

  Page<Map<String, Object>> queryIpDeviceList(LogRecordQueryVO queryVO, PageRequest page);

  List<Map<String, Object>> queryRtpNetworkSegmentationHistograms(LogRecordQueryVO queryVO);
}
