package com.machloop.fpc.cms.center.global.library;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * @author liyongjun
 *
 * create at 2019年11月22日, fpc-manager
 */
public interface LicenseLibrary extends Library {

  LicenseLibrary INSTANCE = Native.load("license", LicenseLibrary.class);

  /**
   * 函数名：license_get_esn
   * 功能：获取本机的序列号 
   * 参数：szHardWareId 序列号字符串开始地址，格式为 key：value 形式，具体字符串为中括号内的内容【Serial Number:XXX】
   * ulHardWareIdLen 序列号长度，不包括结尾\0
   * @return
   */
  int license_get_esn(Pointer esnPointer, IntByReference length);

  /**
   * 验证签名接口
   * 函数名：license_verify
   * 功能：校验license文件
   * 参数：pucFileName 文件名，要校验的文件绝对路径
   * 返回值：0 - 校验成功
   * 其他值校验失败
   * @param path
   * @return
   */
  int license_verify(String path);
}
