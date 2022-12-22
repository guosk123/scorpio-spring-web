import TimeRangeSlider from '@/components/TimeRangeSlider';
import styles from './index.less';
import Loading from '@/components/Loading';
import type { AppModelState } from '@/models/app/index';
import type { INetflowModel } from '../../model';
import type { ISearchParam } from '../../utils/converter';
import type { Dispatch } from 'umi';
import { searchConverter } from '../../utils/converter';
import { Search } from '../../service';
import { connect, history, useParams } from 'umi';
import { Space, TreeSelect } from 'antd';
import React, { useEffect, useState, useMemo } from 'react';

// 模糊搜索树状结构定义接口
interface ITreeSelectData {
  value: string;
  children?: any[];
  label: string;
  key?: string;
}

// 当前选择的源定义接口
export interface ICurrentSource {
  deviceName: string;
  deviceAlias: string;
  netifNo?: string;
  netifAlias?: string;
  netifSpeed?: number;
}

// 组件参数接口
interface IFlowWrapper extends AppModelState {
  location: { pathname: string };
  dispatch: Dispatch;
}

const FlowWrapper: React.FC<IFlowWrapper> = ({ dispatch, location: { pathname }, children }) => {
  // 路由参数
  const urlParams = useParams<{ deviceName: string; netifNo: string }>();

  // 模糊搜索列表
  const [darkSearchList, setDarkSearchList] = useState<ITreeSelectData[]>();

  // 树状结构选择值
  const [treeSelectValue, setTreeSelectValue] = useState<string>();

  // 当前源信息
  const [currentSource, setCurrentSource] = useState<ICurrentSource>();

  // 更新源信息，页面跳转
  function updateSourceUrl(deviceName: string, netifNo?: string) {
    let dstUrl = pathname;
    if (!pathname.includes('device')) {
      dstUrl = `${pathname}'/device/'${deviceName}`;
    } else {
      const pathList = pathname.split('/');
      const index = pathList.indexOf('device');
      dstUrl = `${pathList.slice(0, index + 1).join('/')}/${deviceName}/${pathList
        .slice(index + 2)
        .join('/')}`;
    }
    if (netifNo !== undefined && netifNo !== '') {
      if (dstUrl.includes('netif')) {
        const pathList = dstUrl.split('/');
        const index = pathList.indexOf('netif');
        dstUrl = `${pathList.slice(0, index + 1).join('/')}/${netifNo}/${pathList
          .slice(index + 2)
          .join('/')}`;
      } else {
        const pathList = dstUrl.split('/');
        const index = pathList.indexOf('device');
        dstUrl = `${pathList.slice(0, index + 2).join('/')}/netif/${netifNo}/${pathList
          .slice(index + 2)
          .join('/')}`;
      }
    }
    if (netifNo === undefined || netifNo === '') {
      if (dstUrl.includes('netif')) {
        const pathList = dstUrl.split('/');
        const index = pathList.indexOf('netif');
        dstUrl = `${pathList.slice(0, index).join('/')}/${pathList.slice(index + 2).join('/')}`;
      }
    }
    history.push(dstUrl);
  }

  // 模糊搜索选择回调函数
  function changeSource(value: string) {
    if (!value) {
      return;
    }
    setTreeSelectValue(value);
    const [deviceName, netifNo] = value.split('_');
    updateSourceUrl(deviceName, netifNo);
  }

  // 根据用户输入，动态获取数据
  function fetchUserList(keywords: string) {
    return Search({ keywords }).then((res) => {
      if (!res.success) {
        return;
      }
      setDarkSearchList(searchConverter(res.result));
    });
  }

  // 当前选择的源信息
  const sourceTitle = useMemo(() => {
    if (currentSource) {
      let title = '';
      if (currentSource?.deviceName) {
        if (currentSource.deviceAlias) {
          title += `${currentSource.deviceAlias}`;
        } else {
          title += `${currentSource.deviceName}`;
        }
      }
      if (currentSource?.netifNo) {
        if (currentSource.netifAlias) {
          title += ` - ${currentSource.netifAlias}`;
        } else {
          title += ` - 接口${currentSource.netifNo}`;
        }
      }
      return title;
    }
    return '';
  }, [currentSource]);

  // 在源变动时，查找详细信息[别名，速率]
  useEffect(() => {
    Search({ keywords: urlParams.deviceName }).then((res) => {
      if (!res.success) {
        return;
      }
      const sources: ISearchParam[] = res.result;
      const sourceInfo = (() => {
        if (sources) {
          for (let index = 0; index < sources.length; index += 1) {
            const source = sources[index];
            if (
              source.deviceName === urlParams.deviceName &&
              ((urlParams.netifNo && source.netifNo === urlParams.netifNo) ||
                urlParams.netifNo === undefined)
            ) {
              return {
                deviceName: source.deviceName,
                deviceAlias: (() => {
                  if (source.netifNo !== '') {
                    return (
                      sources.find((item) => item.deviceName === source.deviceName && !item.netifNo)
                        ?.alias || ''
                    );
                  }
                  return source.alias;
                })(),
                netifNo: urlParams.netifNo ? source.netifNo : undefined,
                netifAlias: urlParams.netifNo ? source.alias : undefined,
                netifSpeed: source.netifSpeed !== undefined ? source.netifSpeed : undefined,
              };
            }
          }
        }
        return undefined;
      })();
      dispatch({
        type: 'netflowModel/setSelectedNetifSpeed',
        payload: sourceInfo?.netifNo === undefined ? undefined : sourceInfo?.netifSpeed,
      });
      if (sourceInfo) {
        setCurrentSource(sourceInfo);
      }
    });
  }, [dispatch, urlParams, urlParams.deviceName, urlParams.netifNo]);

  return (
    <div className={styles.contentWrap}>
      <Space style={{ marginBottom: 10 }} size="middle">
        {!currentSource ? (
          <Loading height={2} style={{ paddingBottom: 10 }} />
        ) : (
          <h3 key={pathname} className={styles.contentWrap__title}>
            {sourceTitle}
          </h3>
        )}
        <TreeSelect
          showSearch
          style={{ width: 200 }}
          dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
          placeholder="设备搜索"
          treeData={darkSearchList}
          onSearch={fetchUserList}
          onSelect={changeSource}
          value={treeSelectValue}
          filterTreeNode={false}
          treeDefaultExpandAll={true}
          onFocus={() => {
            fetchUserList('');
          }}
        />
        <TimeRangeSlider />
      </Space>
      {children}
    </div>
  );
};

export default connect(({ appModel: { globalSelectedTime } }: INetflowModel) => ({
  globalSelectedTime,
}))(FlowWrapper);
