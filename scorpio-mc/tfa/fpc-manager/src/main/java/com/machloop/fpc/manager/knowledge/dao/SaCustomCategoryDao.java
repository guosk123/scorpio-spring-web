package com.machloop.fpc.manager.knowledge.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.knowledge.data.SaCustomCategoryDO;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public interface SaCustomCategoryDao {

  List<SaCustomCategoryDO> querySaCustomCategorys();

  List<String> queryAssignSaCustomCategorys(Date beforeTime);

  List<SaCustomCategoryDO> querySaCustomCategoryIdAndNumIds(boolean onlyLocal);

  SaCustomCategoryDO querySaCustomCategory(String id);

  SaCustomCategoryDO querySaCustomCategoryByName(String name);

  SaCustomCategoryDO querySaCustomCategoryByCategoryId(String categoryId);

  SaCustomCategoryDO queryCustomCategoryByCmsCategoryId(String cmsCategoryId);

  int countSaCustomCategorys();

  SaCustomCategoryDO saveOrRecoverSaCustomCategory(SaCustomCategoryDO categoryDO);

  int updateSaCustomCategory(SaCustomCategoryDO categoryDO);

  int deleteSaCustomCategory(String id, String operatorId);

}
