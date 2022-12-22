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
import { timeFormatter } from '@/utils/utils';
import { Result, Space } from 'antd';
import classNames from 'classnames';
import moment from 'moment';
import pathToRegexp from 'path-to-regexp';
import React, { useEffect, useLayoutEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, history, useLocation, useParams } from 'umi';
import SearchTree from '../../components/SearchTree';
import type { IUriParams } from '../../typings';
import { EPcapState } from '../../typings';
import type { IOfflinePcapData } from '../typing';
import styles from './index.less';

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

  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length === 0) {
      return;
    }
    history.push(pathname.replace(params.networkId!, selectedKeys[0] as string));
  };
  const changeGlobalTime = (reset: boolean = false, from?: number, to?: number) => {
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
    if (!from || !to) {
      return;
    }
    let timeObj;
    if ((to - from) / 1000 < 120) {
      const diffSeconds = 120 - (to - from) / 1000;
      const offset = diffSeconds / 2;
      timeObj = timeFormatter(from - offset * 1000, to + offset * 1000);
    } else {
      timeObj = timeFormatter(from, to);
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

  useEffect(() => {
    changeGlobalTime(
      false,
      moment(currentPcpInfo.packetStartTime).valueOf(),
      moment(currentPcpInfo.packetEndTime).valueOf(),
    );
    return () => {
      changeGlobalTime(true);
    };
  }, []);

  const currentPcapFile = useMemo(() => {
    return currentPcpInfo;
  }, [currentPcpInfo]);

  if (queryPcapInfoLoading || !pcapInfoState) {
    return <PageLoading />;
  }

  if (!currentPcapFile.id) {
    return <Result status="warning" subTitle="离线文件不存在或已被删除" />;
  }
  if (currentPcapFile.status !== EPcapState.COMPLETE) {
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
      <div className={styles.leftWrap}>
        <SearchTree
          data={[]}
          selectedKeys={[params.networkId || '']}
          onSelect={handleTreeSelect}
          collapsed={collapsed}
          onToggleCollapsed={handleCollapsed}
        />
      </div>
      <div className={styles.contentWrap}>
        <div style={{ display: 'flex' }}>
          <Space size="middle">
            <span className={styles.name}>{currentPcapFile?.name || params.pcapFileId}</span>
          </Space>
          <GlobalTimeSelector
            limit={[
              moment(currentPcpInfo.packetStartTime).valueOf(),
              moment(currentPcpInfo.packetEndTime).valueOf(),
            ]}
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
