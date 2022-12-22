package com.machloop.fpc.cms.center.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary.SaApplicationStructure;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary.SaCategoryStructure;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary.SaProtocolLabelStructure;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary.SaProtocolStructure;
import com.machloop.fpc.cms.center.global.library.SaParseLibrary.SaSubCategoryStructure;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月7日, fpc-manager
 */
@Component
public class SaParseLibraryHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaParseLibraryHelper.class);

  private long lastLoadFailTime;

  /**
   * 获取SA规则库文件信息
   * @param filePath
   * @return knowledge info: 
   * {"releaseDate": "发布时间","version": "当前版本","importDate": "导入时间"}
   */
  public synchronized Map<String, Object> queryKnowledgeInfos(String filePath) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    try {
      // 初始化
      init(filePath);

      // 获取规则库生成时间
      IntByReference timestampReference = new IntByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_time(timestampReference) == 0) {
        int timestamp = timestampReference.getValue();
        result.put("releaseDate", new Date(timestamp * 1000L));
        LOGGER.debug("parse from sa knowledge, timestamp:[{}]", timestamp);
      }

      // 获取版本
      PointerByReference versionReference = new PointerByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_version(versionReference) == 0
          && versionReference.getValue() != null) {
        String version = versionReference.getValue().getString(0L,
            StandardCharsets.UTF_8.toString());
        result.put("version", version);
        LOGGER.debug("parse from sa knowledge, version:[{}]", version);
      }

      // 导入时间 importDate
      result.put("importDate", new Date(Paths.get(filePath).toFile().lastModified()));

      return result;
    } catch (IOException e) {
      LOGGER.warn("failed to parse knowledge file.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "SA规则库文件解析失败");
    } finally {
      SaParseLibrary.INSTANCE.sa_libparse_deinit();
    }
  }

  /**
   * 解析SA规则
   * @param filePath
   * @return <p>reactor.util.function.Tuple3<分类集合, 子分类集合, 应用集合>  </p>
   * <p>分类：{"categoryId":"分类ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述"}</p>
   * <p>子分类：{"categoryId":"分类ID","subCategoryId":"子分类ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述"}</p>
   * <p>应用：{"categoryId":"分类ID","subCategoryId":"子分类ID","applicationId":"应用ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述"}</p>
   */
  public synchronized Tuple3<List<Map<String, String>>, List<Map<String, String>>,
      List<Map<String, String>>> parseKnowledgeRules(String filePath) {

    List<Map<String, String>> categoryList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, String>> subCatList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, String>> appList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<Map<String, String>>, List<Map<String, String>>,
        List<Map<String, String>>> result = Tuples.of(categoryList, subCatList, appList);
    try {
      // 初始化
      init(filePath);

      // 解析category
      IntByReference categoryCountReference = new IntByReference();
      PointerByReference categoryReference = new PointerByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_category(categoryReference,
          categoryCountReference) == 0) {
        try {
          SaCategoryStructure structureResult = Structure.newInstance(SaCategoryStructure.class,
              categoryReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(categoryCountReference.getValue());
          for (Structure structure : structures) {
            Map<String,
                String> category = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            SaCategoryStructure categoryStructure = (SaCategoryStructure) structure;

            category.put("categoryId", String.valueOf(categoryStructure.ucCategoryId));
            category.put("name", convertCTypeString(categoryStructure.cNameEn));
            category.put("nameText", convertCTypeString(categoryStructure.cNameCn));
            category.put("description", convertCTypeString(categoryStructure.cDescEn));
            category.put("descriptionText", convertCTypeString(categoryStructure.cDescCn));
            categoryList.add(category);
          }
        } finally {
          SaParseLibrary.INSTANCE.sa_libparse_free_result(categoryReference);
        }
      }

      // 解析subcategory
      PointerByReference subCategoryReference = new PointerByReference();
      IntByReference subCategoryCountReference = new IntByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_subcategory(subCategoryReference,
          subCategoryCountReference) == 0) {
        try {
          SaSubCategoryStructure structureResult = Structure
              .newInstance(SaSubCategoryStructure.class, subCategoryReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(subCategoryCountReference.getValue());
          for (Structure structure : structures) {
            SaSubCategoryStructure subCategoryStructure = (SaSubCategoryStructure) structure;

            Map<String,
                String> subCategory = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            subCategory.put("categoryId", String.valueOf(subCategoryStructure.ucCategoryId));
            subCategory.put("subCategoryId", String.valueOf(subCategoryStructure.uiSubCategoryId));
            subCategory.put("name", convertCTypeString(subCategoryStructure.cNameEn));
            subCategory.put("nameText", convertCTypeString(subCategoryStructure.cNameCn));
            subCategory.put("description", convertCTypeString(subCategoryStructure.cDescEn));
            subCategory.put("descriptionText", convertCTypeString(subCategoryStructure.cDescCn));
            subCatList.add(subCategory);
          }
        } finally {
          SaParseLibrary.INSTANCE.sa_libparse_free_result(subCategoryReference);
        }
      }

      // 解析application
      PointerByReference appReference = new PointerByReference();
      IntByReference appCountReference = new IntByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_application(appReference,
          appCountReference) == 0) {
        try {
          SaApplicationStructure structureResult = Structure
              .newInstance(SaApplicationStructure.class, appReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(appCountReference.getValue());
          for (Structure structure : structures) {
            SaApplicationStructure applicationStructure = (SaApplicationStructure) structure;

            Map<String,
                String> application = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            application.put("categoryId", String.valueOf(applicationStructure.ucCategoryId));
            application.put("subCategoryId", String.valueOf(applicationStructure.uiSubCategoryId));
            application.put("applicationId", String.valueOf(applicationStructure.uiApplicationId));
            application.put("name", convertCTypeString(applicationStructure.cNameEn));
            application.put("nameText", convertCTypeString(applicationStructure.cNameCn));
            application.put("description", convertCTypeString(applicationStructure.cDescEn));
            application.put("descriptionText", convertCTypeString(applicationStructure.cDescCn));
            appList.add(application);
          }
        } finally {
          SaParseLibrary.INSTANCE.sa_libparse_free_result(appReference);
        }
      }
      return result;
    } catch (IOException e) {
      LOGGER.warn("failed to parse knowledge file rules.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "SA规则库文件解析失败");
    } finally {
      SaParseLibrary.INSTANCE.sa_libparse_deinit();
    }
  }

  /**
   * 解析SA协议
   * @param filePath
   * @return <p>SA协议集合：</p>
   * [{"protocolId":"协议ID","name":"名称","nameText":"中文名称","description":"描述","descriptionText":"中文描述","standard":"是否为标准协议（0：否；1：是）","label":"id1,id2"}]
   */
  public synchronized List<Map<String, String>> parseProtocols(String filePath) {
    List<Map<String, String>> protocolList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      // 初始化
      init(filePath);

      // 获取protocol
      PointerByReference protocolReference = new PointerByReference();
      IntByReference protocolCountReference = new IntByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_protocol_and_label(protocolReference,
          protocolCountReference) == 0) {
        try {
          SaProtocolStructure structureResult = Structure.newInstance(SaProtocolStructure.class,
              protocolReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(protocolCountReference.getValue());
          for (Structure structure : structures) {
            SaProtocolStructure protocolStructure = (SaProtocolStructure) structure;

            Map<String,
                String> saProtocol = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            saProtocol.put("protocolId", String.valueOf(protocolStructure.uiProtocolId));
            saProtocol.put("name", convertCTypeString(protocolStructure.cNameEn));
            saProtocol.put("nameText", convertCTypeString(protocolStructure.cNameCn));
            saProtocol.put("description", convertCTypeString(protocolStructure.cDescEn));
            saProtocol.put("descriptionText", convertCTypeString(protocolStructure.cDescCn));
            saProtocol.put("standard", String.valueOf(protocolStructure.is_standard));
            saProtocol.put("label", convertCTypeString(protocolStructure.label));

            protocolList.add(saProtocol);
          }
        } finally {
          SaParseLibrary.INSTANCE.sa_libparse_free_result(protocolReference);
        }
      }
      return protocolList;
    } catch (IOException e) {
      LOGGER.warn("failed to parse knowledge file protocol.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "SA规则库文件解析失败");
    } finally {
      SaParseLibrary.INSTANCE.sa_libparse_deinit();
    }
  }

  /**
   * 解析SA协议标签
   * @param filePath
   * @return <p>SA协议标签集合：</p>
   * [{"labelId":"协议标签ID","name":"名称","nameText":"中文名称"}]
   */
  public synchronized List<Map<String, String>> parseProtocolLabels(String filePath) {
    List<Map<String, String>> saProtocolLabelList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      // 初始化
      init(filePath);

      // 获取protocol label
      PointerByReference labelReference = new PointerByReference();
      IntByReference labelCountReference = new IntByReference();
      if (SaParseLibrary.INSTANCE.sa_libparse_get_prolabel(labelReference,
          labelCountReference) == 0) {
        try {
          SaProtocolLabelStructure structureResult = Structure
              .newInstance(SaProtocolLabelStructure.class, labelReference.getValue());
          structureResult.read();
          Structure[] structures = structureResult.toArray(labelCountReference.getValue());
          for (Structure structure : structures) {
            SaProtocolLabelStructure labelStructure = (SaProtocolLabelStructure) structure;

            Map<String, String> saProtocolLabel = Maps
                .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            saProtocolLabel.put("labelId", String.valueOf(labelStructure.uiProlabelId));
            saProtocolLabel.put("name", convertCTypeString(labelStructure.cNameEn));
            saProtocolLabel.put("nameText", convertCTypeString(labelStructure.cNameCn));

            saProtocolLabelList.add(saProtocolLabel);
          }
        } finally {
          SaParseLibrary.INSTANCE.sa_libparse_free_result(labelReference);
        }
      }
      return saProtocolLabelList;
    } catch (IOException e) {
      LOGGER.warn("failed to parse knowledge file protocol label.", e);
      loadFailAlert(lastLoadFailTime);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "SA规则库文件解析失败");
    } finally {
      SaParseLibrary.INSTANCE.sa_libparse_deinit();
    }
  }

  private void init(String filePath) throws IOException {
    if (SaParseLibrary.INSTANCE.sa_libparse_init(null) != 0) {
      LOGGER.warn("failed to invoke sa_libparse_init.");
      throw new IOException("failed to init sa knowledge.");
    }
    if (SaParseLibrary.INSTANCE.sa_libparse_parse_file(filePath) != 0) {
      LOGGER.warn("failed to invoke sa_libparse_parse_file.");
      throw new IOException("failed to parse sa knowledge file.");
    }
  }

  /**
   * @param bytes
   * @return
   */
  private String convertCTypeString(byte[] bytes) {
    if (bytes == null) {
      return "";
    }
    int length = 0;
    for (byte b : bytes) {
      // 找到\0
      if (b == 0) {
        break;
      }
      length += 1;
    }
    if (length <= 0) {
      return "";
    }
    byte[] dest = new byte[length];
    System.arraycopy(bytes, 0, dest, 0, length);
    String result = StringUtils.toEncodedString(dest, StandardCharsets.UTF_8);
    return result;
  }

  /**
   * 告警
   * @param lastLoadFailTime
   */
  private void loadFailAlert(long lastLoadFailTime) {
    long current = System.currentTimeMillis();
    // 第一次产生告警或距离上一次告警30分钟, 生成告警信息
    if (lastLoadFailTime == 0L
        || current - lastLoadFailTime > 1000L * 30 * Constants.ONE_MINUTE_SECONDS) {
      AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, FpcCmsConstants.ALARM_CATEGORY_KNOWLEDGEBASE,
          "sa", "知识库文件损坏，加载SA知识库失败.");
    }
  }

}
