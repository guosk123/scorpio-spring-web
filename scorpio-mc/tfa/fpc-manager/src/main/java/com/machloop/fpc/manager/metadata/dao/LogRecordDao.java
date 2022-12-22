package com.machloop.fpc.manager.metadata.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
public interface LogRecordDao<DO extends AbstractLogRecordDO> {

  Page<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids, Pageable page);

  /**
   * 导出流日志时，获取startTime_flowId集合
   * @param queryVO
   * @param ids
   * @param sort
   * @param size
   * @return t1：表名；t2：id集合
   */
  Tuple2<String, List<String>> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids,
      Sort sort, int size);

  /**
   * 通过ID集合查询元数据
   * @param tableName
   * @param columns
   * @param ids ID集合（id：startTime_flowId）
   * @param sort
   * @return
   */
  List<DO> queryLogRecordByIds(String tableName, String columns, List<String> ids, Sort sort);

  List<Map<String, Object>> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds,
      int size);

  List<Object> queryFlowIds(String queryId, LogRecordQueryVO queryVO, List<String> ids, Sort sort,
      int size);

  String queryLogRecordsViaDsl(String dsl);

  DO queryLogRecord(LogRecordQueryVO queryVO, String id);

  long countLogRecords(LogRecordQueryVO queryVO, List<String> ids);

  Map<String, Long> countLogRecords(LogCountQueryVO queryVO);
}
