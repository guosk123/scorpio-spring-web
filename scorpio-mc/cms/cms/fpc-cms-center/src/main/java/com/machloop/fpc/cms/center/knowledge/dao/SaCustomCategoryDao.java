package com.machloop.fpc.cms.center.knowledge.dao;

import com.machloop.fpc.cms.center.knowledge.data.SaCustomCategoryDO;

import java.util.Date;
import java.util.List;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public interface SaCustomCategoryDao {

  List<SaCustomCategoryDO> querySaCustomCategorys();

  SaCustomCategoryDO querySaCustomCategory(String id);

  List<String> querySaCustomCategoryIds(boolean onlyLocal);

  SaCustomCategoryDO querySaCustomCategoryByName(String name);

  SaCustomCategoryDO querySaCustomCategoryByCategoryId(String categoryId);

  SaCustomCategoryDO queryCustomCategoryByAssignId(String assignId);

  List<SaCustomCategoryDO> queryAssignSaCustomCategoryIds(Date beforeTime);

  int countSaCustomCategorys();

  SaCustomCategoryDO saveSaCustomCategory(SaCustomCategoryDO categoryDO);

  int updateSaCustomCategory(SaCustomCategoryDO categoryDO);

  int deleteSaCustomCategory(String id, String operatorId);

}
