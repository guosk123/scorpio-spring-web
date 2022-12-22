package com.machloop.fpc.manager.system.service;

import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 *
 * create at 2022/9/15 10:26 AM, fpc-manager
 * @version 1.0
 */
public interface DataClearCategoryService {

  Map<String, String> queryDataClearCategory();

  void clearData(List<String> param, String id);
}
