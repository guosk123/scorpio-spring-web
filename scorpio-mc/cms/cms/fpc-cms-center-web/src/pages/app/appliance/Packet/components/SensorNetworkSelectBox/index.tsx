import type { SensorItem } from '@/pages/app/Configuration/equipment/Sensor/List';
import { querySensorList } from '@/pages/app/Configuration/equipment/service';
import { getNetworkType, NetworkTypeContext } from '@/pages/app/Network/Analysis';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { ENetowrkType } from '@/pages/app/Network/typing';
import { Select, Card, Button, Divider, Result, message } from 'antd';
import { ReactElement, useCallback } from 'react';
import { Fragment } from 'react';
import { useMemo } from 'react';
import React from 'react';
// import { useMemo } from 'react';
import { useContext } from 'react';
import { useEffect, useState } from 'react';
import { useParams, history } from 'umi';
import { queryAnalyzableObject } from '../../service';
import { ServiceContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { queryNetWorkTree } from '@/pages/app/Network/service';
import type { URLFilter } from '@/pages/app/Network/Analysis/constant';

const { Option } = Select;

interface Props {
  children: ReactElement;
  initSelect?: { fpcSerialNumber: string; networkId: string };
  disabled?: boolean;
  drilldownNetworkId?: string;
  onNoFpcSensor?: any;
}

export enum E_PKT_DEF_SHOW_TYPE {
  DEF = 'def',
  NO_NETWORK = 'noNetwork',
  ERROR = 'error',
}

export const packetShowPage = {
  [E_PKT_DEF_SHOW_TYPE.DEF]: (
    children: React.ReactElement,
    submitInfo: any,
    setNetworkValue: any,
    // 现在选择的探针与其对应的探针网络的信息
    sensorNetworkList: any,
  ) => {
    return React.cloneElement(children, {
      submitInfo: submitInfo,
      changeNetworkId: setNetworkValue,
      sensorNetworkList: sensorNetworkList,
    });
  },
  [E_PKT_DEF_SHOW_TYPE.NO_NETWORK]: () => {
    return (
      <Result
        title="未找到网络"
        extra={
          <Button
            type="primary"
            onClick={() => {
              window.location.reload();
            }}
          >
            刷新
          </Button>
        }
      />
    );
  },
  [E_PKT_DEF_SHOW_TYPE.ERROR]: () => {
    return (
      <Result
        title="获取失败"
        extra={
          <Button
            type="primary"
            onClick={() => {
              window.location.reload();
            }}
          >
            刷新
          </Button>
        }
      />
    );
  },
};

export default function SensorNetworkSelectBox(props: Props) {
  const { children, initSelect, disabled = false, drilldownNetworkId, onNoFpcSensor } = props;
  const { serviceId, networkId } = useParams<IUriParams>();
  const URLFilter = useMemo(() => {
    let filterNetworkId;
    let filterServiceId;
    JSON.parse(String(history.location.query?.filter || '[]')).forEach((item: URLFilter) => {
      ['network_id', 'service_id'].includes(item.field);
      if ('network_id' === item.field) {
        filterNetworkId = item.operand;
      }
      if ('service_id' === item.field) {
        filterServiceId = item.operand;
      }
    });
    return { networkId: filterNetworkId, serviceId: filterServiceId };
  }, []);
  const getUrlParams = () => {
    const tmpNetworkId = URLFilter.networkId || drilldownNetworkId || networkId || undefined;
    const tmpServiceId = URLFilter.serviceId || serviceId;
    if (tmpNetworkId?.includes('^')) {
      return [tmpServiceId, tmpNetworkId.split('^')[1]];
    }
    return [tmpServiceId, tmpNetworkId];
  };

  const [networkType, networkDataSetContext] = useContext<[ENetowrkType, INetworkTreeItem[]] | any>(
    serviceId ? ServiceContext : NetworkTypeContext,
  );

  const [networkDataSet, setNetworkDataSet] = useState<any>([]);
  const [networkDataSetLoading, setNetworkDataSetLoading] = useState<boolean>(true);

  useEffect(() => {
    setNetworkDataSetLoading(true);
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSetLoading(false);
        // console.log(result, 'networkDataSet');
        setNetworkDataSet(result);
      })
      .catch((err) => {
        message.error(err);
      });
  }, []);

  // 用来记录选中的探针、网络
  // const [sensorAndNetwork, setSensorAndNetwork] = useState({
  //   fpcSerialNumber: '',
  //   networkId: '',
  // });
  // 提供给packet页
  const [submitInfo, setsubmitInfo] = useState({
    fpcSerialNumber: initSelect?.fpcSerialNumber,
    networkId: initSelect?.networkId,
  });
  // 探针、网络tree
  const [sensorNetworkTree, setSensorNetworkTree] = useState();
  // 探针列表详情
  const [sensorListDetail, setSensorListDetail] = useState<SensorItem[]>([]);

  // 用来记录探针
  const [sensorList, setSensorList] = useState<any>([]);
  // 用来记录网络
  const [sensorNetworkList, setSensorNetworkList] = useState<any>({});
  useEffect(() => {
    querySensorList().then((res) => {
      const { success, result } = res;
      if (success) {
        // console.log(sensorNetworkList, 'sensorNetworkList');
        setSensorListDetail(result);
      }
    });
  }, []);

  const [sensorInfoLoading, setSensorInfoLoading] = useState(true);

  // 组合探针、探针网络数据
  useEffect(() => {
    const tmpSensorNetworkTree = sensorNetworkTree || {};
    if (Object.keys(tmpSensorNetworkTree).length && sensorListDetail.length) {
      const tmpSensorList: any = [];
      Object.keys(tmpSensorNetworkTree).forEach((item) => {
        const tmp = sensorListDetail.find((ele) => ele.serialNumber === item);
        if (tmp) {
          tmpSensorList.push({ title: tmp.name, value: tmp.serialNumber, key: tmp.serialNumber });
        }
      });
      // console.log(tmpSensorList, 'tmpSensorList');
      setSensorList(tmpSensorList);

      const tmpSensorNetworkList: any = {};

      Object.keys(tmpSensorNetworkTree).forEach((item) => {
        const tmpList: (INetworkTreeItem | undefined)[] = [];
        tmpSensorNetworkTree[item].forEach((ele: string) => {
          const tmp = networkDataSet.find((sub: any) => sub.recordId === ele);
          if (tmp) {
            tmpList.push(tmp);
          }
        });
        tmpSensorNetworkList[item] = tmpList;
      });
      // console.log(tmpSensorNetworkList, 'tmpSensorNetworkList');
      setSensorNetworkList(tmpSensorNetworkList);
    }
  }, [networkDataSet, sensorListDetail, sensorNetworkTree]);

  //获取networktree结构参数
  const queryNetworkTreeParams = useMemo(() => {
    const [tmpServiceId, tmpNetworkId] = getUrlParams();
    const currentNetworkType = getNetworkType(networkDataSet, tmpNetworkId || '');
    console.log(currentNetworkType, 'currentNetworkType');
    let tmpPayload: any = {
      // sourceType: tmpServiceId ? 'service' : networkType,
      // networkGroupId: networkType === ENetowrkType.NETWORK_GROUP ? tmpNetworkId : undefined,
      // networkId: networkType === ENetowrkType.NETWORK ? tmpNetworkId : undefined,
      // serviceId: tmpServiceId,
    };
    let currentSourceType = networkType;
    let isNetworkGroupId = false;
    let isNetworkId = false;
    if (currentNetworkType === ENetowrkType.NETWORK_GROUP) {
      currentSourceType = ENetowrkType.NETWORK_GROUP;
      isNetworkGroupId = true;
      isNetworkId = false;
    }
    if (currentNetworkType === ENetowrkType.NETWORK) {
      currentSourceType = ENetowrkType.NETWORK;
      isNetworkGroupId = false;
      isNetworkId = true;
    }

    tmpPayload = {
      sourceType: tmpServiceId ? 'service' : currentSourceType,
      networkGroupId: isNetworkGroupId ? tmpNetworkId : undefined,
      networkId: isNetworkId ? tmpNetworkId : undefined,
      serviceId: tmpServiceId,
    };
    console.log(tmpPayload, 'tmpPayload');
    if (drilldownNetworkId) {
      tmpPayload = {
        sourceType: serviceId ? 'service' : 'network',
        networkId: tmpNetworkId,
        serviceId: tmpServiceId,
      };
    }
    return tmpPayload;
  }, [drilldownNetworkId, networkDataSet, networkType, serviceId]);

  const queryNetworkTree = useCallback((queryParams: any) => {
    setSensorInfoLoading(true);
    queryAnalyzableObject(queryParams).then((res) => {
      const { success, result } = res;
      setSensorInfoLoading(false);
      if (success) {
        if (result?.length) {
          onNoFpcSensor();
        }
        // console.log(result, 'sensorNetworkTree');
        setSensorNetworkTree(result);
      }
    });
  }, []);

  // 获取networktree结构
  useEffect(() => {
    // console.log(queryNetworkTreeParams, 'queryNetworkTreeParams');
    if (networkDataSetLoading === false) {
      let queryNetwork = queryNetworkTreeParams.networkGroupId;
      if (!queryNetwork) {
        queryNetwork = queryNetworkTreeParams.networkId;
      }
      queryNetworkTree(queryNetwork ? queryNetworkTreeParams : null);
    }
  }, [networkDataSetLoading, queryNetworkTree, queryNetworkTreeParams]);

  // 默认选中第一个探针的第一个网络
  const [sensorValue, setSensorValue] = useState('');
  useEffect(() => {
    if (sensorList.length) {
      // setSensorAndNetwork({ fpcSerialNumber: sensorList[0].value, networkId: '' });
      setSensorValue(sensorList[0].value);
    } else {
      setSensorValue('');
    }
    if (initSelect) {
      setSensorValue(initSelect.fpcSerialNumber);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sensorList]);

  const [networkValue, setNetworkValue] = useState('');

  useEffect(() => {
    // if (sensorValue && sensorNetworkList[sensorValue]?.length) {
    //   // 将探针网络放到过滤组件中修改不需要放第一个探针网络的值
    //   // const tmpDefNetwork = sensorNetworkList[sensorValue][0]?.value;
    //   // // setSensorAndNetwork({ ...sensorAndNetwork, networkId: tmpDefNetwork });
    //   // setNetworkValue(tmpDefNetwork);
    //   setNetworkValue('ALL');
    // } else {
    //   setNetworkValue('');
    // }
    if (initSelect) {
      setNetworkValue(initSelect.networkId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sensorValue, sensorNetworkList]);

  const [initState, setInitState] = useState(true);
  useEffect(() => {
    if (sensorValue?.length) {
      setInitState(false);
      if (initState) {
        setsubmitInfo({ fpcSerialNumber: sensorValue, networkId: networkValue });
      }
    }
  }, [initState, networkValue, sensorValue]);

  const renderPacketDefPage = useMemo(() => {
    let res = E_PKT_DEF_SHOW_TYPE.DEF;
    if (JSON.stringify(sensorNetworkTree) === '{}') {
      res = E_PKT_DEF_SHOW_TYPE.NO_NETWORK;
    }
    if (JSON.stringify(sensorNetworkTree) === 'undefined') {
      res = E_PKT_DEF_SHOW_TYPE.ERROR;
    }
    return res;
  }, [sensorNetworkTree]);

  return (
    <Card
      title={
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <span>所属网络</span>
          <Divider type="vertical" />
          <span>探针： </span>
          <Select
            size="small"
            value={sensorValue}
            disabled={disabled}
            style={{ minWidth: 120 }}
            onChange={(e) => {
              setSensorValue(e);
              // setSensorAndNetwork({ ...sensorAndNetwork, fpcSerialNumber: e });
            }}
          >
            {sensorList.map((item: any) => {
              return <Option value={item.key}>{item.title}</Option>;
            })}
          </Select>
          {/* <Divider type="vertical" />
          <span>探针网络： </span>
          <Select
            size="small"
            value={networkValue}
            disabled={disabled}
            style={{ minWidth: 120 }}
            onChange={(e) => {
              setNetworkValue(e);
              // setSensorAndNetwork({ ...sensorAndNetwork, networkId: e });
            }}
          >
            {sensorValue in sensorNetworkList &&
              sensorNetworkList[sensorValue]?.map((item: any) => {
                return <Option value={item?.key}>{item?.title}</Option>;
              })}
          </Select> */}
        </div>
      }
      size={'small'}
      loading={sensorInfoLoading}
      extra={
        <Button
          size="small"
          type="link"
          onClick={() => {
            console.log(sensorValue, 'sensorValue');
            setsubmitInfo({ fpcSerialNumber: sensorValue, networkId: networkValue });
          }}
        >
          更新
        </Button>
      }
    >
      {packetShowPage[renderPacketDefPage](
        children,
        submitInfo,
        setNetworkValue,
        sensorNetworkList,
      )}
      {/* {JSON.stringify(sensorNetworkTree) === '{}' ? (
        <Result
          title="未找到网络"
          extra={
            <Button
              type="primary"
              onClick={() => {
                window.location.reload();
              }}
            >
              刷新
            </Button>
          }
        />
      ) : (
        React.cloneElement(children, {
          submitInfo: submitInfo,
        })
      )} */}
    </Card>
  );
}
