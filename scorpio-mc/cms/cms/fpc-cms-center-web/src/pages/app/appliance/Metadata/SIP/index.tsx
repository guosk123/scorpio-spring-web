import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { history } from 'umi';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataSip } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';

export const tableColumns: IColumnProps<IMetadataSip>[] = [
  {
    title: '主叫',
    dataIndex: 'from',
    searchable: true,
  },
  {
    title: '被叫',
    dataIndex: 'to',
    searchable: true,
  },
  {
    title: '信令传输协议',
    dataIndex: 'ipProtocol',
    searchable: true,
  },
  {
    title: '请求类型',
    dataIndex: 'type',
    searchable: true,
  },
  {
    title: '序列号',
    dataIndex: 'seqNum',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '呼叫ID',
    dataIndex: 'callId',
    searchable: true,
  },
  {
    title: '请求URI',
    dataIndex: 'requestUri',
    searchable: true,
  },
  {
    title: '状态码',
    dataIndex: 'statusCode',
    searchable: true,
    render: (dom, record) => {
      const { statusCode } = record;
      return statusCode ? statusCode : '-';
    },
  },
  {
    title: 'SDP',
    dataIndex: 'sdp',
    searchable: true,
    fieldType: EFieldType.Map,
  },
];

const MetadataSIP = () => {
  const { pathname } = history.location;
  return (
    <Template<IMetadataSip>
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.SIP}
      tableColumns={tableColumns}
      isNewIpFieldType={true}
    />
  );
};

export default MetadataSIP;
