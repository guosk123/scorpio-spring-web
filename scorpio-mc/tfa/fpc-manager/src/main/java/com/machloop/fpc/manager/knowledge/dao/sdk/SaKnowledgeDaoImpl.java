package com.machloop.fpc.manager.knowledge.dao.sdk;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.helper.SaParseLibraryHelper;
import com.machloop.fpc.manager.knowledge.dao.SaKnowledgeDao;
import com.machloop.fpc.manager.knowledge.data.SaApplicationDO;
import com.machloop.fpc.manager.knowledge.data.SaCategoryDO;
import com.machloop.fpc.manager.knowledge.data.SaKnowledgeInfoDO;
import com.machloop.fpc.manager.knowledge.data.SaSubCategoryDO;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月21日, fpc-manager
 */
@Repository
public class SaKnowledgeDaoImpl implements SaKnowledgeDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaKnowledgeDaoImpl.class);

  private Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>, List<SaApplicationDO>> rulesCache;

  private long lastLoadTime;

  @Autowired
  private SaParseLibraryHelper saParseLibraryHelper;

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.SaKnowledgeDao#queryKnowledgeInfos(java.lang.String)
   */
  @Override
  public SaKnowledgeInfoDO queryKnowledgeInfos(String filePath) {
    SaKnowledgeInfoDO saKnowledgeInfoDO = new SaKnowledgeInfoDO();
    Map<String, Object> knowledgeInfos = saParseLibraryHelper.queryKnowledgeInfos(filePath);
    saKnowledgeInfoDO.setReleaseDate((Date) knowledgeInfos.get("releaseDate"));
    saKnowledgeInfoDO.setVersion((String) knowledgeInfos.get("version"));
    saKnowledgeInfoDO.setImportDate((Date) knowledgeInfos.get("importDate"));

    return saKnowledgeInfoDO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.SaKnowledgeDao#queryKnowledgeRules(java.lang.String)
   */
  @Override
  public Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>,
      List<SaApplicationDO>> queryKnowledgeRules(String filePath) {
    // 检查当前文件时间与上次加载文件时间是否一致, 不一致则更新规则库cache
    long currentLoadTime = Paths.get(filePath).toFile().lastModified();
    if (rulesCache == null || this.lastLoadTime != currentLoadTime) {
      LOGGER.debug("reload knowledge file.");
      this.rulesCache = parseKnowledgeRules(filePath);
      this.lastLoadTime = currentLoadTime;
    }
    return rulesCache;
  }

  private Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>,
      List<SaApplicationDO>> parseKnowledgeRules(String filePath) {

    List<SaCategoryDO> categoryList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SaSubCategoryDO> subCatList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SaApplicationDO> appList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>,
        List<SaApplicationDO>> result = Tuples.of(categoryList, subCatList, appList);

    Tuple3<List<Map<String, String>>, List<Map<String, String>>, List<
        Map<String, String>>> knowledgeRules = saParseLibraryHelper.parseKnowledgeRules(filePath);

    knowledgeRules.getT1().forEach(category -> {
      SaCategoryDO categoryDO = new SaCategoryDO();
      categoryDO.setCategoryId(category.get("categoryId"));
      categoryDO.setName(category.get("name"));
      categoryDO.setNameText(category.get("nameText"));
      categoryDO.setDescription(category.get("description"));
      categoryDO.setDescriptionText(category.get("descriptionText"));
      categoryList.add(categoryDO);
    });
    knowledgeRules.getT2().forEach(subCategory -> {
      SaSubCategoryDO subCategoryDO = new SaSubCategoryDO();
      subCategoryDO.setCategoryId(subCategory.get("categoryId"));
      subCategoryDO.setSubCategoryId(subCategory.get("subCategoryId"));
      subCategoryDO.setName(subCategory.get("name"));
      subCategoryDO.setNameText(subCategory.get("nameText"));
      subCategoryDO.setDescription(subCategory.get("description"));
      subCategoryDO.setDescriptionText(subCategory.get("descriptionText"));
      subCatList.add(subCategoryDO);
    });
    knowledgeRules.getT3().forEach(application -> {
      SaApplicationDO applicationDO = new SaApplicationDO();
      applicationDO.setCategoryId(application.get("categoryId"));
      applicationDO.setSubCategoryId(application.get("subCategoryId"));
      applicationDO.setApplicationId(application.get("applicationId"));
      applicationDO.setName(application.get("name"));
      applicationDO.setNameText(application.get("nameText"));
      applicationDO.setDescription(application.get("description"));
      applicationDO.setDescriptionText(application.get("descriptionText"));
      appList.add(applicationDO);
    });

    return result;
  }

}
