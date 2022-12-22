package com.machloop.fpc.cms.center.knowledge.service;

import com.machloop.fpc.cms.center.knowledge.bo.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.function.Tuple3;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月21日, fpc-manager
 */
public interface SaService {

  SaKnowledgeInfoBO queryKnowledgeInfos();

  Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>, List<SaApplicationBO>> queryKnowledgeRules();

  SaKnowledgeInfoBO importKnowledges(MultipartFile fileMultifile);

  SaKnowledgeInfoBO importKnowledges(FileInputStream inputStream);

  /**
   * 导入导出自定义配置
   */
  List<String> exportCustomRules();

  int importCustomRules(MultipartFile file, String operatorId);

  /*
   * 自定义分类
   */
  List<SaCustomCategoryBO> queryCustomCategorys();

  SaCustomCategoryBO queryCustomCategory(String id);

  SaCustomCategoryBO saveCustomCategory(SaCustomCategoryBO customCategoryBO, String operatorId);

  SaCustomCategoryBO updateCustomCategory(String id, SaCustomCategoryBO customCategoryBO,
      String operatorId);

  SaCustomCategoryBO deleteCustomCategory(String id, String operatorId, boolean forceDelete);

  /*
   * 自定义子分类
   */
  List<SaCustomSubCategoryBO> queryCustomSubCategorys();

  SaCustomSubCategoryBO queryCustomSubCategory(String id);

  SaCustomSubCategoryBO saveCustomSubCategory(SaCustomSubCategoryBO customSubCategoryBO,
      String operatorId);

  SaCustomSubCategoryBO updateCustomSubCategory(String id,
      SaCustomSubCategoryBO customSubCategoryBO, String operatorId);

  SaCustomSubCategoryBO deleteCustomSubCategory(String id, String operatorId, boolean forceDelete);

  /*
   * 自定义应用规则
   */
  List<SaCustomApplicationBO> queryCustomApps();

  SaCustomApplicationBO queryCustomApp(String id);

  SaCustomApplicationBO saveCustomApp(SaCustomApplicationBO customAppBO, String operatorId);

  SaCustomApplicationBO updateCustomApp(String id, SaCustomApplicationBO customAppBO,
      String operatorId);

  SaCustomApplicationBO deleteCustomApp(String id, String operatorId, boolean forceDelete);

  /*
   * 查询所有应用<应用ID,应用名称>
   */
  Map<Integer, String> queryAllAppsIdNameMapping();
}
