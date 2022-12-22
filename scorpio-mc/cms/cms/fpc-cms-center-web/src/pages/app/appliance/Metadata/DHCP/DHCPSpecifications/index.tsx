import { DHCP_MESSAGE_TYPE_ENUM, DHCP_MESSAGE_TYPE_LIST, DHCP_VERSION_ENUM } from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../../components/Template';
import Template from '../../components/Template';
import type { IMetadataDhcp } from '../../typings';
import { EMetadataProtocol } from '../../typings';

export const tableColumns: IColumnProps<IMetadataDhcp>[] = [
  {
    title: 'DHCP版本',
    dataIndex: 'version',
    render: (version) => DHCP_VERSION_ENUM[version] || version,
  },
  {
    title: '源IPv4',
    dataIndex: 'srcIpv4',
    align: 'center',
    searchable: true,
    fieldType: EFieldType.IPV4,
    operandType: EFieldOperandType.IPV4,
  },
  {
    title: '目的IPv4',
    dataIndex: 'destIpv4',
    align: 'center',
    searchable: true,
    fieldType: EFieldType.IPV4,
    operandType: EFieldOperandType.IPV4,
  },
  {
    title: '源MAC地址',
    dataIndex: 'srcMac',
    searchable: true,
  },
  {
    title: '目的MAC地址',
    dataIndex: 'destMac',
    searchable: true,
  },
  {
    title: '源端口',
    dataIndex: 'srcPort',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '目的端口',
    dataIndex: 'destPort',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    searchable: true,

    operandType: EFieldOperandType.ENUM,
    enumValue: [...DHCP_MESSAGE_TYPE_LIST],
    render: (messageType) => DHCP_MESSAGE_TYPE_ENUM[messageType] || messageType,
  },
  {
    title: '事务ID',
    dataIndex: 'transactionId',
    operandType: EFieldOperandType.NUMBER,
    searchable: true,
  },
  {
    title: '请求参数列表',
    dataIndex: 'parameters',
    searchable: true,
    fieldType: EFieldType.ARRAY,
    operandType: EFieldOperandType.NUMBER,
    render: (parameters: number[]) => parameters.join(','),
  },
  {
    title: '分配的IPv4地址',
    dataIndex: 'offeredIpv4Address',
    fieldType: EFieldType.IPV4,
    operandType: EFieldOperandType.IPV4,
    searchable: true,
  },
  {
    title: '请求字节数',
    dataIndex: 'upstreamBytes',
    operandType: EFieldOperandType.NUMBER,
    searchable: true,
  },
  {
    title: '应答字节数',
    dataIndex: 'downstreamBytes',
    operandType: EFieldOperandType.NUMBER,
    searchable: true,
  },
];

interface Props {
  paneTitle?: string;
}

const DHCPSpecifications = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.DHCP}
      tableColumns={tableColumns as any}
    />
  );
};

export default DHCPSpecifications;
