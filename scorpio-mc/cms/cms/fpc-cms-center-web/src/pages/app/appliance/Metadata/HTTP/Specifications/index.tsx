import { EHttpAuthType, HTTP_AUTH_TYPE_LIST, OS_VERSION_LIST } from '@/common/app';
import { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import type {
  IpAddressGroup
} from '@/pages/app/Configuration/IpAddressGroup/typings';
import { Tooltip, Typography } from 'antd';
import { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { connect, getDvaApp } from 'umi';
import type { IColumnProps } from '../../components/Template';
import Template from '../../components/Template';
import type { IMetadataHttp } from '../../typings';
import { EMetadataProtocol, FILE_FLAG_MAP, HTTP_DECRYPTED_MAP, LORToText } from '../../typings';

export const tableColumns: () => IColumnProps<IMetadataHttp>[] = () => {
  const { allIpAddressGroupList, allIpAddressGroupMap } =
    getDvaApp()._store.getState().ipAddressGroupModel;

  const ipAddressGroupList = (allIpAddressGroupList as IpAddressGroup[])
    .map((data) => ({
      text: data.name,
      value: data.id,
    }))
    .concat({
      text: '其他',
      value: '',
    });

  return [
    {
      title: 'URL',
      dataIndex: 'uri',
      searchable: true,
    },
    {
      title: '请求方法',
      dataIndex: 'method',
      searchable: true,
    },
    {
      title: 'status',
      dataIndex: 'status',
      searchable: true,
    },

    {
      title: 'Accept-Encoding',
      dataIndex: 'acceptEncoding',
      searchable: true,
    },
    {
      title: 'Accept-Language',
      dataIndex: 'acceptLanguage',
      searchable: true,
    },
    {
      title: 'cookie',
      dataIndex: 'cookie',
      searchable: true,
    },
    {
      title: 'host',
      dataIndex: 'host',
      searchable: true,
    },
    {
      title: 'origin',
      dataIndex: 'origin',
      searchable: true,
    },
    {
      title: 'referer',
      dataIndex: 'referer',
      searchable: true,
    },
    {
      title: 'HTTP Proxy 连接状态',
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
      title: 'xff',
      dataIndex: 'xff',
      searchable: true,
    },
    {
      title: '头部xff',
      dataIndex: 'xffFirst',
      searchable: true,
      operandType: EFieldOperandType.IP,
      fieldType: EFieldType.IP,
    },
    {
      title: '尾部xff',
      dataIndex: 'xffLast',
      searchable: true,
      operandType: EFieldOperandType.IP,
      fieldType: EFieldType.IP,
    },
    {
      title: '头部xff-地址组',
      dataIndex: 'xffFirstAlias',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: ipAddressGroupList,
      render: (xffFirstAlias) => {
        return allIpAddressGroupMap[xffFirstAlias]?.name || xffFirstAlias || '其他';
      },
    },
    {
      title: '尾部xff-地址组',
      dataIndex: 'xffLastAlias',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: ipAddressGroupList,
      render: (xffLastAlias) => {
        return allIpAddressGroupMap[xffLastAlias]?.name || xffLastAlias || '其他';
      },
    },
    {
      title: 'HTTP请求头',
      dataIndex: 'requestHeader',
      searchable: true,
      render: (text) => {
        if (!text) {
          return '';
        }
        return (
          <Tooltip
            placement="bottomLeft"
            title={text}
            zIndex={800}
            overlayInnerStyle={{ width: 500, overflow: 'auto', maxHeight: 300 }}
          >
            <Typography.Text
              style={{ width: 160, cursor: 'pointer' }}
              ellipsis={{
                tooltip: false,
              }}
            >
              {text}
            </Typography.Text>
          </Tooltip>
        );
      },
    },
    {
      title: 'Content-Type',
      dataIndex: 'contentType',
      searchable: true,
    },
    {
      title: 'User-Agent',
      dataIndex: 'userAgent',
      searchable: true,
    },
    {
      title: '操作系统',
      dataIndex: 'osVersion',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: OS_VERSION_LIST.map((version) => ({
        text: version,
        value: version,
      })),
    },
    {
      title: '重定向地址',
      dataIndex: 'location',
      searchable: true,
    },
    {
      title: 'set_cookie',
      dataIndex: 'setCookie',
      searchable: true,
    },

    {
      title: '文件传输方式',
      dataIndex: 'fileFlag',
      render: (fileFlag) => FILE_FLAG_MAP[fileFlag],
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: Object.keys(FILE_FLAG_MAP).map((key) => ({
        text: FILE_FLAG_MAP[key],
        value: key,
      })),
    },
    {
      title: '传输文件名称',
      dataIndex: 'fileName',
      searchable: true,
    },
    {
      title: '传输文件类型',
      dataIndex: 'fileType',
      searchable: true,
    },
    {
      title: 'Authorization',
      dataIndex: 'authorization',
      searchable: true,
    },
    {
      title: '认证方式',
      dataIndex: 'authType',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: HTTP_AUTH_TYPE_LIST,

      render: (authType) => EHttpAuthType[authType] || authType,
    },
    {
      title: '加密方式',
      dataIndex: 'decrypted',
      align: 'center',
      searchable: true,
      operandType: EFieldOperandType.ENUM,
      enumValue: Object.keys(HTTP_DECRYPTED_MAP).map((key) => ({
        text: HTTP_DECRYPTED_MAP[key],
        value: key,
      })),
      render: (decrypted) => HTTP_DECRYPTED_MAP[decrypted],
    },
  ];
};

interface Props {
  paneTitle?: string;
  dispatch: Dispatch;
}

function HttpSpecifications(props: Props) {
  const { dispatch, paneTitle } = props;

  useEffect(() => {
    dispatch({
      type: 'ipAddressGroupModel/queryAllIpAddressGroup',
    });
  }, [dispatch]);

  return (
    <Template
      entry={paneTitle}
      protocol={EMetadataProtocol.HTTP}
      tableColumns={tableColumns() as any}
    />
  );
}
export default connect()(HttpSpecifications);
