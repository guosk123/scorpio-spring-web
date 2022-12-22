import Template, { IColumnProps } from '../components/Template';
import {
  EMetadataProtocol,
  EOpType,
  EResStatus,
  IMetadataLdap,
  opTypeMap,
  resStatusMap,
} from '../typings';
import { history } from 'umi';
import { getEntryTag } from '../utils/entryTools';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
const tableColumns: IColumnProps<IMetadataLdap>[] = [
  {
    title: '请求类型',
    dataIndex: 'opType',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(EOpType).map((key) => ({
      text: key,
      value: EOpType[key],
    })),
    render: (text) => {
      return opTypeMap.get(`${text}`);
    },
  },
  {
    title: '请求内容',
    dataIndex: 'reqContent',
    searchable: true,
    fieldType: EFieldType.Map,
  },
  {
    title: '回复内容',
    dataIndex: 'resContent',
    searchable: true,
    fieldType: EFieldType.Map,
  },
  {
    title: '回复状态',
    dataIndex: 'resStatus',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    enumValue: Object.keys(EResStatus).map((key) => ({
      text: key,
      value: EResStatus[key],
    })),
    render: (text: string) => {
      return resStatusMap.get(`${text}`);
    },
  },
];

const MetadataHttp = () => {
  const { pathname } = history.location;
  return (
    <>
      <Template<IMetadataLdap>
        entry={getEntryTag(pathname)}
        protocol={EMetadataProtocol.LDAP}
        tableColumns={tableColumns}
        isNewIpFieldType={true}
      />
    </>
  );
};

export default MetadataHttp;
