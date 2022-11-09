package com.scorpio.jna.lib;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface JnaLibrary extends Library {

  static final int LIBPARSE_NAME_LEN = 64;
  static final int LIBPARSE_DESCRIPTION_LEN = 256;

  /**
   * 加载so文件，其中{so_name}是so的文件名，如：libbpf.so > bpf
   */
  JnaLibrary INSTANCE = Native.load("so_name", JnaLibrary.class);

  /**
   * 根据so的头文件，去写方法和实体内容
   *
   * 接下来将要用到的so内部的方法写出来，名称、参数名保持一致，数据类型参考jna文档一一对应
   */

  // int lib_init(void * pfCompile)
  int lib_init(Pointer pfCompile);

  // int lib_parse_file(char *pcFilePathIn, unsigned int uiApplicationId)
  int lib_parse_file(String pcFilePathIn, int uiApplicationId);

  // int lib_get_message(unsigned char msgId, LIBPARSE_CATEGORY **ppstBody, unsigned int *puiNum)
  int lib_get_message(char msgId,  PointerByReference ppstBody, IntByReference puiNum);

  /**
   * 实体，根据so内实体的变量去编写
   */
  public class MsgStructure extends Structure {

    public static class ByReference extends MsgStructure implements Structure.ByReference {
    }

    public static class ByValue extends MsgStructure implements Structure.ByValue {
    }

    public byte id;
    public byte[] cName;

    public MsgStructure() {
      super(Structure.ALIGN_GNUC);
      this.cName = new byte[LIBPARSE_NAME_LEN];
    }

    @Override
    public String toString() {
      return "MsgStructure [id=" + id + ", cName=" + Arrays.toString(cName);
    }

    @Override
    protected List<String> getFieldOrder() {
      return Arrays.asList("id", "ucRes", "cName");
    }
  }

}
