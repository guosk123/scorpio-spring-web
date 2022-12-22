import { ARP_TYPE_LIST, EARPType } from '@/common/app';
import { EFieldType, EFieldOperandType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataArp } from '../typings';
import { EMetadataProtocol } from '../typings';

export const tableColumns: IColumnProps<IMetadataArp>[] = [
  {
    title: '源IPv4',
    dataIndex: 'srcIp',
    align: 'center',
    searchable: false,
  },
  {
    title: '源IPv4',
    dataIndex: 'srcIpv4',
    show: false,
    searchable: true,
    fieldType: EFieldType.IPV4,
    operandType: EFieldOperandType.IPV4,
  },
  {
    title: '目的IPv4',
    dataIndex: 'destIp',
    align: 'center',
    searchable: false,
  },
  {
    title: '目的IPv4',
    dataIndex: 'destIpv4',
    show: false,
    searchable: true,
    fieldType: EFieldType.IPV4,
    operandType: EFieldOperandType.IPV4,
  },
  {
    title: '源MAC地址',
    dataIndex: 'srcMac',
    align: 'center',
    searchable: true,
  },
  {
    title: '目的MAC地址',
    dataIndex: 'destMac',
    align: 'center',
    searchable: true,
  },
  {
    title: '报文类型',
    dataIndex: 'type',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: ARP_TYPE_LIST,
    render: (text) => EARPType[text] || text,
  },
];

interface Props {
  paneTitle?: string;
}

const MetadataARP = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.ARP}
      tableColumns={tableColumns as any}
    />
  );
};

export default MetadataARP;
