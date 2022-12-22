import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFlowRecordColumnProps } from '@/pages/app/Netflow/FlowRecord'; 
import moment from 'moment';
import type { IIpDevices } from '../typings';
import numeral from 'numeral';

export const allDeviceColumns: IFlowRecordColumnProps<IIpDevices>[] = [
  {
    title: '#',
    dataIndex: 'index',
    align: 'center',
    width: 60,
    ellipsis: true,
    searchable: false,
    render: (dom, record, index) => {
      return index + 1;
    },
  },
  {
    title: '设备IP',
    dataIndex: 'deviceIp',
    width: 120,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.IP,
    show: true,
    fieldType: EFieldType.IP,
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    width: 200,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.STRING,
    show: true,
  },
  // {
  //   title: '设备类型',
  //   dataIndex: 'type',
  //   width: 90,
  //   ellipsis: true,
  //   searchable: true,
  //   operandType: EFieldOperandType.STRING,
  //   show: true,
  // },
  {
    title: 'RTP总包数',
    dataIndex: 'rtpTotalPackets',
    sorter: true,
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    render: (text) => numeral(text).format('0,0'),
  },
  {
    title: 'RTP丢包数',
    dataIndex: 'rtpLossPackets',
    sorter: true,
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    render: (text) => numeral(text).format('0,0'),
  },
  {
    title: 'RTP丢包率',
    dataIndex: 'rtpLossPacketsRate',
    width: 130,
    searchable: false,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '最大抖动',
    dataIndex: 'jitterMax',
    sorter: true,
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '平均抖动',
    dataIndex: 'jitterMean',
    sorter: true,
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '上线时间',
    dataIndex: 'startTime',
    sorter: true,
    width: 170,
    show: true,
    searchable: false,
    render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '更新时间',
    dataIndex: 'reportTime',
    sorter: true,
    width: 170,
    show: true,
    searchable: false,
    render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
];
