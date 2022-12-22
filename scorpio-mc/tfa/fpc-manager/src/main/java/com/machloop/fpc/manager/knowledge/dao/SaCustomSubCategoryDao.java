package com.machloop.fpc.manager.knowledge.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.knowledge.data.SaCustomSubCategoryDO;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public interface SaCustomSubCategoryDao {

  List<SaCustomSubCategoryDO> querySaCustomSubCategorys();

  List<String> queryAssignSaCustomSubCategorys(Date beforeTime);

  List<SaCustomSubCategoryDO> querySaCustomSubCategoryIdAndNumIds(boolean onlyLocal);

  List<SaCustomSubCategoryDO> querySubCategoryByCategoryId(String categoryId);

  List<SaCustomSubCategoryDO> querySubCategoryBySubCategoryIds(List<String> subCategoryIds);

  SaCustomSubCategoryDO querySaCustomSubCategory(String id);

  SaCustomSubCategoryDO querySaCustomSubCategoryByName(String name);

  SaCustomSubCategoryDO querySaCustomSubCategoryBySubCategoryId(String subCategoryId);

  SaCustomSubCategoryDO querySubCategoryByCmsSubCategoryId(String cmsSubCategoryId);

  int countSaCustomSubCategorys();

  SaCustomSubCategoryDO saveOrRecoverSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO);

  void batchSaveCustomSubCategory(List<SaCustomSubCategoryDO> subCategoryDOs);

  int batchUpdateCategoryId(List<String> subCategoryIds, String categoryId, String operatorId);

  int updateSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO);

  int deleteSaCustomSubCategory(String id, String operatorId);

  int deleteSaCustomSubCategoryByCategoryId(String categoryId, String operatorId);

  int batchDeleteSubCategory(List<String> subCategoryIds);

}
