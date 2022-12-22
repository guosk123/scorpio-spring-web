import type { IProTableData } from '@/common/typings';
import type { AppModelState } from '@/models/app/index';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import type { MouseEventHandler } from 'dva/node_modules/@types/react';
import type { Dispatch } from 'umi';
import { ERealTimeStatisticsFlag } from 'umi';
import type { INetflowDevice, INetflowDeviceNetif } from '../typing';
import type { INetflowModel } from '../model';
import { stringify } from 'qs';
import { useEffect, useMemo, useRef, useState } from 'react';
import { convertBandwidth, processingMinutes } from '@/utils/utils';
import { Button, Form, Input, InputNumber, Modal, message } from 'antd';
import { EDeviceType, EProtocalVersion } from '../typing';
import { connect, history } from 'umi';
import { getTablePaginationDefaultSettings } from '@/common/app';
import { editSource } from '../service';
import { ESortDirection } from '../typing';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { sourceConverter } from '../utils/converter';
import ProTable from '@ant-design/pro-table';
import ajax from '@/utils/frame/ajax';
import TextArea from 'antd/lib/input/TextArea';
import TimeRangeSlider from '@/components/TimeRangeSlider';
import styles from './index.less';

interface IDeviceParams extends AppModelState {
  location: {
    pathname: string;
  };
  dispatch: Dispatch;
  realTimeStatisticsFlag: ERealTimeStatisticsFlag;
}

// 验证速率信息
const validateSpeed = (rules: any, value: string, callback: any) => {
  if (!isNaN(parseInt(value, 10)) && parseInt(value, 10) >= 0) {
    callback();
  }
  callback('接口速率不能为负值');
};

