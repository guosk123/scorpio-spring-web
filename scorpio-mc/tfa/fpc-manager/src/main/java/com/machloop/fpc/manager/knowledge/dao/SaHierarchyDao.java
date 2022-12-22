package com.machloop.fpc.manager.knowledge.dao;

import java.util.List;

import com.machloop.fpc.manager.knowledge.data.SaHierarchyDO;

/**
 * @author guosk
 *
 * create at 2021年1月29日, fpc-manager
 */
public interface SaHierarchyDao {

  List<SaHierarchyDO> querySaHierarchys(String type, String categoryId, String subCategoryId);

  List<SaHierarchyDO> querySaHierarchyBySubCategoryIds(List<String> subCategoryIds);

  List<SaHierarchyDO> querySaHierarchyByApplicationIds(List<String> applicationIds);

  void batchSaveSaHierarchy(List<SaHierarchyDO> saHierarchys);

  /**
   * 当子分类修改所属分类时，将属于该子分类的应用修改所属分类
   * @param subCategoryIds
   * @param categoryId
   * @return
   */
  int batchUpdateAppsBySubCategory(List<String> subCategoryIds, String categoryId,
      String operatorId);

  int batchDeleteByCategoryId(String categoryId);

  int batchDeleteBySubCategoryIds(String type, List<String> subCategoryIds);

  int batchDeleteByApplicationIds(String type, List<String> applicationIds);

}
