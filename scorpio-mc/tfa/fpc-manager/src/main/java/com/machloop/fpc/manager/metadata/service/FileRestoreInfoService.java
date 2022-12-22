package com.machloop.fpc.manager.metadata.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.metadata.vo.FileRestoreInfoVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/27
 */
public interface FileRestoreInfoService {
  Page<FileRestoreInfoVO> queryFileRestoreInfos(LogRecordQueryVO queryVO, PageRequest page);

  Map<String, Object> queryFileRestoreInfoStatistics(LogRecordQueryVO queryVO);

  FileRestoreInfoVO queryFileRestoreInfo(LogRecordQueryVO queryVO, String id);

  void exportLogRecords(LogRecordQueryVO queryVO, Sort sort, String fileType, int count,
      OutputStream out) throws IOException;
}
