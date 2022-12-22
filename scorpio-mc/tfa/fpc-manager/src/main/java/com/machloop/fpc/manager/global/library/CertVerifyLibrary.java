package com.machloop.fpc.manager.global.library;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CertVerifyLibrary extends Library {

  CertVerifyLibrary INSTANCE = Native.load("tlsfun", CertVerifyLibrary.class);

  /**
   *  验证签名接口，返回0成功，返回其他失败，int check_pri_key_file(char * filepath);
  */
  int check_pri_key_file(String path);
}
