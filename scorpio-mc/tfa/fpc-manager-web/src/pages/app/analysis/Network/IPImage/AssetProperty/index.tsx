import { WalletOutlined } from '@ant-design/icons';
import { Card } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  getAssetsList,
  getDeviceTypeLists,
  getOperateSystemTypeLists,
} from '@/pages/app/analysis/Assets/service';
import type { AssetItem } from '../../../Assets/AssetsList';

import type { ISearchBoxInfo } from '../typings';
import moment from 'moment';

export interface IAssertSerchProps {
  searchInfo: ISearchBoxInfo;
}

const AssetProperty = (props: IAssertSerchProps) => {
  const { searchInfo } = props;
  const { IpAddress } = searchInfo;
  const { Meta } = Card;
  const [isShow, setIsShow] = useState(false);
  const [assetMeg, setAssetMeg] = useState<AssetItem>();
  const queryAssetMeg = useCallback(() => {
    setIsShow(false);
    if (IpAddress) {
      getAssetsList({ ipAddress: IpAddress }).then((res) => {
        const { success, result } = res;
        if (success) {
          const { content } = result;
          if (content.length > 0) {
            setAssetMeg(content[0]);
            setIsShow(true);
          }
        }
      });
    }
  }, [IpAddress]);

  useEffect(() => {
    queryAssetMeg();
  }, [queryAssetMeg]);

  const [deviceTypeMap, setDeviceTypeMap] = useState({});
  useEffect(() => {
    getDeviceTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const deviceMap = {};
        result.forEach((item: any) => {
          deviceMap[item.id] = item.device_name;
        });
        setDeviceTypeMap(deviceMap);
      }
    });
  }, []);
  const [osTypeMap, setOsTypeMap] = useState({});
  useEffect(() => {
    getOperateSystemTypeLists().then((res) => {
      const { success, result } = res;
      if (success) {
        const osMap = {};
        result.forEach((item: any) => {
          osMap[item.id] = item.os;
        });
        setOsTypeMap(osMap);
      }
    });
  }, []);

  const assetDescrption = useMemo(() => {
    // const {  deviceType, os, port, label, timestamp } = assetMeg;
    // console.log(assetMeg, 'assetMeg');
    const updateTime = assetMeg?.timestamp
      ? moment(assetMeg?.timestamp).format('YYYY-MM-DD HH:mm:ss')
      : undefined;
    const device = assetMeg?.deviceType
      ?.split(',')
      .map((item) => deviceTypeMap[item])
      .join(',');
    const os = assetMeg?.os
      ?.split(',')
      .map((item) => osTypeMap[item])
      .join(',');
    const osVersionType = assetMeg?.osValue2;
    const updateTimeText = updateTime ? updateTime : '';
    const deviceText = device ? '设备类型: ' + device + '; ' : '';
    const osText = assetMeg?.os ? '操作系统: ' + assetMeg?.os + '; ' : '';
    const portText = assetMeg?.port ? '开放端口: ' + assetMeg?.port + '; ' : '';
    const labelText = assetMeg?.label ? '业务标签' + assetMeg?.label + '; ' : '';

    const description = deviceText + osText + portText + labelText;
    return {
      title: updateTimeText,
      description: description,
    };
  }, [assetMeg, deviceTypeMap, osTypeMap]);

  return isShow ? (
    <Card style={{ margin: 10, padding: 0 }}>
      <Meta
        title={`资产属性 (更新时间: ${assetDescrption.title})`}
        avatar={<WalletOutlined />}
        description={assetDescrption.description}
      />
    </Card>
  ) : null;
};

export default AssetProperty;
