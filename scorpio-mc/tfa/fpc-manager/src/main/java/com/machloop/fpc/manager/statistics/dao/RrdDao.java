package com.machloop.fpc.manager.statistics.dao;

import com.machloop.fpc.manager.statistics.data.RrdDO;

public interface RrdDao {

  RrdDO queryRrdByName(String name);

  RrdDO saveRrd(RrdDO rrdDO);

  int updateRrd(RrdDO rrdDO);
}
