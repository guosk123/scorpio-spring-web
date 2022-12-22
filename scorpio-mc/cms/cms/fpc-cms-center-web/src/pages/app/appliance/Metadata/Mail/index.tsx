import Ellipsis from '@/components/Ellipsis';
import EllipsisDiv from '@/components/EllipsisDiv';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type { IColumnProps } from '../components/Template';
import Template from '../components/Template';
import type { IMetadataMail } from '../typings';
import { EMetadataProtocol } from '../typings';
import { getEntryTag } from '../utils/entryTools';

const beautifyEmail = (email: string) => {
  return (
    <Ellipsis tooltip lines={1}>
      {email}
    </Ellipsis>
  );
};

const beautifyText = (text: string) => {
  return (
    <Ellipsis tooltip lines={1}>
      {text}
    </Ellipsis>
  );
};

export const ellipsisText = (text: string) => {
  return (
    <EllipsisDiv>
      {text}
    </EllipsisDiv>
  );
};

export const LEVEL_MAIL_TSL_MAP = {
  '1': '密文',
  '0': '明文',
};

export const tableColumns: IColumnProps<IMetadataMail>[] = [
  {
    title: '邮件ID',
    dataIndex: 'messageId',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '协议',
    dataIndex: 'protocol',
    searchable: false,
  },
  {
    title: '发送日期',
    dataIndex: 'date',
    searchable: true,
    render: (text) => beautifyText(text),
  },
  {
    title: '邮件主题',
    dataIndex: 'subject',
    render: (text) => beautifyEmail(text),
    searchable: true,
  },
  {
    title: '发件人',
    dataIndex: 'from',
    render: (from) => (
      <Ellipsis tooltip lines={1}>
        {from}
      </Ellipsis>
    ),
    searchable: true,
  },
  {
    title: '收件人',
    dataIndex: 'to',
    render: (text) => beautifyEmail(text),
    searchable: true,
  },
  {
    title: '抄送',
    dataIndex: 'cc',
    render: (text) => beautifyEmail(text),
    searchable: true,
  },
  {
    title: '密送',
    dataIndex: 'bcc',
    render: (text) => beautifyEmail(text),
    searchable: true,
  },
  {
    title: 'URL',
    dataIndex: 'urlList',
    searchable: true,
    fieldType: EFieldType.ARRAY,
    operandType: EFieldOperandType.STRING,
    render: (parameters: string[]) => {
      return (parameters || []).join(',');
    },
  },
  {
    title: '加密方式',
    dataIndex: 'decrypted',
    searchable: true,
    operandType: EFieldOperandType.ENUM,
    render: (decrypted) => LEVEL_MAIL_TSL_MAP[decrypted],
    enumValue: Object.keys(LEVEL_MAIL_TSL_MAP).map((key) => ({
      text: LEVEL_MAIL_TSL_MAP[key],
      value: key,
    })),
  },
  {
    title: '附件名称',
    dataIndex: 'attachment',
    render: (text) => {
      if (!text) return '';
      const attachment = text;
      const split = attachment.split(';');
      const fileNames = split
        .filter((item: any) => item.length > 0)
        .map((item: any) => {
          const fileName = item.split(':')[0];
          return fileName;
        });

      return (
        <Ellipsis tooltip lines={1}>
          {fileNames.join(',')}
        </Ellipsis>
      );
    },
    searchable: true,
  },
];

const MetadataMail = ({ protocol, entry }: { protocol: EMetadataProtocol; entry: string }) => {
  // @ts-ignore
  return <Template entry = { entry || getEntryTag(location.pathname)} protocol={ protocol || EMetadataProtocol.MAIL} tableColumns={tableColumns} />;
};

export default MetadataMail;
