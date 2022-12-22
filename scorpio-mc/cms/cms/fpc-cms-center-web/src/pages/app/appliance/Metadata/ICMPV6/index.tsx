import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol, LORToYN } from '../typings';

export const tableColumns = [
  {
    title: '详细信息',
    dataIndex: 'result',
  },
  {
    title: '只有请求',
    dataIndex: 'onlyRequest',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(LORToYN).map((key) => ({
      text: LORToYN[key],
      value: key,
    })),
    render: (text: string) => {
      if (text === undefined) {
        return undefined;
      }
      return LORToYN[String(text)];
    },
  },
  {
    title: '只有应答',
    dataIndex: 'onlyResponse',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(LORToYN).map((key) => ({
      text: LORToYN[key],
      value: key,
    })),
    render: (text: string) => {
      if (text === undefined) {
        return undefined;
      }
      return LORToYN[String(text)];
    },
  },
  {
    title: '请求应答payload hash不一致',
    dataIndex: 'payloadHashInconsistent',
    searchable: true,
    width: 240,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(LORToYN).map((key) => ({
      text: LORToYN[key],
      value: key,
    })),
    render: (text: string) => {
      if (text === undefined) {
        return undefined;
      }
      return LORToYN[String(text)];
    },
  },
];

interface Props {
  paneTitle?: string;
}

const MetadataIcmp = (props: Props) => {
  const { paneTitle } = props;
  return (
    <Template entry={paneTitle} protocol={EMetadataProtocol.ICMPV6} tableColumns={tableColumns} />
  );
};

export default MetadataIcmp;
