package com.machloop.fpc.cms.center.knowledge.dao;

import com.machloop.fpc.cms.center.knowledge.data.SaCustomSubCategoryDO;

import java.util.Date;
import java.util.List;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public interface SaCustomSubCategoryDao {

  List<SaCustomSubCategoryDO> querySaCustomSubCategorys();

  List<String> querySaCustomSubCategoryIds();

  List<SaCustomSubCategoryDO> querySubCategoryByCategoryId(String categoryId);

  List<SaCustomSubCategoryDO> querySubCategoryBySubCategoryIds(List<String> subCategoryIds);

  SaCustomSubCategoryDO querySaCustomSubCategory(String id);

  SaCustomSubCategoryDO querySaCustomSubCategoryByName(String name);

  SaCustomSubCategoryDO querySaCustomSubCategoryBySubCategoryId(String subCategoryId);

  SaCustomSubCategoryDO queryCustomSubCategoryByAssignId(String assignId);

  List<SaCustomSubCategoryDO> queryAssignSaCustomSubCategoryIds(Date beforeTime);

  int countSaCustomSubCategorys();

  SaCustomSubCategoryDO saveSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO);

  void batchSaveCustomSubCategory(List<SaCustomSubCategoryDO> subCategoryDOs);

  int batchUpdateCategoryId(List<String> subCategoryIds, String categoryId, String operatorId);

  int updateSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO);

  int deleteSaCustomSubCategory(String id, String operatorId);

  int batchDeleteSubCategory(List<String> subCategoryIds);

}
