package com.machloop.fpc.baseline.mission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.baseline.calculate.CalculateResult;
import com.machloop.fpc.baseline.calculate.service.CalculateService;
import com.machloop.fpc.baseline.publish.PublishDao;
import com.machloop.fpc.npm.appliance.data.BaselineValueDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
@Service
public class MissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MissionService.class);

  private static final Map<String,
      Mission> hourMissions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private static final Map<String,
      Mission> minuteMissions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private static final Map<String,
      Mission> fiveMinuteMissions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private static final Map<String,
      Mission> retryMissions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
  private static final Map<String,
      Integer> retryCountMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  @Value("${task.mission.retry.max}")
  private int maxMissionRetry;

  @Autowired
  private CalculateService calculateService;

  @Autowired
  private PublishDao publishDao;

  public void executeHourMissions() throws InterruptedException {
    Collection<Mission> missions = hourMissions.values();
    if (missions.isEmpty()) {
      return;
    }
    Collection<Mission> missionList = filterMissions(missions);
    int total = missionList.size();
    int success = executeMissions(missionList);

    LOGGER.info("finish execute hour missions, total: [{}], success: [{}]", total, success);
  }

  public void executeMinuteMissions() throws InterruptedException {
    Collection<Mission> missions = minuteMissions.values();
    if (missions.isEmpty()) {
      return;
    }
    Collection<Mission> missionList = filterMissions(missions);
    int total = missionList.size();
    int success = executeMissions(missionList);

    LOGGER.info("finish execute minute missions, total: [{}], success: [{}]", total, success);
  }

  public void executeFiveMinuteMissions() throws InterruptedException {
    Collection<Mission> missions = fiveMinuteMissions.values();
    if (missions.isEmpty()) {
      return;
    }
    Collection<Mission> missionList = filterMissions(missions);
    int total = missionList.size();
    int success = executeMissions(missionList);

    LOGGER.info("finish execute five minute missions, total: [{}], success: [{}]", total, success);
  }

  public void retryFailedMissions() throws InterruptedException {
    Collection<Mission> missions = retryMissions.values();
    if (missions.isEmpty()) {
      return;
    }
    Collection<Mission> missionList = filterMissions(missions);
    int total = missionList.size();
    int success = executeMissions(missionList);

    LOGGER.info("finish retry failed missions, total: [{}], success: [{}]", total, success);
  }

  public int executeMissions(Collection<Mission> missions) throws InterruptedException {

    List<CalculateResult> results = calculateService.executeCalculate(missions);

    List<CalculateResult> successMissionResults = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    results.forEach(result -> {
      if (!result.isSuccess()) {
        // 任务执行失败，稍后重试
        String missionId = result.getMissionId();
        Mission failedMission = minuteMissions.get(missionId);
        failedMission = failedMission == null ? fiveMinuteMissions.get(missionId) : failedMission;
        failedMission = failedMission == null ? hourMissions.get(missionId) : failedMission;
        if (failedMission != null) {
          int currentRetryCount = retryCountMap.getOrDefault(missionId, 0);
          if (currentRetryCount < maxMissionRetry) {
            // 未超过最大重试次数
            retryMissions.put(missionId, failedMission);
            retryCountMap.put(missionId, currentRetryCount + 1);
            LOGGER.info(
                "failed to execute mission: [{}], plan to retry later, current retry count: [{}].",
                missionId, currentRetryCount);
          } else {
            // 超过最大重试次数
            retryMissions.remove(missionId);
            retryCountMap.remove(missionId);
            LOGGER.info("failed to execute mission: [{}], exceed max retry count: [{}].", missionId,
                maxMissionRetry);
          }
        }
      } else {
        successMissionResults.add(result);

        // 失败重试任务执行成功后移除重试集合
        retryMissions.remove(result.getMissionId());
        retryCountMap.remove(result.getMissionId());
      }
    });

    if (CollectionUtils.isNotEmpty(successMissionResults)) {
      List<BaselineValueDO> baselineDOList = successMissionResults.stream().map(result -> {
        BaselineValueDO baseline = new BaselineValueDO();
        BeanUtils.copyProperties(result, baseline);
        return baseline;
      }).collect(Collectors.toList());
      publishDao.publish(baselineDOList);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("success mission result: [{}]", JsonHelper.serialize(successMissionResults));
    }

    return successMissionResults.size();
  }

  public void addMission(Mission mission) {
    LOGGER.info("new mission created, mission id: [{}], execute every {}", mission.getId(),
        mission.getWindow().getPeriod());

    switch (mission.getWindow().getPeriod()) {
      case Constants.ONE_MINUTE_SECONDS:
        minuteMissions.put(mission.getId(), mission);
        break;
      case Constants.FIVE_MINUTE_SECONDS:
        fiveMinuteMissions.put(mission.getId(), mission);
        break;
      case Constants.ONE_HOUR_SECONDS:
        hourMissions.put(mission.getId(), mission);
        break;
      default:
        throw new IllegalArgumentException("unsupport window period");
    }
  }

  public void removeMission(String id) {
    hourMissions.remove(id);
    minuteMissions.remove(id);
    fiveMinuteMissions.remove(id);
  }

  private Collection<Mission> filterMissions(Collection<Mission> missions) {
    for (Mission mission : missions) {
      String customTime = mission.getCustomTime();
      // customTime不为空才是自定义时间基线告警
      if (StringUtils.isNotBlank(customTime)) {
        List<Map<String, String>> deserialize = JsonHelper.deserialize(customTime,
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        List<String> timeList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
        for (Map<String, String> map : deserialize) {
          List<String> values = new ArrayList<String>(map.values());
          timeList.addAll(values);
        }
        Date currentTime = new Date();
        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < timeList.size(); i = i + 2) {
          // 将当前的年月日日期与自定义时间拼起来
          String dateStringLeft = ymd.format(currentTime).toString() + " " + timeList.get(i);
          String dateStringRight = ymd.format(currentTime).toString() + " " + timeList.get(i + 1);
          try {
            long dateLeft = formatter.parse(dateStringLeft).getTime();
            // 防止最后一个时间点无法取到，这里将结束时间延后1分钟
            long dateRight = formatter.parse(dateStringRight).getTime() + Constants.NUM_1000;
            long now = currentTime.getTime();
            if (now >= dateLeft && now <= dateRight) {
              // 如果当前时间满足自定义时间，则跳出循环，表示此时该任务可以执行
              break;
            }
            if (i >= timeList.size() - 2) {
              // 当执行到最后一个自定义时间时仍然不满足，则移除该任务
              missions.remove(mission);
              break;
            }
          } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    return missions;
  }

}
