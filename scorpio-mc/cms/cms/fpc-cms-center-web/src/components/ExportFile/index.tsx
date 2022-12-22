import { ExportOutlined } from '@ant-design/icons';
import { Alert, Button, Dropdown, InputNumber, Menu, message, Modal } from 'antd';
import { useState } from 'react';

export enum EFileType {
  EXCEL = 'excel',
  CSV = 'csv',
}

const FILE_TYPE_LABEL: Record<EFileType, string> = {
  [EFileType.EXCEL]: 'Excel',
  [EFileType.CSV]: 'CSV',
};

export const BIG_EXPORT_FILE_COUNT = 10000;
export const MAX_EXPORT_FILE_COUNT = 100000;

interface Props {
  loading: boolean;
  totalNum: number;
  queryFn: (params: { fileType: EFileType; count?: number }) => Promise<any>;
  /** 是否限制最多导出xxxx, 默认为true */
  limit?: boolean;
  /** 导出支持的格式： 默认csv,excel */
  supportFile?: EFileType[];
}

export default function ExportFile(props: Props) {
  const {
    loading,
    totalNum,
    queryFn = () => new Promise(() => {}),
    limit = true,
    supportFile = ['csv', 'excel'],
  } = props;
  const [count, setCount] = useState<number>();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const isSobig = totalNum > BIG_EXPORT_FILE_COUNT;
  const [fileType, setFileType] = useState<EFileType>();

  // 使用自己的map鉴权
  // const access = useAccess().hasUserPerm();

  const handleExport = (key: EFileType) => {
    queryFn({
      fileType: key,
      count,
    }).then((res: any) => {
      setCount(undefined);
      if (res?.success) {
        // window.open(result);
        message.info('导出成功');
      }
    });
  };

  const handleOk = () => {
    if (fileType) {
      handleExport(fileType);
      setIsModalVisible(false);
    }
  };

  return (
    <>
      <Dropdown
        disabled={totalNum === 0}
        overlay={
          <Menu
            onClick={(e) => {
              if (!limit || !isSobig) {
                handleExport(e.key as EFileType);
              } else {
                setFileType(e.key as EFileType);
                setIsModalVisible(true);
              }
            }}
          >
            {supportFile.map((type) => {
              return (
                <Menu.Item key={type} style={{ textTransform: 'capitalize' }}>
                  导出{FILE_TYPE_LABEL[type]}文件
                </Menu.Item>
              );
            })}
          </Menu>
        }
        trigger={['click']}
      >
        <Button icon={<ExportOutlined />} type="primary" loading={loading}>
          导出
        </Button>
      </Dropdown>
      {limit && (
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
      )}
    </>
  );
}
