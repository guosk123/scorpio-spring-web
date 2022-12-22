import GlobalTimeSelector, {
  ETimeType,
  ETimeUnit,
  getGlobalTime,
  globalTimeFormatText,
} from '@/components/GlobalTimeSelector';
import PageLoading from '@/components/PageLoading';
import type { IPageLayout } from '@/layouts/PageLayout';
import PageLayout from '@/layouts/PageLayout';
import type { ConnectState } from '@/models/connect';
import { Result, Space } from 'antd';
import classNames from 'classnames';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useLayoutEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
// import SearchTree from '../../components/SearchTree';
import type { IUriParams } from '../../typings';
import { EPcapState } from '../typing';
import type { IOfflinePcapData } from '../typing';
import styles from './index.less';

function timeFormatter(startTime: string | number, endTime: string | number, interval = 60) {
  // 相差的秒
  const diffSeconds = (moment(endTime).valueOf() - moment(startTime).valueOf()) / 1000;

  let nextStartTime = startTime;
  let nextEndTime = endTime;
  let nextInterval = interval;

  // 计算时间间隔
  // 时间间隔小于1小时，时间间隔为1分钟
  if (diffSeconds <= 1 * 60 * 60) {
    nextInterval = 60;
  }

  // 时间间隔小于1天，时间间隔为5分钟
  else if (diffSeconds <= 24 * 60 * 60) {
    nextInterval = 5 * 60;
  }

  // 时间间隔小于10天，时间间隔为1小时
  else {
    nextInterval = 60 * 60;
  }

  // 其他时间，时间间隔为1天
  // else {
  //   nextInterval = 24 * 60 * 60;
  // }

  // 最后格式化成 UTC 时间
  nextStartTime = moment(nextStartTime).format();
  nextEndTime = moment(nextEndTime).format();

  return {
    startTime: nextStartTime,
    endTime: nextEndTime,
    interval: nextInterval,
    totalSeconds: diffSeconds,
  };
}

interface IOfflineLayout extends IPageLayout {
  dispatch: Dispatch;
  currentPcpInfo: IOfflinePcapData;
  queryPcapInfoLoading: boolean | undefined;
}

const OfflineLayout: React.FC<IOfflineLayout> = ({
  dispatch,
  currentPcpInfo,
  // 默认设置为 true，防止空屏
  queryPcapInfoLoading,
  ...restProps
}) => {
  const [collapsed, setCollapsed] = useState<boolean>(true);
  const { pathname } = useLocation();
  const params: IUriParams = useParams();

  const isOfflineAnalysis = pathToRegexp(`(/embed)?/analysis/offline/:pcapFileId/(.*)`).test(
    `${pathname}/`,
  );

  // const handleTreeSelect = (selectedKeys: React.Key[]) => {
  //   if (selectedKeys.length === 0) {
  //     return;
  //   }
  //   history.push(pathname.replace(params.networkId!, selectedKeys[0] as string));
  // };
  const changeGlobalTime = (
    reset: boolean = false,
    timeObj?: {
      startTime: string;
      endTime: string;
      interval: number;
      totalSeconds: number;
    },
  ) => {
    if (reset) {
      // 重制时间为最近30mins
      dispatch({
        type: 'appModel/updateGlobalTime',
        payload: getGlobalTime({
          relative: true,
          type: ETimeType.RANGE,
          last: {
            unit: ETimeUnit.MINUTES,
            range: 30,
          },
        }),
      });
      return;
    }
    if (!timeObj) {
      return;
    }
    dispatch({
      type: 'appModel/updateGlobalTime',
      payload: getGlobalTime({
        relative: false,
        type: ETimeType.CUSTOM,
        custom: [
          moment(timeObj.startTime, globalTimeFormatText),
          moment(timeObj.endTime, globalTimeFormatText),
        ],
        isOrigin: true,
      }),
    });
  };

  const handleCollapsed = (nextCollapsed: boolean) => {
    setCollapsed(nextCollapsed);
  };

  // 记录是否获取到最新的离线文件信息，当前流程有缓存，且loading刚开始为false不能作为判断依据
  const [pcapInfoState, setPcapInfoState] = useState(false);

  useLayoutEffect(() => {
    dispatch({
      type: 'npmdModel/queryPcapInfo',
      payload: params.pcapFileId,
    }).then((res: boolean) => {
      setPcapInfoState(res);
    });
  }, [dispatch, params.pcapFileId]);

  const currentPcapFile = useMemo(() => {
    return currentPcpInfo;
  }, [currentPcpInfo]);

  const timeObj = useMemo(() => {
    const packetStartTime = moment(currentPcapFile.packetStartTime).set('second', 0);
    const packetEndTime = (() => {
      const endTime = moment(currentPcapFile.packetEndTime);
      return endTime.add(1, 'minute').set('second', 0);
    })();
    return timeFormatter(packetStartTime.format(), packetEndTime.format());
  }, [currentPcapFile.packetStartTime, currentPcapFile.packetEndTime]);

  useEffect(() => {
    changeGlobalTime(false, timeObj);
    return () => {
      changeGlobalTime(true);
    };
  }, [currentPcapFile.packetStartTime, currentPcapFile.packetEndTime]);

  if (queryPcapInfoLoading || !pcapInfoState) {
    return <PageLoading />;
  }

  if (!currentPcapFile.id) {
    return <Result status="warning" subTitle="离线文件不存在或已被删除" />;
  }
  if (
    currentPcapFile.status !== EPcapState.COMPLETE &&
    currentPcapFile.status !== EPcapState.CONTINUE
  ) {
    return <Result status="warning" subTitle="离线文件分析未完成" />;
  }

  if (!isOfflineAnalysis) {
    return (
      <>
        <PageLayout {...restProps} />
      </>
    );
  }

  return (
    <div className={classNames([styles.layoutWrap, collapsed && styles.collapsed])}>
      {/* <div className={styles.leftWrap}>
        <SearchTree
          data={[]}
          selectedKeys={[params.networkId!]}
          onSelect={handleTreeSelect}
          collapsed={collapsed}
          onToggleCollapsed={handleCollapsed}
        />
      </div> */}
      <div className={styles.contentWrap}>
        <div style={{ display: 'flex' }}>
          <Space size="middle">
            <span className={styles.name}>{currentPcapFile?.name || params.pcapFileId}</span>
          </Space>
          <GlobalTimeSelector
            limit={[moment(timeObj.startTime).valueOf(), moment(timeObj.endTime).valueOf()]}
            history={false}
          />
        </div>
        <PageLayout {...restProps} />
      </div>
    </div>
  );
};

const mapStateToProps = ({
  npmdModel: { currentPcpInfo },

  loading: { effects },
}: ConnectState) => ({
  currentPcpInfo,
  queryPcapInfoLoading: effects['npmdModel/queryPcapInfo'],
});
export default connect(mapStateToProps)(OfflineLayout);
