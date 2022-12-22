package com.machloop.fpc.cms.npm.analysis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.npm.analysis.bo.MitreAttackBO;
import com.machloop.fpc.cms.npm.analysis.dao.MitreAttackDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataAlertMessageDao;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataRuleDao;
import com.machloop.fpc.cms.npm.analysis.data.MitreAttackDO;
import com.machloop.fpc.cms.npm.analysis.service.MitreAttackService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:33 AM,cms
 * @version 1.0
 */
@Service
public class MitreAttackServiceImpl implements MitreAttackService {

  @Autowired
  private MitreAttackDao mitreAttackDao;

  @Autowired
  private SuricataRuleDao suricataRuleDao;

  @Autowired
  private SuricataAlertMessageDao suricataAlertMessageDao;

  @Override
  public List<MitreAttackBO> queryMitreAttacks(Date startTime, Date endTime) {
    List<MitreAttackDO> mitreAttacks = mitreAttackDao.queryMitreAttacks();

    // 战术分类/技术分类包含的规则分布
    Map<String,
        Integer> mitreUsedCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    mitreUsedCount.putAll(suricataRuleDao.statisticsByMitreTactic());
    mitreUsedCount.putAll(suricataRuleDao.statisticsByMitreTechnique());

    // 战术分类/技术分类包含的告警分布
    Map<String, Long> mitreAlertCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (startTime != null && endTime != null) {
      SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
      queryVO.setStartTimeDate(startTime);
      queryVO.setEndTimeDate(endTime);
      List<Map<String, Object>> statisticsMitreAttack = suricataAlertMessageDao
          .statisticsMitreAttack(queryVO);

      mitreAlertCount.putAll(statisticsMitreAttack.stream()
          .filter(item -> StringUtils.isNotBlank(MapUtils.getString(item, "mitreTacticId")))
          .collect(Collectors.groupingBy(item -> MapUtils.getString(item, "mitreTacticId"),
              Collectors.summingLong(item -> MapUtils.getLongValue(item, "count")))));
      mitreAlertCount.putAll(statisticsMitreAttack.stream()
          .filter(item -> StringUtils.isNotBlank(MapUtils.getString(item, "mitreTechniqueId")))
          .collect(Collectors.groupingBy(item -> MapUtils.getString(item, "mitreTechniqueId"),
              Collectors.summingLong(item -> MapUtils.getLongValue(item, "count")))));
    }

    return mitreAttacks.stream().map(mitreAttackDO -> {
      MitreAttackBO mitreAttackBO = new MitreAttackBO();
      BeanUtils.copyProperties(mitreAttackDO, mitreAttackBO);
      mitreAttackBO.setRuleSize(mitreUsedCount.getOrDefault(mitreAttackDO.getId(), 0));
      mitreAttackBO.setAlertSize(mitreAlertCount.getOrDefault(mitreAttackDO.getId(), 0L));

      return mitreAttackBO;
    }).collect(Collectors.toList());
  }
}
