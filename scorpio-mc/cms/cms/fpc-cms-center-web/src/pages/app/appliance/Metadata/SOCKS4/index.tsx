import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { history } from 'umi';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataSocks4} from '../typings';
import { LORToText } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';

export const tableColumns: IColumnProps<IMetadataSocks4>[] = [
  {
    title: '请求命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    searchable: true,
  },
  {
    title: '远端域名',
    dataIndex: 'domainName',
    searchable: true,
  },
  {
    title: '结果',
    dataIndex: 'cmdResult',
    searchable: true,
  },
  {
    title: '连接状态',
    dataIndex: 'channelState',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(LORToText).map((key) => ({
      text: LORToText[key],
      value: key,
    })),
    render: (text) => {
      return LORToText[String(text)];
    },
  },
  {
    title: '远端IP',
    dataIndex: 'requestRemoteIp',
    searchable: true,
    fieldType: EFieldType.IP,
    operandType: EFieldOperandType.IP,
  },
  {
    title: '远端端口',
    dataIndex: 'requestRemotePort',
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },

  {
    title: '服务器返回的远端IP',
    dataIndex: 'responseRemoteIp',
    searchable: true,
    fieldType: EFieldType.IP,
    operandType: EFieldOperandType.IP,
  },
  {
    title: '服务器返回的远端端口',
    dataIndex: 'responseRemotePort',
    searchable: true,
    operandType: EFieldOperandType.PORT,
  },
];

const MetadataSocks4 = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.SOCKS4}
      tableColumns={tableColumns}
      isNewIpFieldType={true}
    />
  );
};
export default MetadataSocks4;
