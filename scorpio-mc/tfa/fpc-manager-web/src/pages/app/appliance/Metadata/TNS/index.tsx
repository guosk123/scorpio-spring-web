import { EFieldOperandType } from '@/components/FieldFilter/typings';
import Template from '../components/Template';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';
import { history } from 'umi';

export const tableColumns = [
  {
    title: 'sql命令',
    dataIndex: 'cmd',
    searchable: true,
  },
  {
    title: '错误',
    dataIndex: 'error',
    searchable: true,
  },
  {
    title: '响应时间(ms)',
    dataIndex: 'delaytime',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
  },
  // {
  //   title: '数据库版本',
  //   dataIndex: 'version',
  //   searchable: true,
  // },
  // {
  //   title: '连接信息',
  //   dataIndex: 'connectData',
  // },
  // {
  //   title: '连接结果',
  //   dataIndex: 'connectResult',
  // },
];

const MetadataTns = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.TNS}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataTns;
