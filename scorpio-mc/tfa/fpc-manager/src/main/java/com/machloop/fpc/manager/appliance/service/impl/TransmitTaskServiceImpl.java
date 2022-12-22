package com.machloop.fpc.manager.appliance.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.algorithm.bpf.BpfCheck;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.TransmitTaskBO;
import com.machloop.fpc.manager.appliance.dao.TransmitTaskDao;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.appliance.service.TransmitTaskService;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO;
import com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Service
public class TransmitTaskServiceImpl implements TransmitTaskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransmitTaskServiceImpl.class);

  @Autowired
  private TransmitTaskDao transmitTaskDao;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private PacketAnalysisSubTaskDao packetAnalysisSubTaskDao;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#queryTransmitTasks(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO)
   */
  @Override
  public Page<TransmitTaskBO> queryTransmitTasks(Pageable page, TransmitTaskQueryVO queryVO) {

    Map<String, String> modeDict = dictManager.getBaseDict().getItemMap("appliance_transmit_mode");
    Map<String,
        String> rateUnitDict = dictManager.getBaseDict().getItemMap("appliance_transmit_rateunit");
    Map<String, String> filterTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_filter_type");
    Map<String, String> networkMap = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName));
    networkMap.putAll(logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getName)));

    Page<TransmitTaskDO> transmitTaskDOPage = transmitTaskDao.queryTransmitTasks(page, queryVO);
    long totalElem = transmitTaskDOPage.getTotalElements();

    List<TransmitTaskBO> transmitTaskBOList = Lists
        .newArrayListWithCapacity(transmitTaskDOPage.getSize());
    for (TransmitTaskDO transmitTaskDO : transmitTaskDOPage) {
      TransmitTaskBO transmitTaskBO = new TransmitTaskBO();
      BeanUtils.copyProperties(transmitTaskDO, transmitTaskBO);

      transmitTaskBO.setFilterNetworkName(networkMap
          .getOrDefault(transmitTaskDO.getFilterNetworkId(), transmitTaskDO.getFilterNetworkId()));
      if (StringUtils.isNotBlank(transmitTaskDO.getFilterPacketFileId())) {
        PacketAnalysisSubTaskDO packetAnalysisSubTaskDO = packetAnalysisSubTaskDao
            .queryPacketAnalysisSubTask(transmitTaskDO.getFilterPacketFileId());
        transmitTaskBO.setFilterPacketFileName(packetAnalysisSubTaskDO.getName());
      }

      transmitTaskBO.setModeText(MapUtils.getString(modeDict, transmitTaskBO.getMode(), ""));
      transmitTaskBO.setReplayRateUnitText(
          MapUtils.getString(rateUnitDict, transmitTaskBO.getReplayRateUnit(), ""));
      transmitTaskBO.setFilterConditionTypeText(
          MapUtils.getString(filterTypeDict, transmitTaskBO.getFilterConditionType(), ""));

      transmitTaskBO
          .setFilterStartTime(DateUtils.toStringISO8601(transmitTaskDO.getFilterStartTime()));
      transmitTaskBO.setFilterEndTime(DateUtils.toStringISO8601(transmitTaskDO.getFilterEndTime()));

      transmitTaskBO
          .setExecutionStartTime(DateUtils.toStringISO8601(transmitTaskDO.getExecutionStartTime()));
      transmitTaskBO
          .setExecutionEndTime(DateUtils.toStringISO8601(transmitTaskDO.getExecutionEndTime()));

      transmitTaskBOList.add(transmitTaskBO);
    }

    return new PageImpl<>(transmitTaskBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#queryTransmitTask(java.lang.String)
   */
  @Override
  public TransmitTaskBO queryTransmitTask(String id) {
    TransmitTaskBO transmitTaskBO = new TransmitTaskBO();

    TransmitTaskDO transmitTaskDO = transmitTaskDao.queryTransmitTask(id);
    BeanUtils.copyProperties(transmitTaskDO, transmitTaskBO);

    Map<String, String> modeDict = dictManager.getBaseDict().getItemMap("appliance_transmit_mode");
    transmitTaskBO.setModeText(MapUtils.getString(modeDict, transmitTaskBO.getMode(), ""));

    Map<String,
        String> rateUnitDict = dictManager.getBaseDict().getItemMap("appliance_transmit_rateunit");
    transmitTaskBO.setReplayRateUnitText(
        MapUtils.getString(rateUnitDict, transmitTaskBO.getReplayRateUnit(), ""));

    Map<String, String> forwardActionDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_forward_policy");
    transmitTaskBO.setForwardActionText(
        MapUtils.getString(forwardActionDict, transmitTaskBO.getForwardAction(), ""));

    Map<String, String> filterTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_filter_type");
    transmitTaskBO.setFilterConditionTypeText(
        MapUtils.getString(filterTypeDict, transmitTaskBO.getFilterConditionType(), ""));

    NetworkDO networkDO = networkDao.queryNetwork(transmitTaskDO.getFilterNetworkId());
    transmitTaskBO.setFilterNetworkName(
        StringUtils.defaultIfBlank(networkDO.getName(), transmitTaskDO.getFilterNetworkId()));
    PacketAnalysisSubTaskDO packetAnalysisSubTaskDO = packetAnalysisSubTaskDao
        .queryPacketAnalysisSubTask(transmitTaskDO.getFilterPacketFileId());
    transmitTaskBO.setFilterPacketFileName(packetAnalysisSubTaskDO.getName());

    transmitTaskBO
        .setFilterStartTime(DateUtils.toStringISO8601(transmitTaskDO.getFilterStartTime()));
    transmitTaskBO.setFilterEndTime(DateUtils.toStringISO8601(transmitTaskDO.getFilterEndTime()));

    transmitTaskBO
        .setExecutionStartTime(DateUtils.toStringISO8601(transmitTaskDO.getExecutionStartTime()));
    transmitTaskBO
        .setExecutionEndTime(DateUtils.toStringISO8601(transmitTaskDO.getExecutionEndTime()));

    return transmitTaskBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#saveTransmitTask(com.machloop.fpc.manager.appliance.bo.TransmitTaskBO, java.lang.String, java.lang.String)
   */
  @Override
  public TransmitTaskBO saveTransmitTask(TransmitTaskBO transmitTaskBO, String operatorId,
      String source) {
    TransmitTaskDO existName = transmitTaskDao.queryTransmitTaskByName(transmitTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "任务名称已经存在");
    }

    TransmitTaskDO transmitTaskDO = new TransmitTaskDO();
    BeanUtils.copyProperties(transmitTaskBO, transmitTaskDO);
    transmitTaskDO.setOperatorId(operatorId);
    transmitTaskDO.setSource(source);
    transmitTaskDO
        .setFilterStartTime(DateUtils.parseISO8601Date(transmitTaskBO.getFilterStartTime()));
    transmitTaskDO.setFilterEndTime(DateUtils.parseISO8601Date(transmitTaskBO.getFilterEndTime()));
    transmitTaskDO
        .setFilterNetworkId(StringUtils.defaultIfBlank(transmitTaskBO.getFilterNetworkId(), ""));
    transmitTaskDO.setFilterPacketFileId(
        StringUtils.defaultIfBlank(transmitTaskBO.getFilterPacketFileId(), ""));
    if (StringUtils.isNotBlank(transmitTaskDO.getFilterBpf())
        && StringUtils.equals(transmitTaskDO.getFilterTuple(), "[]")) {
      transmitTaskDO.setFilterConditionType(FpcConstants.TRANSMIT_TASK_FILTER_TYPE_BPF);
    } else if (!StringUtils.equals(transmitTaskDO.getFilterTuple(), "[]")
        && StringUtils.isBlank(transmitTaskDO.getFilterBpf())) {
      transmitTaskDO.setFilterConditionType(FpcConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE);
    } else {
      transmitTaskDO.setFilterConditionType(FpcConstants.TRANSMIT_TASK_FILTER_TYPE_MIX);
    }

    // 模式非重放时，重放相关字段置空
    if (!StringUtils.equals(transmitTaskDO.getMode(), FpcConstants.TRANSMIT_TASK_MODE_REPLAY)) {
      transmitTaskDO.setReplayRateUnit("0");
      transmitTaskDO.setReplayNetif("");
      transmitTaskDO.setReplayRate(0);
      transmitTaskDO.setReplayRule("");
      transmitTaskDO.setForwardAction("0");
      transmitTaskDO.setIpTunnel("");
    }
    transmitTaskDO.setAssignTaskId("");

    // 检查BPF表达式
    if (StringUtils.isNotBlank(transmitTaskDO.getFilterBpf())
        && !BpfCheck.isBpfValid(transmitTaskDO.getFilterBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    // 写入数据库
    transmitTaskDao.saveTransmitTask(transmitTaskDO);

    return queryTransmitTask(transmitTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#updateTransmitTask(com.machloop.fpc.manager.appliance.bo.TransmitTaskBO, java.lang.String, java.lang.String)
   */
  @Override
  public TransmitTaskBO updateTransmitTask(TransmitTaskBO transmitTaskBO, String taskId,
      String operatorId) {
    TransmitTaskBO oldTask = queryTransmitTask(taskId);
    if (StringUtils.isBlank(oldTask.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "任务不存在");
    }

    TransmitTaskDO existName = transmitTaskDao.queryTransmitTaskByName(transmitTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())
        && !StringUtils.equals(taskId, existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "任务名称已经存在");
    }

    // 写入数据库
    TransmitTaskDO transmitTaskDO = new TransmitTaskDO();
    BeanUtils.copyProperties(oldTask, transmitTaskDO);
    transmitTaskDO.setName(transmitTaskBO.getName());
    transmitTaskDO.setFilterTuple(transmitTaskBO.getFilterTuple());
    transmitTaskDO.setFilterRaw(transmitTaskBO.getFilterRaw());
    transmitTaskDO.setDescription(transmitTaskBO.getDescription());
    transmitTaskDO.setOperatorId(operatorId);
    transmitTaskDO.setFilterPacketFileId(
        StringUtils.defaultIfBlank(transmitTaskBO.getFilterPacketFileId(), ""));
    if (StringUtils.equals(FpcConstants.TRANSMIT_TASK_MODE_REPLAY, transmitTaskDO.getMode())) {
      transmitTaskDO.setReplayNetif(transmitTaskBO.getReplayNetif());
      transmitTaskDO.setReplayRate(transmitTaskBO.getReplayRate());
      transmitTaskDO.setReplayRateUnit(transmitTaskBO.getReplayRateUnit());
      transmitTaskDO.setReplayRule(transmitTaskBO.getReplayRule());
      transmitTaskDO.setIpTunnel(transmitTaskBO.getIpTunnel());
    }

    transmitTaskDao.updateTransmitTask(transmitTaskDO);

    return queryTransmitTask(transmitTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#redoTransmitTask(java.lang.String)
   */
  @Override
  public TransmitTaskBO redoTransmitTask(String id) {
    TransmitTaskBO transmitTaskBO = queryTransmitTask(id);
    if (StringUtils.isNotBlank(transmitTaskBO.getId())) {

      // 重置任务执行状态
      transmitTaskDao.redoTransmitTask(id);
    }

    return transmitTaskBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#stopTransmitTask(java.lang.String)
   */
  @Override
  public TransmitTaskBO stopTransmitTask(String id) {
    TransmitTaskBO transmitTaskBO = queryTransmitTask(id);
    if (StringUtils.isNotBlank(transmitTaskBO.getId())) {

      // 停止任务
      transmitTaskDao.stopTransmitTask(id);
    }
    return transmitTaskBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.TransmitTaskService#deleteTransmitTask(java.lang.String, java.lang.String)
   */
  @Override
  public TransmitTaskBO deleteTransmitTask(String id, String operatorId) {
    TransmitTaskBO transmitTaskBO = queryTransmitTask(id);
    if (StringUtils.isNotBlank(transmitTaskBO.getId())) {

      // 删除任务缓存文件
      String filePath = transmitTaskBO.getExecutionCachePath();
      if (StringUtils.isNotBlank(filePath)) {

        try {
          boolean result = Files.deleteIfExists(Paths.get(filePath));
          if (!result) {
            LOGGER.warn("File not exsit when delete transmit task.");
          }
        } catch (IOException e) {
          LOGGER.warn("Fail to delete transmit task.", e);
        }
      }

      // 重置任务执行状态
      transmitTaskDao.deleteTransmitTask(id, operatorId);
    }

    return transmitTaskBO;
  }

}
