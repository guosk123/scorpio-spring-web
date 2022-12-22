package com.machloop.fpc.manager.metadata.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.metadata.data.FileRestoreInfoDO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author ChenXiao
 * create at 2022/10/27
 */
public interface FileRestoreInfoDao {
    Page<FileRestoreInfoDO> queryFileRestoreInfos(LogRecordQueryVO queryVO, List<String> ids,
                                                  PageRequest page);

    long countLogRecords(LogRecordQueryVO queryVO, List<String> flowIds);

    FileRestoreInfoDO queryFileRestoreInfo(LogRecordQueryVO queryVO, String id);

    Tuple2<String, List<String>> queryLogRecords(LogRecordQueryVO queryVO, List<String> analysisResultIds, Sort sort, int count);

    List<FileRestoreInfoDO> queryFileRestoreInfosByIds(String tableName, String dataColumns, List<String> tmpIds, Sort sort);
}
