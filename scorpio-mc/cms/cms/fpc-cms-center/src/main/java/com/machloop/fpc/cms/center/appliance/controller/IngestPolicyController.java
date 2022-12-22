package com.machloop.fpc.cms.center.appliance.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.cms.center.appliance.service.IngestPolicyService;
import com.machloop.fpc.cms.center.appliance.vo.IngestPolicyCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.IngestPolicyModificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class IngestPolicyController {

  @Autowired
  private IngestPolicyService ingestPolicyService;

  @GetMapping("/ingest-policies")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryIngestPolicys(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    Sort sort = new Sort(new Order(Sort.Direction.ASC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<IngestPolicyBO> ingestPolicys = ingestPolicyService.queryIngestPolicys(page);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(ingestPolicys.getSize());
    ingestPolicys.forEach(ingestPolicy -> {
      Map<String, Object> ingestPolicyMap = ingestPolicyBO2Map(ingestPolicy, false);
      ingestPolicyMap.put("referenceCount", ingestPolicy.getReferenceCount());
      result.add(ingestPolicyMap);
    });

    return new PageImpl<>(result, page, ingestPolicys.getTotalElements());
  }

  @GetMapping("/ingest-policies/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryIngestPolicys() {
    List<IngestPolicyBO> ingestPolicys = ingestPolicyService.queryIngestPolicys();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(ingestPolicys.size());
    ingestPolicys.forEach(ingestPolicy -> {
      result.add(ingestPolicyBO2Map(ingestPolicy, false));
    });

    return result;
  }

  @GetMapping("/ingest-policies/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIngestPolicy(@PathVariable String id) {
    IngestPolicyBO ingestPolicy = ingestPolicyService.queryIngestPolicy(id);

    return ingestPolicyBO2Map(ingestPolicy, true);
  }

  @PostMapping("/ingest-policies")
  @Secured({"PERM_USER"})
  public void saveIngestPolicy(@Validated IngestPolicyCreationVO ingestPolicyVO) {
    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    BeanUtils.copyProperties(ingestPolicyVO, ingestPolicyBO);

    IngestPolicyBO result = ingestPolicyService.saveIngestPolicy(ingestPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/ingest-policies/{id}")
  @Secured({"PERM_USER"})
  public void updateIngestPolicy(@PathVariable String id,
      @Validated IngestPolicyModificationVO ingestPolicyVO) {
    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    BeanUtils.copyProperties(ingestPolicyVO, ingestPolicyBO);

    IngestPolicyBO result = ingestPolicyService.updateIngestPolicy(id, ingestPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/ingest-policies/{id}")
  @Secured({"PERM_USER"})
  public void deleteIngestPolicy(@PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id) {
    IngestPolicyBO result = ingestPolicyService.deleteIngestPolicy(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }

  private Map<String, Object> ingestPolicyBO2Map(IngestPolicyBO ingestPolicyBO, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", ingestPolicyBO.getId());
    map.put("name", ingestPolicyBO.getName());
    map.put("description", ingestPolicyBO.getDescription());
    if (isDetail) {
      map.put("defaultAction", ingestPolicyBO.getDefaultAction());
      map.put("defaultActionText", ingestPolicyBO.getDefaultActionText());
      map.put("exceptBpf", ingestPolicyBO.getExceptBpf());
      map.put("exceptTuple", ingestPolicyBO.getExceptTuple());
      map.put("deduplication", ingestPolicyBO.getDeduplication());
      map.put("deduplicationText", ingestPolicyBO.getDeduplicationText());
      map.put("createTime", ingestPolicyBO.getCreateTime());
      map.put("updateTime", ingestPolicyBO.getUpdateTime());
    }

    return map;
  }

}
