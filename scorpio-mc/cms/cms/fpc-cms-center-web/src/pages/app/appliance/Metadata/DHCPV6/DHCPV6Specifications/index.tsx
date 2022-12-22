import {
  DHCP_V6_MESSAGE_TYPE_ENUM,
  DHCP_V6_MESSAGE_TYPE_LIST,
  DHCP_VERSION_ENUM,
} from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../../components/Template';
import Template from '../../components/Template';
import type { IMetadataDhcp } from '../../typings';
import { EMetadataProtocol } from '../../typings';

export const tableColumns: IColumnProps<IMetadataDhcp>[] = [
  {
    title: 'DHCP版本',
    dataIndex: 'version',
    // operandType: EFieldOperandType.ENUM,
    // enumValue: Object.keys(DHCP_VERSION_ENUM).map((key) => ({
    //   text: DHCP_VERSION_ENUM[key],
    //   value: key,
    // })),
    render: (version) => DHCP_VERSION_ENUM[version] || version,
  },
  {
    title: '源IPv6',
    dataIndex: 'srcIpv6',
    align: 'center',
    searchable: true,
    fieldType: EFieldType.IPV6,
    operandType: EFieldOperandType.IPV6,
  },
  {
    title: '目的IPv6',
    dataIndex: 'destIpv6',
    align: 'center',

    searchable: true,
    fieldType: EFieldType.IPV6,
    operandType: EFieldOperandType.IPV6,
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
    enumValue: [...DHCP_V6_MESSAGE_TYPE_LIST],
    render: (messageType) => DHCP_V6_MESSAGE_TYPE_ENUM[messageType] || messageType,
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
    fieldType: EFieldType.ARRAY,
    operandType: EFieldOperandType.NUMBER,
    render: (parameters: number[]) => parameters.join(','),
  },
  {
    title: '分配的IPv6地址',
    dataIndex: 'offeredIpv6Address',
    searchable: true,
    fieldType: EFieldType.IPV6,
    operandType: EFieldOperandType.IPV6,
  },
  {
    title: '请求字节数',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    dataIndex: 'upstreamBytes',
  },
  {
    title: '应答字节数',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    dataIndex: 'downstreamBytes',
  },
];

interface Props {
  paneTitle?: string;
}

const DHCPV6Specifications = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.DHCPV6}
      tableColumns={tableColumns as any}
    />
  );
};

export default DHCPV6Specifications;
