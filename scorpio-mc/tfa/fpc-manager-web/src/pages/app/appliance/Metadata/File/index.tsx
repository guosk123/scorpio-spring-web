import type { ITableColumnProps } from '@/common/typings';
import { EFieldOperandType } from '@/components/FieldFilter/typings';
import { bytesToSize } from '@/utils/utils';
import { history } from 'umi';
import Template from '../components/Template';
import type { IMetadataFile } from '../typings';
import { EMetadataProtocol, FileRestoreStateLabel } from '../typings';
import { getEntryTag } from '../utils/entryTools';

export const tableColumns: ITableColumnProps<IMetadataFile>[] = [
  {
    title: '还原时间',
    dataIndex: 'timestamp',
  },
  {
    title: '文件名称',
    dataIndex: 'name',
    searchable: true,
  },
  {
    title: '应用层协议',
    dataIndex: 'l7Protocol',
    searchable: true,
  },
  {
    title: '文件大小',
    dataIndex: 'size',
    searchable: true,
    operandType: EFieldOperandType.NUMBER,
    render(dom, record) {
      const { size } = record;
      return bytesToSize(size);
    },
  },
  {
    title: '文件格式',
    dataIndex: 'magic',
    searchable: true,
  },
  {
    title: 'MD5',
    dataIndex: 'md5',
    searchable: true,
  },
  {
    title: 'SHA1',
    dataIndex: 'sha1',

    searchable: true,
  },
  {
    title: 'SHA256',
    dataIndex: 'sha256',
    searchable: true,
  },
  {
    title: '还原状态',
    dataIndex: 'state',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(FileRestoreStateLabel).map((key) => {
      return {
        value: key,
        text: FileRestoreStateLabel[key],
      };
    }),
    render: (dom, record) => {
      const { state } = record;
      return FileRestoreStateLabel[state];
    },
  },
];

const MetadataFile = () => {
  const location = history.location;

  return (
    <Template
      entry={getEntryTag(location.pathname)}
      timeField={'timestamp'}
      protocol={EMetadataProtocol.FILE}
      tableColumns={tableColumns}
      isNewIpFieldType={true}
    />
  );
};

export default MetadataFile;
