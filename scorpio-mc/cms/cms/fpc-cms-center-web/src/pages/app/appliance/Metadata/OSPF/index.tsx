import { enum2List } from '@/common/app';
import Ellipsis from '@/components/Ellipsis';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataOspf } from '../typings';
import { EMetadataProtocol, EOspfMessageType } from '../typings';

const messageTypeEnumList = enum2List(EOspfMessageType);

export const tableColumns: IColumnProps<IMetadataOspf>[] = [
  {
    title: '版本',
    dataIndex: 'version',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    render: (text) => EOspfMessageType[text],

    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: messageTypeEnumList,
  },
  {
    title: '包长',
    dataIndex: 'packetLength',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '源路由器',
    dataIndex: 'sourceOspfRouter',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '区域ID',
    dataIndex: 'areaId',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '通告IPv4地址',
    dataIndex: 'linkStateIpv4Address',
    width: 300,
    render: (arr) => (
      <Ellipsis tooltip={false} lines={1}>
        {arr.join(',')}
      </Ellipsis>
    ),

    searchable: true,
    fieldType: EFieldType.ARRAY,
    operandType: EFieldOperandType.STRING,
  },
  {
    title: '通告IPv6地址',
    dataIndex: 'linkStateIpv6Address',
    width: 300,
    render: (arr) => (
      <Ellipsis tooltip={false} lines={1}>
        {arr.join(',')}
      </Ellipsis>
    ),

    searchable: true,
    fieldType: EFieldType.ARRAY,
    operandType: EFieldOperandType.STRING,
  },
  {
    title: '详情',
    dataIndex: 'message',
    width: 300,
    render: (text) => (
      <Ellipsis tooltip={false} lines={1}>
        {text}
      </Ellipsis>
    ),

    searchable: true,
  },
];

interface Props {
  paneTitle?: string;
}

const MetadataOspf = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.OSPF}
      tableColumns={tableColumns as any}
    />
  );
};

export default MetadataOspf;
