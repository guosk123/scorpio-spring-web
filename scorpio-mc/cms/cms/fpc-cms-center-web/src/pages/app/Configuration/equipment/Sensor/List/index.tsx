import type { TableColumnProps } from 'antd';
import { Badge } from 'antd';
import { message } from 'antd';
import { Modal } from 'antd';
import { Popconfirm } from 'antd';
import { Button, Space } from 'antd';
import ReloadOutlined from '@ant-design/icons/lib/icons/ReloadOutlined';
import EnhancedTable from '@/components/EnhancedTable';
import moment from 'moment';
import { connect } from 'dva';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { PlusOutlined, QuestionCircleOutlined, RedoOutlined } from '@ant-design/icons';
import type { Dispatch } from 'umi';
import { history } from 'umi';
import DetailButton from '../../components/DetailButton';
import StatusBox from '../../components/StatusBox';
import { formatDuration } from '@/utils/utils';
import { queryLoginSensorUrl, querySyncRemoteServers } from '../../service';
import UpLoadLicense from '../components/UpLoadLicense';
import { queryDeviceInfo } from '@/pages/frame/System/SSO/service';
import { v1 } from 'uuid';

export interface SensorItem {
  id: string;
  name: string;
  version: string;
  connectStatus: any;
  connectStatusText: any;
  cpuMetric: number;
  memoryMetric: number;
  diskUsed: string;
  systemFsMetric: string; // 系统分区使用率
  indexFsMetric: string; // 索引分区使用率
  metadataFsMetric: string; // 元数据分区使用率
  packetFsMetric: string; // 全包分区使用率
  lastInteractiveTime: number;
  lastInteractiveLatency: string;
  upTime: number;
  alarmCount: string;
  ip: string;
  lastLoginTime: number;
  upperCMS: string;
  licenseStatusText: string;
  serialNumber: string;
}

interface Props {
  querySensorList: any;
  deleteSensorItem: any;
  querySensorListLoading: boolean;
}

const LoginToSensor = (record: SensorItem) => {
  queryLoginSensorUrl(record.serialNumber).then((res) => {
    const { success, result } = res;
    if (success) {
      window.open(result);
    }
  });
};

