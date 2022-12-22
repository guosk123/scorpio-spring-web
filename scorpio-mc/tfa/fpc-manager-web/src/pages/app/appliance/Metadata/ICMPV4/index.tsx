import Template from '../components/Template';
import { EMetadataProtocol, LORToYN } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';
import { beautifyText } from '../Mail';
import { EFieldOperandType } from '@/components/FieldFilter/typings';

export const tableColumns = [
  {
    title: '详细信息',
    dataIndex: 'result',
    render: (text: any) => beautifyText(text),
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

const MetadataIcmp = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.ICMPV4}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataIcmp;
