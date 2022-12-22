import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol, LORToText } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const tableColumns = [
  {
    title: '用户名',
    dataIndex: 'username',
    searchable: true,
  },
  // {
  //   title: '密码',
  //   dataIndex: 'password',
  //   searchable: true,
  // },
  {
    title: '验证方式',
    dataIndex: 'authMethod',
    searchable: true,
  },
  {
    title: '验证结果',
    dataIndex: 'authResult',
    searchable: true,
  },
  {
    title: '操作命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '地址类型',
    dataIndex: 'atyp',
    searchable: true,
  },
  {
    title: '请求服务器地址',
    dataIndex: 'bindAddr',
    searchable: true,
  },
  {
    title: '请求服务器端口',
    dataIndex: 'bindPort',
    operandType: EFieldOperandType.NUMBER,
    searchable: true,
  },
  {
    title: '执行结果',
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
    render: (text: string) => {
      return LORToText[String(text)];
    },
  },
];

const MetadataSocks5 = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.SOCKS5}
      tableColumns={tableColumns}
    />
  );
};
export default MetadataSocks5;
