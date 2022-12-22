package com.machloop.fpc.manager.restapi;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.analysis.bo.MitreAttackBO;
import com.machloop.fpc.npm.analysis.bo.SuricataRuleBO;
import com.machloop.fpc.npm.analysis.bo.SuricataRuleClasstypeBO;
import com.machloop.fpc.npm.analysis.service.MitreAttackService;
import com.machloop.fpc.npm.analysis.service.SuricataRuleClasstypeService;
import com.machloop.fpc.npm.analysis.service.SuricataRuleService;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author minjiajun
 *
 * create at 2022年6月15日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class SuricataRuleRestAPIController {

  private static final int TASK_EXECUTING_CODE = 42002;

  @Value("${file.suricata.result.path}")
  private String suricataResultPath;

  @Autowired
  private SuricataRuleService suricataRuleService;

  @Autowired
  private SuricataRuleClasstypeService suricataRuleClasstypeService;

  @Autowired
  private UserService userService;

  @Autowired
  private MitreAttackService mitreAttackService;

  @GetMapping("/suricata-rules/sources")
  @RestApiSecured
  public Map<String, String> queryRuleSource() {
    return suricataRuleService.queryRuleSource();
  }

  @GetMapping("/suricata-rules/classtypes")
  @RestApiSecured
  public List<Map<String, Object>> querySuricataRuleClasstypes() {

    List<SuricataRuleClasstypeBO> suricataRuleClasstypeBOS = suricataRuleClasstypeService
        .querySuricataRuleClasstypes(null, null);
    return suricataRuleClasstypeBOS.stream()
        .map(suricataRuleClasstype -> suricataRuleClasstype2Map(suricataRuleClasstype))
        .collect(Collectors.toList());
  }

  @GetMapping("/suricata-rules/mitre-attacks")
  @RestApiSecured
  public List<Map<String, Object>> queryMitreAttacks() {

    List<MitreAttackBO> mitreAttackBOS = mitreAttackService.queryMitreAttacks(null, null);
    return mitreAttackBOS.stream().map(item -> mitreAttack2Map(item)).collect(Collectors.toList());
  }

  @GetMapping("/suricata-rules")
  @RestApiSecured
  public Page<Map<String, Object>> querySuricataRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      SuricataRuleQueryVO queryVO) {
    Sort sort = new Sort(new Order(Sort.Direction.DESC, "create_time"),
        new Order(Sort.Direction.ASC, "sid"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<SuricataRuleBO> suricataRulesPage = suricataRuleService.querySuricataRules(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(suricataRulesPage.getSize());
    for (SuricataRuleBO suricataRuleBO : suricataRulesPage) {
      resultList.add(suricataRule2Map(suricataRuleBO));
    }

    return new PageImpl<>(resultList, page, suricataRulesPage.getTotalElements());
  }

  @PostMapping("/suricata-rules")
  @RestApiSecured
  public RestAPIResultVO importSuricataRules(@RequestParam MultipartFile file,
      @RequestParam(required = false, defaultValue = "60") int timeout,
      HttpServletRequest request) {
    try {
      suricataRuleService.importSuricataKnowledges(file);

      long beginTime = DateUtils.now().getTime();
      while (DateUtils.now().getTime() - beginTime <= timeout * 1000
          && !Thread.currentThread().isInterrupted()) {
        File read = new File(suricataResultPath.toString());
        long modifyTime = read.exists() ? read.lastModified() : 0L;
        if (modifyTime != 0L && beginTime < modifyTime) {
          String result = readFile(read.toString());
          return RestAPIResultVO.resultSuccess(result);
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    return new RestAPIResultVO.Builder(TASK_EXECUTING_CODE).msg(
        String.format("It took more than %ss to load the rule file, please try again.", timeout))
        .build();
  }

  @PostMapping("/suricata-rules/as-import")
  @RestApiSecured
  public void importSuricataRules(@RequestParam MultipartFile file, String classtypeId,
      String source, HttpServletRequest request) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名称存在非法字符：[%s]", matchingIllegalCharacters));
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    int importSuricataRules = suricataRuleService.importSuricataRules(file, classtypeId, source,
        userBO.getId());

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "导入" + importSuricataRules + "条安全分析规则");
  }

  @PostMapping("/suricata-rules/as-import/issued")
  @RestApiSecured
  public RestAPIResultVO importIssuedSuricataRules(@RequestParam MultipartFile file,
      String classtypeId, String source, HttpServletRequest request) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名称存在非法字符：[%s]", matchingIllegalCharacters));
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    int importSuricataRules = 0;
    try {
      importSuricataRules = suricataRuleService.importIssuedSuricataRules(file, classtypeId, source,
          userBO.getId());
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }


    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "导入" + importSuricataRules + "条Suricata规则");

    return RestAPIResultVO.resultSuccess("导入成功。");
  }

  @PutMapping("/suricata-rules/update")
  @RestApiSecured
  public void updateRules(@RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false, defaultValue = "") String sids,
      @RequestParam(required = false, defaultValue = "") String state,
      @RequestParam(required = false, defaultValue = "") String classtypeId,
      @RequestParam(required = false, defaultValue = "") String source,
      @RequestParam(required = false, defaultValue = "") String mitreTacticId,
      @RequestParam(required = false, defaultValue = "") String mitreTechniqueId,
      HttpServletRequest request) {

    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(query)) {
      param = JsonHelper.deserialize(query, new TypeReference<Map<String, Object>>() {

      });
    }
    queryVO = MapToSuricataRule(param);

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    suricataRuleService.batchUpdateSuricataRule(queryVO,
        CsvUtils.convertCSVToList(sids).stream().map(sid -> Integer.parseInt(sid))
            .collect(Collectors.toList()),
        state, classtypeId, source, mitreTacticId, mitreTechniqueId, userBO.getId());

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(), "修改过滤条件 " + query + "，修改"
        + (StringUtils.isNotBlank(state) ? "state为" + state : "")
        + (StringUtils.isNotBlank(classtypeId) ? "classtypeIds为" + classtypeId : "")
        + (StringUtils.isNotBlank(source) ? "source为" + source : "")
        + (StringUtils.isNotBlank(mitreTacticId) ? "mitreTacticIds为" + mitreTacticId : "")
        + (StringUtils.isNotBlank(mitreTechniqueId) ? "mitreTechniqueId为" + mitreTechniqueId : ""));
  }

  @PutMapping("/suricata-rules/state")
  @RestApiSecured
  public void updateState(@RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false, defaultValue = "") String sids, @RequestParam String state,
      HttpServletRequest request) {

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    if (!StringUtils.equalsAny(state, Constants.BOOL_YES, Constants.BOOL_NO)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "状态只能选择1（启用）和0（禁用）");
    }

    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(query)) {
      param = JsonHelper.deserialize(query, new TypeReference<Map<String, Object>>() {

      });
    }
    queryVO = MapToSuricataRule(param);

    suricataRuleService.updateSuricataState(CsvUtils.convertCSVToList(sids), queryVO, state,
        userBO.getId());

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        StringUtils.equals(state, Constants.BOOL_YES) ? "启用全部suricata规则：" : "禁用全部suricata规则：");
  }

  @DeleteMapping("/suricata-rules")
  @RestApiSecured
  public void deleteSuricataRule(@RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false, defaultValue = "") String sids, HttpServletRequest request) {

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(query)) {
      param = JsonHelper.deserialize(query, new TypeReference<Map<String, Object>>() {

      });
    }
    queryVO = MapToSuricataRule(param);

    suricataRuleService.deleteSuricataRule(CsvUtils.convertCSVToList(sids), queryVO,
        userBO.getId());

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(), "删除全部suricata规则");
  }

  @SuppressWarnings("resource")
  private String readFile(String path) {
    InputStream is = null;
    try {
      is = new FileInputStream(path);
    } catch (FileNotFoundException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "读取suricata结果文件失败");
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    StringBuffer buffer = new StringBuffer();
    String line = "";
    try {
      while ((line = in.readLine()) != null) {
        buffer.append(line);
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "读取suricata结果文件失败");
    }
    return buffer.toString();
  }

  private Map<String, Object> suricataRuleClasstype2Map(
      SuricataRuleClasstypeBO suricataRuleClasstypeBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", suricataRuleClasstypeBO.getId());
    map.put("name", suricataRuleClasstypeBO.getName());
    map.put("ruleSize", suricataRuleClasstypeBO.getRuleSize());
    map.put("createTime", suricataRuleClasstypeBO.getCreateTime());

    return map;
  }

  private Map<String, Object> mitreAttack2Map(MitreAttackBO mitreAttackBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", mitreAttackBO.getId());
    map.put("name", mitreAttackBO.getName());
    map.put("parentId", mitreAttackBO.getParentId());
    map.put("ruleSize", mitreAttackBO.getRuleSize());

    return map;
  }

  private Map<String, Object> suricataRule2Map(SuricataRuleBO suricataRuleBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", suricataRuleBO.getId());
    map.put("sid", suricataRuleBO.getSid());
    map.put("action", suricataRuleBO.getAction());
    map.put("protocol", suricataRuleBO.getProtocol());
    map.put("srcIp", suricataRuleBO.getSrcIp());
    map.put("srcPort", suricataRuleBO.getSrcPort());
    map.put("direction", suricataRuleBO.getDirection());
    map.put("destIp", suricataRuleBO.getDestIp());
    map.put("destPort", suricataRuleBO.getDestPort());
    map.put("msg", suricataRuleBO.getMsg());
    map.put("rev", suricataRuleBO.getRev());
    map.put("content", suricataRuleBO.getContent());
    map.put("priority", suricataRuleBO.getPriority());
    map.put("classtypeId", suricataRuleBO.getClasstypeId());
    map.put("mitreTacticId", suricataRuleBO.getMitreTacticId());
    map.put("mitreTechniqueId", suricataRuleBO.getMitreTechniqueId());
    map.put("cve", suricataRuleBO.getCve());
    map.put("cnnvd", suricataRuleBO.getCnnvd());
    map.put("signatureSeverity", suricataRuleBO.getSignatureSeverity());
    map.put("signatureSeverityText", suricataRuleBO.getSignatureSeverityText());
    map.put("target", suricataRuleBO.getTarget());
    map.put("threshold", suricataRuleBO.getThreshold());
    map.put("parseState", suricataRuleBO.getParseState());
    map.put("parseStateText", suricataRuleBO.getParseStateText());
    map.put("parseLog", suricataRuleBO.getParseLog());
    map.put("state", suricataRuleBO.getState());
    map.put("source", suricataRuleBO.getSource());
    map.put("sourceText", suricataRuleBO.getSourceText());
    map.put("createTime", suricataRuleBO.getCreateTime());

    return map;
  }

  private SuricataRuleQueryVO MapToSuricataRule(Map<String, Object> param) {
    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    queryVO.setSrcIp(MapUtils.getString(param, "srcIp"));
    queryVO.setSrcPort(MapUtils.getString(param, "srcPort"));
    queryVO.setDestIp(MapUtils.getString(param, "destIp"));
    queryVO.setDestPort(MapUtils.getString(param, "destPort"));
    queryVO.setProtocol(MapUtils.getString(param, "protocol"));
    queryVO.setDirection(MapUtils.getString(param, "direction"));
    queryVO.setSid(MapUtils.getString(param, "sid"));
    queryVO.setClasstypeIds(MapUtils.getString(param, "classtypeIds"));
    queryVO.setMitreTacticIds(MapUtils.getString(param, "mitreTacticIds"));
    queryVO.setMitreTechniqueIds(MapUtils.getString(param, "mitreTechniqueIds"));
    queryVO.setCve(MapUtils.getString(param, "cve"));
    queryVO.setCnnvd(MapUtils.getString(param, "cnnvd"));
    queryVO.setPriority(MapUtils.getIntValue(param, "priority") == 0 ? null
        : MapUtils.getIntValue(param, "priority"));
    queryVO.setSignatureSeverity(MapUtils.getString(param, "signatureSeverity"));
    queryVO.setTarget(MapUtils.getString(param, "target"));
    queryVO.setState(MapUtils.getString(param, "state"));
    queryVO.setSource(MapUtils.getString(param, "source"));
    queryVO.setParseState(MapUtils.getString(param, "parseState"));

    return queryVO;
  }
}
