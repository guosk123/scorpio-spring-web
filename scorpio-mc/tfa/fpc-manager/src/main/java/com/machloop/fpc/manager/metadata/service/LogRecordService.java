package com.machloop.fpc.manager.metadata.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.metadata.vo.AbstractLogRecordVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * 
 * @author liumeng
 *
 * create at 2019年10月9日, fpc-manager
 */
public interface LogRecordService<VO extends AbstractLogRecordVO> {

  Page<VO> queryLogRecords(LogRecordQueryVO queryVO, Pageable page);

  List<Map<String, Object>> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds);

  String queryLogRecordsViaDsl(String dsl);

  VO queryLogRecord(LogRecordQueryVO queryVO, String id);

  Map<String, Object> queryLogRecordStatistics(LogRecordQueryVO queryVO);

  void exportLogRecords(LogRecordQueryVO queryVO, Sort sort, String fileType, int count,
      OutputStream out) throws IOException;

  Map<String, Object> fetchLogPacketFileUrls(String queryId, LogRecordQueryVO queryVO, Sort sort);

}
