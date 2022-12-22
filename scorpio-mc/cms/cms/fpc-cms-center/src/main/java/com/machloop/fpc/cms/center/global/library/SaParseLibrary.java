package com.machloop.fpc.cms.center.global.library;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface SaParseLibrary extends Library {

  static final int LIBPARSE_NAME_LEN = 64;
  static final int LIBPARSE_DESCRIPTION_LEN = 256;
  static final int LIBPARSE_LABEL_LEN = 256;

  SaParseLibrary INSTANCE = Native.load("parse", SaParseLibrary.class);

  // int sa_libparse_init(void * pfCompile) 初始化接口, 返回0初始化成功
  int sa_libparse_init(Pointer pfCompile);

  // void sa_libparse_deinit(void) 去初始化接口
  void sa_libparse_deinit();

  // int sa_libparse_parse_file(char *pcFilePathIn) 规则库密文文件解析接口, pcFilePathIn:规则库密文文件路径, 返回0解析成功
  int sa_libparse_parse_file(String pcFilePathIn);

  // int sa_libparse_get_version(unsigned char **ppucVersion) ppucVersion:存储规则库版本号的指针，输出参数,
  // 返回0获取成功
  int sa_libparse_get_version(PointerByReference ppucVersion);

  // int sa_libparse_get_time(unsigned int *timestamp) timestamp:存储规则库时间戳的指针，输出参数, 返回0获取成功
  int sa_libparse_get_time(IntByReference timestamp);

  // int sa_libparse_get_category(LIBPARSE_CATEGORY **ppstCategory, unsigned int *puiNum)
  // 获取规则库支持的所有分类信息
  int sa_libparse_get_category(PointerByReference ppstCategory, IntByReference puiNum);

  // int sa_libparse_get_subcategory(LIBPARSE_SUBCATEGORY **ppstSubcategory, unsigned int *puiNum)
  // 获取规则库支持的所有子分类信息
  int sa_libparse_get_subcategory(PointerByReference ppstSubcategory, IntByReference puiNum);

  // int sa_libparse_get_application(LIBPARSE_APPLICATION **ppstApp, unsigned int *puiNum)
  // 获取规则库支持的所有应用信息
  int sa_libparse_get_application(PointerByReference ppstApp, IntByReference puiNum);

  // int sa_libparse_get_category_by_app(unsigned int uiApplicationId, unsigned int
  // *puiSubCategoryId, unsigned char *pucCategoryId) 根据应用ID，获取其对应的分类ID和子分类ID
  int sa_libparse_get_category_by_app(int uiApplicationId, IntByReference puiSubCategoryId,
      String pucCategoryId);

  // int sa_libparse_get_category_info_by_id(unsigned char ucCategoryId, LIBPARSE_CATEGORY
  // **ppstCategory)
  // 获取分类ID对应的分类信息
  int sa_libparse_get_category_info_by_id(char ucCategoryId, PointerByReference ppstCategory);

  // int sa_libparse_get_subcategory_info_by_id(unsigned int uiSubCategoryId, LIBPARSE_SUBCATEGORY
  // **ppstSubCategory) 获取子分类ID对应的子分类信息
  int sa_libparse_get_subcategory_info_by_id(int uiSubCategoryId,
      PointerByReference ppstSubCategory);

  // int sa_libparse_get_application_info_by_id(unsigned int uiApplicationId, LIBPARSE_APPLICATION
  // **ppstApplication) 获取应用ID对应的应用信息
  int sa_libparse_get_application_info_by_id(int uiApplicationId,
      PointerByReference ppstApplication);

  // int sa_libparse_get_protocol(LIBPARSE_PROTOCOL **ppstProtocol, unsigned int *puiNum)
  // 获取协议信息
  int sa_libparse_get_protocol(PointerByReference ppstProtocol, IntByReference puiNum);

  // int sa_libparse_get_protocol_and_label(LIBPARSE_PROTOCOL_AND_LABEL **ppstProtocol, unsigned int
  // *puiNum);
  // 获取协议信息（包含协议的标签ID）
  int sa_libparse_get_protocol_and_label(PointerByReference ppstProtocol, IntByReference puiNum);

  // int sa_libparse_get_prolabel(LIBPARSE_PROLABEL **ppstProlabel, unsigned int *puiNum);
  int sa_libparse_get_prolabel(PointerByReference ppstProlabel, IntByReference puiNum);

  // void sa_libparse_free_result(void **ppstResult) 获取规则库相关资源后，需调用此接口释放结果集
  void sa_libparse_free_result(PointerByReference ppstResult);

  public class SaCategoryStructure extends Structure {

    public static class ByReference extends SaCategoryStructure implements Structure.ByReference {
    }

    public static class ByValue extends SaCategoryStructure implements Structure.ByValue {
    }

    public byte ucCategoryId;
    public byte[] ucRes;
    public byte[] cNameCn;
    public byte[] cNameEn;
    public byte[] cDescCn;
    public byte[] cDescEn;

    public SaCategoryStructure() {
      super(Structure.ALIGN_GNUC);
      this.ucRes = new byte[3];
      this.cNameCn = new byte[LIBPARSE_NAME_LEN];
      this.cNameEn = new byte[LIBPARSE_NAME_LEN];
      this.cDescCn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.cDescEn = new byte[LIBPARSE_DESCRIPTION_LEN];
    }

    @Override
    public String toString() {
      return "SaCategoryStructure [ucCategoryId=" + ucCategoryId + ", ucRes="
          + Arrays.toString(ucRes) + ", cNameCn=" + Arrays.toString(cNameCn) + ", cNameEn="
          + Arrays.toString(cNameEn) + ", cDescCn=" + Arrays.toString(cDescCn) + ", cDescEn="
          + Arrays.toString(cDescEn) + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("ucCategoryId", "ucRes", "cNameCn", "cNameEn", "cDescCn", "cDescEn");
    }
  }

  public class SaSubCategoryStructure extends Structure {

    public byte ucCategoryId;
    public byte[] ucRes;
    public int uiSubCategoryId;
    public byte[] cNameCn;
    public byte[] cNameEn;
    public byte[] cDescCn;
    public byte[] cDescEn;

    public SaSubCategoryStructure() {
      super(Structure.ALIGN_GNUC);
      this.ucRes = new byte[3];
      this.cNameCn = new byte[LIBPARSE_NAME_LEN];
      this.cNameEn = new byte[LIBPARSE_NAME_LEN];
      this.cDescCn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.cDescEn = new byte[LIBPARSE_DESCRIPTION_LEN];
    }

    @Override
    public String toString() {
      return "SaSubCategoryStructure [ucCategoryId=" + ucCategoryId + ", ucRes=" + ucRes
          + ", uiSubCategoryId=" + uiSubCategoryId + ", cNameCn=" + Arrays.toString(cNameCn)
          + ", cNameEn=" + Arrays.toString(cNameEn) + ", cDescCn=" + Arrays.toString(cDescCn)
          + ", cDescEn=" + Arrays.toString(cDescEn) + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("ucCategoryId", "ucRes", "uiSubCategoryId", "cNameCn", "cNameEn",
          "cDescCn", "cDescEn");
    }
  }

  public class SaApplicationStructure extends Structure {

    public byte ucCategoryId;
    public byte[] ucRes;
    public int uiSubCategoryId;
    public int uiApplicationId;
    public byte[] cNameCn;
    public byte[] cNameEn;
    public byte[] cDescCn;
    public byte[] cDescEn;

    public SaApplicationStructure() {
      super(Structure.ALIGN_GNUC);
      this.ucRes = new byte[3];
      this.cNameCn = new byte[LIBPARSE_NAME_LEN];
      this.cNameEn = new byte[LIBPARSE_NAME_LEN];
      this.cDescCn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.cDescEn = new byte[LIBPARSE_DESCRIPTION_LEN];
    }

    @Override
    public String toString() {
      return "SaApplicationStructure [ucCategoryId=" + ucCategoryId + ", ucRes=" + ucRes
          + ", uiSubCategoryId=" + uiSubCategoryId + ", uiApplicationId=" + uiApplicationId
          + ", cNameCn=" + Arrays.toString(cNameCn) + ", cNameEn=" + Arrays.toString(cNameEn)
          + ", cDescCn=" + Arrays.toString(cDescCn) + ", cDescEn=" + Arrays.toString(cDescEn) + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("ucCategoryId", "ucRes", "uiSubCategoryId", "uiApplicationId", "cNameCn",
          "cNameEn", "cDescCn", "cDescEn");
    }
  }

  public class SaProtocolStructure extends Structure {

    public static class ByReference extends SaProtocolStructure implements Structure.ByReference {
    }

    public static class ByValue extends SaProtocolStructure implements Structure.ByValue {
    }

    public int uiProtocolId;
    public byte[] cNameCn;
    public byte[] cNameEn;
    public byte[] cDescCn;
    public byte[] cDescEn;
    public byte is_standard;
    public byte[] res;
    public byte[] label;

    public SaProtocolStructure() {
      super(Structure.ALIGN_GNUC);
      this.cNameCn = new byte[LIBPARSE_NAME_LEN];
      this.cNameEn = new byte[LIBPARSE_NAME_LEN];
      this.cDescCn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.cDescEn = new byte[LIBPARSE_DESCRIPTION_LEN];
      this.is_standard = 0;
      this.res = new byte[3];
      this.label = new byte[LIBPARSE_LABEL_LEN];
    }

    @Override
    public String toString() {
      return "SaProtocolStructure [uiProtocolId=" + uiProtocolId + ", cNameCn="
          + Arrays.toString(cNameCn) + ", cNameEn=" + Arrays.toString(cNameEn) + ", cDescCn="
          + Arrays.toString(cDescCn) + ", cDescEn=" + Arrays.toString(cDescEn) + ", is_standard="
          + is_standard + ", res=" + Arrays.toString(res) + ", label=" + Arrays.toString(label)
          + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("uiProtocolId", "cNameCn", "cNameEn", "cDescCn", "cDescEn",
          "is_standard", "res", "label");
    }
  }

  public class SaProtocolLabelStructure extends Structure {

    public static class ByReference extends SaProtocolLabelStructure
        implements Structure.ByReference {
    }

    public static class ByValue extends SaProtocolLabelStructure implements Structure.ByValue {
    }

    public int uiProlabelId;
    public byte[] cNameCn;
    public byte[] cNameEn;

    public SaProtocolLabelStructure() {
      super(Structure.ALIGN_GNUC);
      this.cNameCn = new byte[LIBPARSE_NAME_LEN];
      this.cNameEn = new byte[LIBPARSE_NAME_LEN];
    }

    @Override
    public String toString() {
      return "SaProtocolLabelStructure [uiProlabelId=" + uiProlabelId + ", cNameCn="
          + Arrays.toString(cNameCn) + ", cNameEn=" + Arrays.toString(cNameEn) + "]";
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("uiProlabelId", "cNameCn", "cNameEn");
    }
  }

}

