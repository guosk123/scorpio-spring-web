package com.machloop.fpc.manager.asset.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.data.GlobalSettingDO;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.asset.dao.AssetInformationDao;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月9日, fpc-manager
 */
@Component
public class AssetDropPartitionTask {

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private AssetInformationDao assetInformationDao;

  public static final Logger LOGGER = LoggerFactory.getLogger(AssetDropPartitionTask.class);

  @Scheduled(cron = "${task.system.asset.drop.partition.cron}")
  public void run() throws ParseException {

    GlobalSettingDO assetUsefulLife = globalSettingService
        .getValues(ManagerConstants.ASSET_USEFUL_LIFE).get(0);
    if (StringUtils.isBlank(assetUsefulLife.getSettingValue())) {
      return;
    }

    int configTime = Integer.parseInt(assetUsefulLife.getSettingValue());

    // 将asset.useful.life的更新时间转换为0时0分0秒
    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    long updateTime = dayFormat.parse(dayFormat.format(assetUsefulLife.getUpdateTime())).getTime();

    // 取当前时间与更新时间计算时间差
    long now = dayFormat.parse(dayFormat.format(DateUtils.now())).getTime();
    double timeInterval = ((double) now - (double) updateTime)
        / (double) (Constants.ONE_DAY_SECONDS * 1000);

    // 时间差大于配置时间执行删除分区
    if ((int) timeInterval >= configTime) {
      String partitionName = DateUtils
          .toStringYYYYMMDD(DateUtils.beforeDayDate(assetUsefulLife.getUpdateTime(), configTime))
          .replace("-", "");
      assetInformationDao.dropExpiredData(partitionName);
      globalSettingService.setValue(ManagerConstants.ASSET_USEFUL_LIFE,
          assetUsefulLife.getSettingValue());
    }
  }
}
