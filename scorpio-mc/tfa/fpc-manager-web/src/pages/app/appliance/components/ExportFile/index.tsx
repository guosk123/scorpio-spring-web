import type { XLSXBookType } from '@/components/ExportXlsx';
import type { IUriParams } from '@/pages/app/analysis/typings';
import AccessBox from '@/pages/frame/system/MenuEdit/components/AccessBox';
import useComponentsAccess, {
  IGNORE_KEY,
} from '@/pages/frame/system/MenuEdit/hooks/useComponentsAccess';
import { ExportOutlined } from '@ant-design/icons';
import { Alert, Button, Dropdown, InputNumber, Menu, message, Modal } from 'antd';
import { useState } from 'react';
import { useParams } from 'umi';
import { exportFlowRecords } from '../../FlowRecord/service';
import type { IQueryRecordParams } from '../../FlowRecord/typings';
import { exportMetadataLogs } from '../../Metadata/service';
import { EMetadataProtocol } from '../../Metadata/typings';
import { EQueryLogToPkt } from '../DownLoadPktBtn';

export async function queryExportFile(
  params: IQueryRecordParams & IExportParams,
  type: EQueryLogToPkt,
  metadataType?: EMetadataProtocol,
) {
  let urlType = metadataType;
  if (
    [EMetadataProtocol.POP3, EMetadataProtocol.IMAP, EMetadataProtocol.SMTP].includes(
      metadataType || ('' as any),
    )
  ) {
    urlType = EMetadataProtocol.MAIL;
  } else if (metadataType === EMetadataProtocol.DHCPV6) {
    urlType = EMetadataProtocol.DHCP;
  } else if (
    [EMetadataProtocol.ICMPV4, EMetadataProtocol.ICMPV6].includes(metadataType || ('' as any))
  ) {
    urlType = EMetadataProtocol.ICMP;
  }
  if (type === EQueryLogToPkt.FlowLog) {
    exportFlowRecords(params);
  } else if (type === EQueryLogToPkt.MetaData && urlType) {
    exportMetadataLogs({ protocol: urlType, ...(

      params as any) });
  }
}

export enum EFileType {
  EXCEL = 'excel',
  CSV = 'csv',
}

export interface IExportParams {
  fileType: XLSXBookType;
}

export const BIG_EXPORT_FILE_COUNT = 10000;
export const MAX_EXPORT_FILE_COUNT = 100000;

interface Props {
  loading: boolean;
  totalNum: number;
  accessKey?: string;
  queryFn: (...args: any[]) => Promise<any>;
}

export default function ExportFile(props: Props) {
  const {
    loading,
    totalNum,
    queryFn = () => new Promise(() => {}),
    // accessKey = 'exportBtn',
    accessKey = IGNORE_KEY,
  } = props;
  const [count, setCount] = useState<any>();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const isSobig = totalNum > BIG_EXPORT_FILE_COUNT;
  const [fileType, setFileType] = useState<EFileType | undefined>();
  const { networkId, serviceId, pcapFileId }: IUriParams = useParams();
  const access = useComponentsAccess(accessKey);
  // 使用自己的map鉴权
  // const access = useAccess().hasUserPerm();

  const handleExport = (key: EFileType | undefined) => {
    queryFn({
      fileType: key,
      count,
      networkId: networkId,
      packetFileId: pcapFileId,
      serviceId,
    }).then((res: any) => {
      setCount(undefined);
      if (res?.success) {
        // window.open(result);
        message.info('导出成功');
      }
    });
  };

  const handleOk = () => {
    handleExport(fileType);
    setIsModalVisible(false);
  };

  return (
    <AccessBox access={access ? true : false}>
      <Dropdown
        disabled={totalNum === 0}
        overlay={
          <Menu
            onClick={(e) => {
              if (!isSobig) {
                handleExport(e.key as EFileType);
              } else {
                setFileType(e.key as EFileType);
                setIsModalVisible(true);
              }
            }}
          >
            <Menu.Item key={EFileType.CSV}>导出 CSV 文件</Menu.Item>
            <Menu.Item key={EFileType.EXCEL}>导出 Excel 文件</Menu.Item>
          </Menu>
        }
        trigger={['click']}
      >
        <Button icon={<ExportOutlined />} type="primary" loading={loading}>
          导出
        </Button>
      </Dropdown>
      <Modal
        title="提示"
        onOk={handleOk}
        visible={isModalVisible}
        maskClosable={false}
        destroyOnClose={true}
        okButtonProps={{ disabled: !count }}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        <Alert
          message={`当前数据量较大，请输入导出条数（最大导出${MAX_EXPORT_FILE_COUNT}条）`}
          banner
        />
        <div style={{ paddingTop: 20 }}>
          <span>导出条数：</span>
          <InputNumber
            size="small"
            style={{ width: 360 }}
            min={1}
            max={MAX_EXPORT_FILE_COUNT}
            placeholder={'请指定导出条数'}
            onChange={(v) => {
              setCount(v);
            }}
          />
        </div>
      </Modal>
    </AccessBox>
  );
}
