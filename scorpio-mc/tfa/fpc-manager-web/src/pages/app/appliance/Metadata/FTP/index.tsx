import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataFtp } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';

// export use for IOC
export const tableColumns: IColumnProps<IMetadataFtp>[] = [
  {
    dataIndex: 'dataChannelIp',
    title: '数据通道IP',
    searchable: true,
    fieldType: EFieldType.IP,
    operandType: EFieldOperandType.IP,
  },
  {
    dataIndex: 'dataChannelPort',
    title: '数据通道端口',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  {
    title: '登录用户',
    dataIndex: 'user',
    searchable: true,
  },
  {
    title: '操作序号',
    dataIndex: 'cmdSeq',
    searchable: true,
  },
  {
    title: '文件名',
    dataIndex: 'filename',
    searchable: true,
  },
  {
    title: '操作命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '操作结果',
    dataIndex: 'reply',
    searchable: true,
  },
];

const MetadataFtp = () => {
  const { pathname } = history.location;
  return (
    <Template<IMetadataFtp>
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.FTP}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataFtp;
