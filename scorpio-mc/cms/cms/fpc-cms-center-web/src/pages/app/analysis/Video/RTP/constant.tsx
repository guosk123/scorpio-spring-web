import { enum2List, IP_PROTOCOL_LIST } from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IFlowRecordColumnProps } from '@/pages/app/appliance/FlowRecords/Record/typing'; 
import moment from 'moment';
import type { IRTPFlow } from '../typings';
import { ERTPStatus } from '../typings';
import { getRTPStatusText } from '../typings';
import numeral from 'numeral';

const errorTime = '1970-01-01T08:00:00'
export const allRTPColumns: IFlowRecordColumnProps<IRTPFlow>[] = [
  {
    title: '#',
    align: 'center',
    dataIndex: 'index',
    width: 60,
    ellipsis: true,
    searchable: false,
    render: (dom, record, index) => {
      return index + 1;
    },
  },
  {
    title: '通讯开始时间',
    dataIndex: 'startTime',
    sorter: true,
    width: 170,
    show: true,
    searchable: false,
    render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: '通讯结束时间',
    dataIndex: 'endTime',
    width: 170,
    show: true,
    searchable: false,
    render: (ariseTime) => {
      if (ariseTime.includes(errorTime)) {
        return <div style={{ textAlign: 'center' }}>-</div>
      }
      return moment(ariseTime).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  {
    title: '发送方设备编码',
    dataIndex: 'from',
    width: 200,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.STRING,
    show: true,
  },
  {
    title: '发送方IP',
    dataIndex: 'srcIp',
    width: 120,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.IP,
    show: true,
    fieldType: EFieldType.IP,
  },
  {
    title: '发送方端口',
    dataIndex: 'srcPort',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },
  {
    title: '接收方设备编码',
    dataIndex: 'to',
    width: 200,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.STRING,
    show: true,
  },
  {
    title: '接收方IP',
    dataIndex: 'destIp',
    width: 120,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.IP,
    show: true,
    fieldType: EFieldType.IP,
  },
  {
    title: '接受方端口',
    dataIndex: 'destPort',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },
  {
    title: '传输层协议',
    dataIndex: 'ipProtocol',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: IP_PROTOCOL_LIST.map((item) => ({
      text: item.toLocaleUpperCase(),
      value: item,
    })),
    render: (text, { ipProtocol }) => ipProtocol?.toLocaleUpperCase(),
  },
  {
    title: 'SSRC',
    dataIndex: 'ssrc',
    width: 90,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.STRING,
    show: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 150,
    show: true,
    render: (text) => getRTPStatusText(text),
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: enum2List(ERTPStatus).map(({ value }) => ({
      value,
      text: getRTPStatusText(parseInt(value as string, 10)),
    })),
  },
  {
    title: '通讯邀请时间',
    dataIndex: 'inviteTime',
    width: 170,
    show: true,
    searchable: false,
    render: (ariseTime) => moment(ariseTime).format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    title: 'RTP总包数',
    dataIndex: 'rtpTotalPackets',
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    render: (text) => numeral(text).format('0,0'),
  },
  {
    title: 'RTP丢包数',
    dataIndex: 'rtpLossPackets',
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
    operandType: EFieldOperandType.STRING,
  },
  {
    title: '最大抖动',
    dataIndex: 'jitterMax',
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '平均抖动',
    dataIndex: 'jitterMean',
    width: 100,
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '载荷',
    dataIndex: 'payload',
    width: 100,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.STRING,
    show: true,
  },
  {
    title: '邀请主叫IP',
    dataIndex: 'inviteSrcIp',
    width: 120,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.IP,
    show: true,
    fieldType: EFieldType.IP,
  },
  {
    title: '邀请主叫端口',
    dataIndex: 'inviteSrcPort',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },
  {
    title: '邀请被叫IP',
    dataIndex: 'inviteDestIp',
    width: 120,
    ellipsis: true,
    searchable: true,
    operandType: EFieldOperandType.IP,
    show: true,
    fieldType: EFieldType.IP,
  },
  {
    title: '邀请被叫端口',
    dataIndex: 'inviteDestPort',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },
  {
    title: '邀请传输层协议',
    dataIndex: 'inviteIpProtocol',
    width: 90,
    show: true,
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: IP_PROTOCOL_LIST.map((item) => ({
      text: item.toLocaleUpperCase(),
      value: item,
    })),
    render: (text, { inviteIpProtocol }) => inviteIpProtocol.toLocaleUpperCase(),
  },
];
