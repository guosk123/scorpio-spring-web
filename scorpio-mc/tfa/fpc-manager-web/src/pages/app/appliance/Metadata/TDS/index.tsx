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
];

const MetadataTDS = () => {
  const { pathname } = history.location;
  return (
    <Template
      entry={getEntryTag(pathname)}
      protocol={EMetadataProtocol.TDS}
      tableColumns={tableColumns}
    />
  );
};

export default MetadataTDS;