const Device: React.FC<IDeviceParams> = ({ globalSelectedTime, realTimeStatisticsFlag }) => {
  const realTimePollingRef = useRef<number | undefined>(undefined);
  /** proTable更新ref */
  const actionRef = useRef<ActionType>();

  /** Modal相关 */
  // Modal开关
  const [editVisible, setEditVisible] = useState(false);
  // 设备行信息
  const [selectedRow, setSelectedRow] = useState<INetflowDevice | INetflowDeviceNetif>();

  /** 排序信息 */
  const [sortProperty, setSortProperty] = useState<string>();
  const [totalBytesDirection, setTotalBytesDirection] = useState<'asc' | 'desc'>('asc');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // window.setInterval(() => {
  //   console.log(globalSelectedTime.originStartTime)
  // }, 1000);

  // 时间变动，重新获取数据
  useEffect(() => {
    actionRef.current?.reload();
  }, [globalSelectedTime, sortProperty, sortDirection]);

  /** 表单相关方法 */
  // 处理编辑按钮点击
  function handleEdit(record: INetflowDevice | INetflowDeviceNetif) {
    setSelectedRow({ ...record });
    setEditVisible(true);
  }

  // 处理表单提交
  function handleSubmit(values: Record<string, any>) {
    editSource({
      id: selectedRow ? selectedRow.id : '',
      deviceType: selectedRow ? selectedRow.deviceType : '',
      alias: values.alias,
      description: values.description,
      netifSpeed: (values as INetflowDeviceNetif).netifSpeed,
    }).then((res) => {
      if (!res.success) {
        message.error('保存失败');
        return;
      }
      actionRef.current?.reload();
      message.success('保存成功');
    });
    setEditVisible(false);
  }

  // 截断时间
  const cutStartTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originStartTime);
  }, [globalSelectedTime.originStartTime, globalSelectedTime]);

  const cutEndTime = useMemo(() => {
    return processingMinutes(globalSelectedTime.originEndTime);
  }, [globalSelectedTime.originEndTime, globalSelectedTime]);

  /** modal相关方法 */
  // 计算modal标题
  const rowTitle = useMemo(() => {
    if (!selectedRow) {
      return '';
    }
    if (parseInt(selectedRow.deviceType, 10) === EDeviceType.device) {
      // 设备的情况
      if ((selectedRow as INetflowDevice)?.alias) {
        return (selectedRow as INetflowDevice).alias;
      }
      return (selectedRow as INetflowDevice).deviceName;
    }
    if (parseInt(selectedRow.deviceType, 10) === EDeviceType.interface) {
      // 接口的情况
      if ((selectedRow as INetflowDeviceNetif)?.alias) {
        return (selectedRow as INetflowDeviceNetif).alias;
      }
      return `接口${(selectedRow as INetflowDeviceNetif)?.netifNo}`;
    }
    return '';
  }, [selectedRow]);
  // 计算modal别名
  const rowAlias = useMemo(() => {
    if (!selectedRow) {
      return '';
    }
    return selectedRow.alias;
  }, [selectedRow]);

  /** 表格相关方法 */
  // ProTable列定义
  const tableColumns: ProColumns<INetflowDevice | INetflowDeviceNetif>[] = [
    {
      title: '设备',
      dataIndex: 'deviceName',
      align: 'center',
      search: false,
      ellipsis: true,
      render: (_, record) => {
        return (
          <>
            <span className="link" onClick={() => handleDevice(record)}>
              {parseInt(record.deviceType, 10) === EDeviceType.device
                ? (record as INetflowDevice).deviceName
                : `接口${(record as INetflowDeviceNetif).netifNo}`}
            </span>
          </>
        );
      },
    },
    {
      title: '别名',
      dataIndex: 'alias',
      align: 'center',
      ellipsis: true,
      search: false,
      render: (_, record) => {
        return record.alias || '';
      },
    },
    {
      title: '协议版本',
      dataIndex: 'protocolVersion',
      align: 'center',
      valueType: 'select',
      search: false,
      valueEnum: Object.keys(EProtocalVersion).reduce((prev, current) => {
        return {
          ...prev,
          [current]: { text: EProtocalVersion[current] },
        };
      }, {}),
    },
    {
      title: '平均总带宽',
      dataIndex: 'totalBandwidth',
      align: 'center',
      search: false,
      sorter: true,
      render: (_, { totalBandwidth }) => {
        return <>{convertBandwidth(totalBandwidth)}</>;
      },
    },
    {
      title: '平均入带宽',
      dataIndex: 'ingestBandwidth',
      align: 'center',
      search: false,
      sorter: true,
      render: (_, record) => {
        return (
          <>
            {parseInt(record.deviceType, 10) === EDeviceType.interface &&
            (record as INetflowDeviceNetif).ingestBandwidth
              ? convertBandwidth((record as INetflowDeviceNetif).ingestBandwidth)
              : '-'}
          </>
        );
      },
    },
    {
      title: '平均出带宽',
      dataIndex: 'transmitBandwidth',
      align: 'center',
      search: false,
      sorter: true,
      render: (_, record) => {
        return (
          <>
            {parseInt(record.deviceType, 10) === EDeviceType.interface &&
            (record as INetflowDeviceNetif).transmitBandwidth
              ? convertBandwidth((record as INetflowDeviceNetif).transmitBandwidth)
              : '-'}
          </>
        );
      },
    },
    {
      title: '接口速率',
      dataIndex: 'netifSpeed',
      align: 'center',
      search: false,
      render: (_, record) => {
        return (
          <>
            {parseInt(record.deviceType, 10) === EDeviceType.interface &&
            (record as INetflowDeviceNetif).netifSpeed !== undefined
              ? `${(record as INetflowDeviceNetif).netifSpeed}Mbps`
              : '-'}
          </>
        );
      },
    },
    {
      title: '描述信息',
      dataIndex: 'description',
      align: 'center',
      ellipsis: true,
      search: false,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      align: 'center',
      search: false,
      width: 80,
      render: (id, record) => {
        return (
          <>
            <span
              className="link"
              onClick={(() => handleEdit(record)) as MouseEventHandler<HTMLElement>}
            >
              编辑
            </span>
          </>
        );
      },
    },
  ];
  // 处理设备点击
  function handleDevice(record: INetflowDevice | INetflowDeviceNetif) {
    const uriPrefix = '/analysis/netflow/device';

    const { deviceName, deviceType } = record;
    if (parseInt(deviceType, 10) === EDeviceType.device) {
      history.push(`${uriPrefix}/${deviceName}/dashboard`);
    } else {
      history.push(
        `${uriPrefix}/${deviceName}/netif/${(record as INetflowDeviceNetif).netifNo}/dashboard`,
      );
    }
  }

  // 获取数据方法
  const fetchData = async (params: { current?: number; pageSize?: number }) => {
    const { current, pageSize, ...rest } = params;
    const newParams = {
      pageSize,
      pageNumber: current! - 1,
      startTime: cutStartTime,
      endTime: cutEndTime,
      sortDirection: totalBytesDirection,
      sortProperty: 'total_bytes',
      ...rest,
    } as any;
    const { success, result } = (await ajax(
      `${API_VERSION_PRODUCT_V1}/appliance/netflow-sources?${stringify(newParams)}`,
    )) as any;
    // IAjaxResponseFactory<IPageFactory<INetflowDevice>>;
    if (!success) {
      return {
        data: [],
        success,
      };
    }
    return {
      data:
        result.content.length !== 0
          ? sourceConverter(result.content, sortDirection, sortProperty)
          : [],
      success,
      page: result.number,
      total: result.totalElements,
    } as IProTableData<INetflowDevice[]>;
  };

  // 判断实时统计
  useEffect(() => {
    if (realTimeStatisticsFlag === ERealTimeStatisticsFlag.CLOSED) {
      window.clearInterval(realTimePollingRef.current);
    } else {
      realTimePollingRef.current = window.setInterval(() => {
        actionRef.current?.reload();
      }, 300000);
    }

    return () => {
      window.clearInterval(realTimePollingRef.current);
    };
  }, [realTimeStatisticsFlag, fetchData]);
  return (
    <>
      <TimeRangeSlider />
      <ProTable<INetflowDevice | INetflowDeviceNetif>
        rowKey="id"
        bordered
        size="small"
        columns={tableColumns}
        request={fetchData}
        search={false}
        actionRef={actionRef}
        form={{
          ignoreRules: false,
        }}
        dateFormatter="string"
        toolBarRender={false}
        pagination={getTablePaginationDefaultSettings()}
        onChange={(_, filter, sorter: any) => {
          setSortProperty(sorter.field);
          if (sorter.order === `${ESortDirection.ASC}end`) {
            if (sorter.field === 'totalBandwidth') {
              setTotalBytesDirection(ESortDirection.ASC);
              return;
            }
            setSortDirection(ESortDirection.ASC);
          } else if (sorter.order === `${ESortDirection.DESC}end`) {
            if (sorter.field === 'totalBandwidth') {
              setTotalBytesDirection(ESortDirection.DESC);
              return;
            }
            setSortDirection(ESortDirection.DESC);
          }
        }}
      />
      <Modal
        title={rowTitle}
        visible={editVisible}
        destroyOnClose
        keyboard={false}
        maskClosable={false}
        onOk={() => setEditVisible(false)}
        onCancel={() => setEditVisible(false)}
        footer={null}
        getContainer={false}
      >
        <>
          <Form
            name="basic"
            labelCol={{ span: 5 }}
            wrapperCol={{ span: 16 }}
            onFinish={handleSubmit}
            initialValues={{
              alias: rowAlias,
              netifSpeed: (selectedRow as INetflowDeviceNetif)?.netifSpeed || 0,
              description: selectedRow?.description || '',
            }}
          >
            <Form.Item
              label="别名"
              name="alias"
              rules={[{ max: 32, message: '别名不能大于32个字符' }]}
            >
              <Input placeholder="请输入别名" style={{ width: 300 }} />
            </Form.Item>
            {parseInt(selectedRow?.deviceType || '', 10) === EDeviceType.interface && (
              <Form.Item label="速率" name="netifSpeed" rules={[{ validator: validateSpeed }]}>
                <InputNumber
                  style={{ width: 300 }}
                  parser={(value) => value!.replace('Mbps', '')}
                  formatter={(value) => `${value}Mbps`}
                />
              </Form.Item>
            )}
            <Form.Item
              label="描述"
              name="description"
              rules={[{ max: 255, message: '描述不能大于255个字符' }]}
            >
              <TextArea placeholder="请输入描述信息" style={{ width: 300 }} />
            </Form.Item>
            <div className={styles.modal_button_container}>
              <Button className={styles.modal_button} type="primary" htmlType="submit">
                确定
              </Button>
              <Button className={styles.modal_button} onClick={() => setEditVisible(false)}>
                取消
              </Button>
            </div>
          </Form>
        </>
      </Modal>
    </>
  );
};

export default connect(
  ({ appModel: { globalSelectedTime, realTimeStatisticsFlag } }: INetflowModel) => ({
    globalSelectedTime,
    realTimeStatisticsFlag,
  }),
)(Device);
