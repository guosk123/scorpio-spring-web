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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ScriptExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.knowledge.service.SaService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class Spl2SqlHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(Spl2SqlHelper.class);

  private static final int DEFAULT_TIME_PRECISION = 9;

  private String scriptContent;

  @Value("${file.js.spl2sql.path}")
  private String converterJsFilePath;

  @Autowired
  private SaService saService;

  public Tuple2<String, Map<String, Object>> converte(String spl, boolean hasAgingTime,
      Integer timePrecision, boolean includeStartTime, boolean includeEndTime)
      throws IOException, V8ScriptExecutionException {
    if (scriptContent == null) {
      scriptContent = FileUtils.readFileToString(new File(converterJsFilePath),
          StandardCharsets.UTF_8);
    }
    V8 runtime = V8.createV8Runtime();
    runtime.executeObjectScript(scriptContent);
    V8Object converter = runtime.getObject("splToSqlConverter");

    V8Array parameters = new V8Array(runtime);
    parameters.push(spl);

    // 获取应用id和名称的映射关系，用于解析
    Map<Integer, String> applicationMapping = saService.queryAllAppsIdNameMapping();
    V8Object optionsObject = new V8Object(runtime)
        .add("applications", JsonHelper.serialize(applicationMapping))
        .add("hasAgingTime", hasAgingTime).add("includeStartTime", includeStartTime)
        .add("includeEndTime", includeEndTime)
        .add("timePrecision", timePrecision != null ? timePrecision : DEFAULT_TIME_PRECISION);
    parameters.push(optionsObject);

    V8Object result = converter.executeObjectFunction("parse", parameters);

    Map<String, Object> resultMap = JsonHelper.deserialize(result.getString("result"),
        new TypeReference<Map<String, Object>>() {
        });
    String sql = MapUtils.getString(resultMap, "target");
    @SuppressWarnings("unchecked")
    Map<String, Object> params = MapUtils.getMap(resultMap, "params");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("dsl converter source: [{}], target: [{}], params: [{}], dev: [{}] ",
          MapUtils.getString(resultMap, "source"), MapUtils.getString(resultMap, "target"),
          MapUtils.getString(resultMap, "params"), MapUtils.getString(resultMap, "dev"));
    }

    // 释放内存
    converter.release();
    optionsObject.release();
    parameters.release();
    result.release();
    runtime.release();
    return Tuples.of(sql, params);
  }

  public List<String> getFilterFields(String spl) throws IOException, V8ScriptExecutionException {
    if (scriptContent == null) {
      scriptContent = FileUtils.readFileToString(new File(converterJsFilePath),
          StandardCharsets.UTF_8);
    }
    V8 runtime = V8.createV8Runtime();
    runtime.executeObjectScript(scriptContent);
    V8Object converter = runtime.getObject("splToSqlConverter");

    V8Array parameters = new V8Array(runtime);
    parameters.push(spl);

    V8Object result = converter.executeObjectFunction("parse", parameters);

    Map<String, Object> resultMap = JsonHelper.deserialize(result.getString("result"),
        new TypeReference<Map<String, Object>>() {
        });
    @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getFilterContent(String spl)
      throws IOException, V8ScriptExecutionException {
    if (scriptContent == null) {
      scriptContent = FileUtils.readFileToString(new File(converterJsFilePath),
          StandardCharsets.UTF_8);
    }
    V8 runtime = V8.createV8Runtime();
    runtime.executeObjectScript(scriptContent);
    V8Object converter = runtime.getObject("splToSqlConverter");

    V8Array parameters = new V8Array(runtime);
    parameters.push(spl);

    V8Object result = converter.executeObjectFunction("parse", parameters);

    Map<String, Object> resultMap = JsonHelper.deserialize(result.getString("result"),
        new TypeReference<Map<String, Object>>() {
        });

    Map<String, Object> dev = MapUtils.getMap(resultMap, "dev");
    List<Map<String, Object>> filterContents = JsonHelper.deserialize(
        JsonHelper.serialize(dev.get("fieldCollection")),
        new TypeReference<List<Map<String, Object>>>() {
        });

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("spl2dsl converter filterContent: [{}] ",
          JsonHelper.serialize(filterContents, false));
    }

    // 释放内存
    converter.release();
    parameters.release();
    result.release();
    runtime.release();
    return filterContents;
  }

}
