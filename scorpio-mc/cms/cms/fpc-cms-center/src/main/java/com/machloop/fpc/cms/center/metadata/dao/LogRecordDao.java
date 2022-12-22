package com.machloop.fpc.cms.center.metadata.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.center.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

import reactor.util.function.Tuple2;


/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
public interface LogRecordDao<DO extends AbstractLogRecordDO> {

  Page<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Pageable page);

  List<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Sort sort, int size);

  List<Map<String, Object>> queryLogRecords(String startTime, String endTime, List<String> flowIds,
      int size);

  String queryLogRecordsViaDsl(String dsl);

  List<DO> queryLogRecordByIds(String tableName, String columns, List<String> ids, Sort sort);

  Tuple2<String, List<String>> queryLogRecord(LogRecordQueryVO queryVO, List<String> flowIds,
      Sort sort, int size);


  DO queryLogRecord(LogRecordQueryVO queryVO, String id);

  long countLogRecords(LogRecordQueryVO queryVO, List<String> flowIds);

  Map<String, Long> countLogRecords(LogCountQueryVO queryVO);
}
