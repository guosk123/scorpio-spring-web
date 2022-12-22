package com.machloop.fpc.manager.global.dao;

import java.io.IOException;
import java.util.Date;

/**
 * @author liumeng
 *
 * create at 2019年10月10日, fpc-manager
 */
public interface DataRecordDao {

  int rollup(Date startTime, Date endTime) throws IOException;

  int addAlias();
}
