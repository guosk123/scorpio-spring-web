import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataSsh } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';
import { beautifyText } from '../Mail';
import Ellipsis from '@/components/Ellipsis';

export const tableColumns: IColumnProps<IMetadataSsh>[] = [
  {
    title: '客户端版本',
    dataIndex: 'clientVersion',
    searchable: true,
  },
  {
    title: '客户端软件',
    dataIndex: 'clientSoftware',
    searchable: true,
  },
  {
    title: '客户端请求附带信息',
    dataIndex: 'clientComments',
    searchable: true,
  },
  {
    title: '服务器版本',
    dataIndex: 'serverVersion',
    searchable: true,
  },
  {
    title: '服务器软件',
    dataIndex: 'serverSoftware',
    searchable: true,
  },
  {
    title: '服务器应答附带信息',
    dataIndex: 'serverComments',
    searchable: true,
  },
  {
    title: '服务器密钥类',
    dataIndex: 'serverKeyType',
    searchable: true,
  },
  {
    title: '服务器密钥',
    dataIndex: 'serverKey',
    render: (text) => {
      return (
        <Ellipsis tooltip lines={3}>
          {beautifyText(text)}
        </Ellipsis>
      );
    },
  },
];

const MetadataSsh = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.SSH}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataSsh;
