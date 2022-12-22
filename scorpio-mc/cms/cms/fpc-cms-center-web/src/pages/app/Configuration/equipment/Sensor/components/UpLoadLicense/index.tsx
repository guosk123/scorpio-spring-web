import SETTINGS from '@/common/applicationConfig';
import { getCookie } from '@/utils/frame/cookie';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, message, Modal, notification, Upload } from 'antd';
import type { RcFile, UploadChangeParam, UploadFile } from 'antd/es/upload/interface';
import { Fragment, useState } from 'react';
import { connect } from 'umi';

const uploadProps = {
  name: 'file',
  // listType: 'picture-card' as any,
  action: `${SETTINGS.API_BASE_URL}${SETTINGS.API_VERSION_PRODUCT_V1}/central/fpc-devices/licenses`,
  showUploadList: false,
  withCredentials: true,
  headers: {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  },
  accept: '.txt,.bin',
};

interface Props {
  id: string;
  disabled?: boolean;
}

const UpLoadLicense = (props: Props) => {
  const { id, disabled } = props;
  const [uploadLoading, setUploadLoading] = useState<boolean>(false);

  const handleBeforeUpload = (file: RcFile) => {
    const fileSplit = file.name.split('.');
    const fileType = fileSplit.pop()!;

    // 文件名后缀转大写
    const fileType2UpperCase = fileType.toLocaleUpperCase();
    // 1. 后缀校验：仅支持txt和bin两种格式
    if (fileType2UpperCase !== 'TXT' && fileType2UpperCase !== 'BIN') {
      message.error('文件后缀不支持');
      return false;
    }

    // 2. 校验文件大小
    if (file.size <= 0) {
      message.error('文件内容不能为空');
      return false;
    }

    const isLt100K = file.size / 1024 <= 100;
    if (!isLt100K) {
      message.error('文件大小不能超过100KB');
      return false;
    }

    return true;
  };

  const handleUpload = (info: UploadChangeParam<UploadFile<any>>) => {
    if (info.file.status === 'uploading') {
      setUploadLoading(true);
      return;
    }

    if (info.file.status === 'done') {
      message.success('上传成功');
    } else if (info.file.status === 'error') {
      message.error('上传失败');
      const { response } = info.file;
      notification.error({
        message: '出现了一个问题',
        description: response.message || 'License文件上传失败',
      });
    }

    setUploadLoading(false);
  };

  const [isModalVisible, setIsModalVisible] = useState(false);

  return (
    <Fragment>
      <Button
        type="link"
        size="small"
        onClick={() => {
          setIsModalVisible(true);
        }}
        disabled={disabled}
      >
        License
      </Button>
      <Modal
        title="上传License"
        visible={isModalVisible}
        footer={null}
        onCancel={() => {
          setIsModalVisible(false);
        }}
      >
        {/* 上传 */}
        <div style={{ display: 'flex' }}>
          <div
            style={{
              width: 180,
              display: 'flex',
              alignItems: 'center',
              border: '1px dashed #d9d9d9',
              paddingLeft: 20,
              cursor: 'pointer',
            }}
          >
            <Upload
              disabled={uploadLoading}
              {...uploadProps}
              beforeUpload={handleBeforeUpload}
              onChange={handleUpload}
              data={{
                id,
              }}
            >
              <div style={{ width: 180, height: 58, display: 'flex', alignItems: 'center' }}>
                {uploadLoading ? (
                  <LoadingOutlined style={{ fontSize: 16 }} />
                ) : (
                  <PlusOutlined style={{ fontSize: 16 }} />
                )}
                {uploadLoading ? '上传中...' : '上传License文件'}
              </div>
            </Upload>
          </div>

          <div>
            <ul>
              <li>
                文件后缀仅支持 <b>txt</b>、<b>bin</b> 两种；
              </li>
              <li>
                文件大小不能超过 <b>10KB</b>。
              </li>
            </ul>
          </div>
        </div>
      </Modal>
    </Fragment>
  );
};

export default connect()(UpLoadLicense);