function SensorCMSList(props: Props) {
  const { querySensorList, deleteSensorItem, querySensorListLoading } = props;
  const [tableState, setTableState] = useState<SensorItem[]>();

  const loginToSensorCheck = (record: SensorItem) => {
    new Promise((r, j) => {
      queryDeviceInfo().then((res) => {
        const { success, result } = res;
        if (success && !result.deviceName) {
          j(false);
        } else {
          r(true);
        }
      });
    })
      .then(() => {
        LoginToSensor(record);
      })
      .catch(() => {
        Modal.warning({
          title: '提示',
          content: (
            <div>
              <span>{'设备信息配置不完整，请联系系统管理员前往：'}</span>
              <br />
              <span>{'系统配置>设备信息，完善信息'}</span>
            </div>
          ),
          okText: '确定',
        });
      });
  };

  const queryList = useCallback(
    (payload?) => {
      querySensorList(payload).then((result: SensorItem[]) => {
        setTableState(result.sort((item) => (item.connectStatus === '1' ? 1 : -1)));
      });
    },
    [querySensorList],
  );

  useEffect(() => {
    queryList();
  }, [queryList]);

  const deleteSensor = useCallback(
    (record: any) => {
      deleteSensorItem(record.id).then((success: boolean) => {
        if (success) {
          queryList();
        }
      });
    },
    [deleteSensorItem, queryList],
  );
  const columns: TableColumnProps<SensorItem>[] = [
    {
      title: '探针名称',
      dataIndex: 'name',
      key: 'name',
      align: 'center',
      width: 200,
    },
    {
      title: '探针版本',
      dataIndex: 'version',
      key: 'version',
      width: 200,
      align: 'center',
    },
    {
      title: '在线状态',
      dataIndex: 'connectStatusText',
      key: 'connectStatusText',
      width: 200,
      align: 'center',
      render: (text) => <Badge status={text === '连接正常' ? 'success' : 'error'} text={text} />,
    },
    {
      title: 'CPU',
      dataIndex: 'cpuMetric',
      key: 'cpuMetric',
      width: 200,
      align: 'center',
      render: (text) => <StatusBox progress={text} />,
    },
    {
      title: '内存',
      dataIndex: 'memoryMetric',
      key: 'memoryMetric',
      width: 200,
      align: 'center',
      render: (text) => <StatusBox progress={text} />,
    },
    {
      title: '硬盘',
      dataIndex: 'diskUsed',
      key: 'diskUsed',
      width: 220,
      align: 'center',
      render: (text, record) => {
        const showRecord = {
          systemFsMetric: '系统分区',
          indexFsMetric: '索引分区',
          metadataFsMetric: '详单冷分区',
          metadataHotFsMetric: '详单热分区',
          // packetFsMetric: '全包分区',
        };
        return (
          <div style={{ textAlign: 'right' }}>
            {Object.keys(showRecord).map((item) => {
              const res = record[item] || 0;
              return (
                <Fragment key={item}>
                  <span>
                    {/* {`${showRecord[item]}: ${res} %`} */}
                    {`${showRecord[item]}:`}
                    <StatusBox progress={res} width={110} />
                  </span>
                  <br />
                </Fragment>
              );
            })}
          </div>
        );
      },
    },
    {
      title: '最近通讯时间',
      dataIndex: 'lastInteractiveTime',
      key: 'lastInteractiveTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '最近交互时延(ms)',
      dataIndex: 'lastInteractiveLatency',
      key: 'lastInteractiveLatency',
      width: 200,
      align: 'center',
    },
    {
      title: '系统运行时间',
      dataIndex: 'upTime',
      key: 'upTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? `${formatDuration(text * 1000)}` : '-'),
    },
    {
      title: '设备告警',
      dataIndex: 'alarmCount',
      key: 'alarmCount',
      width: 200,
      align: 'center',
    },
    {
      title: 'IP',
      dataIndex: 'ip',
      key: 'ip',
      width: 200,
      align: 'center',
    },
    {
      title: '登录时间',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      width: 200,
      align: 'center',
      render: (text = 0) => (text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : ''),
    },
    {
      title: '管理CMS',
      dataIndex: 'cmsName',
      key: 'cmsName',
      width: 200,
      align: 'center',
    },
    {
      title: 'License',
      dataIndex: 'licenseStatusText',
      key: 'licenseStatusText',
      width: 200,
      align: 'center',
    },
    {
      title: '设备序列号',
      dataIndex: 'serialNumber',
      key: 'serialNumber',
      width: 280,
      align: 'center',
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      width: 240,
      align: 'center',
      fixed: 'right',
      render: (text, record) => {
        return (
          <Fragment>
            {record?.connectStatus === '1' ? (
              <Popconfirm
                title="确定删除吗？"
                onConfirm={() => {
                  deleteSensor(record);
                }}
                icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              >
                <Button type="link" size="small">
                  删除
                </Button>
              </Popconfirm>
            ) : (
              <Button
                onClick={() => {
                  // LoginToSensor(record);
                  loginToSensorCheck(record);
                }}
                type="link"
                size="small"
              >
                登录
              </Button>
            )}
            <DetailButton detail={record} col={columns} />
            <UpLoadLicense id={record.id} disabled={record?.connectStatus === '1'} />
          </Fragment>
        );
      },
    },
  ];

  return (
    <EnhancedTable<SensorItem>
      tableKey="sensorList"
      columns={columns}
      rowKey={() => v1()}
      loading={querySensorListLoading}
      dataSource={tableState}
      size="small"
      bordered
      scroll={{ x: 1500 }}
      extraTool={
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Space>
            <Button
              type="primary"
              icon={<RedoOutlined />}
              onClick={() => {
                querySyncRemoteServers().then((res) => {
                  const { success } = res;
                  if (success) {
                    message.info('更新成功');
                  } else {
                    message.info('更新失败');
                  }
                  querySensorList();
                });
              }}
            >
              更新集群配置
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              style={{ display: 'none' }}
              onClick={() => {
                history.push('/configuration/equipment/sensor/create');
              }}
            >
              新建
            </Button>
            <Button
              type="primary"
              icon={<ReloadOutlined />}
              onClick={() => {
                queryList({});
              }}
            >
              刷新
            </Button>
          </Space>
        </div>
      }
    />
  );
}

export default connect(
  ({ loading: { effects } }: any) => {
    return {
      querySensorListLoading: effects['ConfigurationModel/querySensorList'],
    };
  },
  (dispatch: Dispatch) => {
    return {
      querySensorList: (payload: any) => {
        return dispatch({
          type: 'ConfigurationModel/querySensorList',
          payload,
        });
      },
      deleteSensorItem: (delItem: any) => {
        return dispatch({
          type: 'ConfigurationModel/deleteSensorItem',
          payload: { id: delItem },
        });
      },
    };
  },
)(SensorCMSList);
