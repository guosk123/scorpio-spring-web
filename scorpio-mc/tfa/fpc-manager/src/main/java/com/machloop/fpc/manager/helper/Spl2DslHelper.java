package com.machloop.fpc.manager.helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.machloop.alpha.common.util.JsonHelper;

@Component
public class Spl2DslHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(Spl2DslHelper.class);

  private String scriptContent;

  @Value("${file.js.spl2dsl.path}")
  private String converterJsFilePath;

  @SuppressWarnings("unchecked")
  public String converte(String spl) throws IOException, V8ScriptExecutionException {
    if (scriptContent == null) {
      scriptContent = FileUtils.readFileToString(new File(converterJsFilePath),
          StandardCharsets.UTF_8);
    }
    V8 runtime = V8.createV8Runtime();
    runtime.executeObjectScript(scriptContent);
    V8Object converter = runtime.getObject("splToDslConverter");

    V8Array parameters = new V8Array(runtime);
    parameters.push(spl);

    V8Object result = converter.executeObjectFunction("parse", parameters);

    Map<String, Object> resultMap = JsonHelper.deserialize(result.getString("result"),
        new TypeReference<Map<String, Object>>() {
        });

    Map<String, Object> target = MapUtils.getMap(resultMap, "target");
    Map<String, Object> query = MapUtils.getMap(target, "query");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("spl2dsl converter target: [{}], dev: [{}] ",
          MapUtils.getString(resultMap, "target"), MapUtils.getString(resultMap, "dev"));
    }

    // 释放内存
    converter.release();
    parameters.release();
    result.release();
    runtime.release();
    return JsonHelper.serialize(query);
  }

  @SuppressWarnings("unchecked")
  public List<String> getFilterFields(String spl) throws IOException, V8ScriptExecutionException {
    if (scriptContent == null) {
      scriptContent = FileUtils.readFileToString(new File(converterJsFilePath),
          StandardCharsets.UTF_8);
    }
    V8 runtime = V8.createV8Runtime();
    runtime.executeObjectScript(scriptContent);
    V8Object converter = runtime.getObject("splToDslConverter");

    V8Array parameters = new V8Array(runtime);
    parameters.push(spl);

    V8Object result = converter.executeObjectFunction("parse", parameters);

    Map<String, Object> resultMap = JsonHelper.deserialize(result.getString("result"),
        new TypeReference<Map<String, Object>>() {
        });
    Map<String, Object> dev = MapUtils.getMap(resultMap, "dev");
    List<String> fields = JsonHelper.deserialize(JsonHelper.serialize(dev.get("fields")),
        new TypeReference<List<String>>() {
        });

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("spl2dsl converter target: [{}], dev: [{}] ",
          MapUtils.getString(resultMap, "target"), MapUtils.getString(resultMap, "dev"));
    }

    // 释放内存
    converter.release();
    parameters.release();
    result.release();
    runtime.release();
    return fields;
  }


}
