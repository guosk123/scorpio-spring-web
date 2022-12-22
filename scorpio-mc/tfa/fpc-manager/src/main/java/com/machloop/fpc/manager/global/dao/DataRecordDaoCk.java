package com.machloop.fpc.manager.global.dao;

import java.io.IOException;
import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年6月11日, fpc-manager
 */
public interface DataRecordDaoCk {

  int rollup(Date startTime, Date endTime) throws IOException;

}
