package com.machloop.fpc.manager.knowledge.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.knowledge.data.SaCustomApplicationDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月18日, fpc-manager
 */
public interface SaCustomApplicationDao {

  List<SaCustomApplicationDO> querySaCustomApps();

  List<String> queryAssignSaCustomApps(Date beforeTime);

  List<SaCustomApplicationDO> querySaCustomAppIdAndNumIds(boolean onlyLocal);

  List<SaCustomApplicationDO> querySaCustomAppBySubCategoryId(String subCategoryId);

  List<SaCustomApplicationDO> querySaCustomAppByAppIds(List<String> appIds);

  SaCustomApplicationDO querySaCustomApp(String id);

  SaCustomApplicationDO querySaCustomAppByName(String name);

  SaCustomApplicationDO queryCustomAppByCmsApplicationId(String cmsApplicationId);

  int countSaCustomApps();

  SaCustomApplicationDO saveOrRecoverSaCustomApp(SaCustomApplicationDO applicationDO);

  void batchSaveCustomApp(List<SaCustomApplicationDO> applicationDOs);

  int batchUpdateApps(List<String> appIds, String categoryId, String subCategoryId,
      String operatorId);

  /**
   * 当子分类修改所属分类时，将属于该子分类的应用修改所属分类
   * @param subCategoryIds
   * @param categoryId
   * @param operatorId
   * @return
   */
  int batchUpdateAppsBySubCategory(List<String> subCategoryIds, String categoryId,
      String operatorId);

  int updateSaCustomApp(SaCustomApplicationDO applicationDO);

  int deleteSaCustomApp(String id, String operatorId);

  int deleteSaCustomAppByCategoryId(String categoryId, String operatorId);

  int deleteSaCustomAppBySubCategoryId(String subCategoryId, String operatorId);

  int batchDeleteCustomApp(List<String> appIds);

}
