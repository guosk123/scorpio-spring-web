import SETTINGS from '@/common/applicationConfig';
import type { ConnectState } from '@/models/connect';
import { getCookie } from '@/utils/frame/cookie';
import {
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  LoadingOutlined,
  PlusOutlined,
  StopOutlined,
} from '@ant-design/icons';
import { Card, Descriptions, message, notification, Skeleton, Upload } from 'antd';
import type { RcFile, UploadChangeParam, UploadFile } from 'antd/es/upload/interface';
import moment from 'moment';
import { useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'umi';
import licenseCommercialImage from './assets/commercial.svg';
import licenseDemoImage from './assets/demo.svg';
import styles from './index.less';
import type { LicenseStateType } from './model';
import type { ILicenseInfo } from './typings';
import { ELicenseType } from './typings';

const uploadProps = {
  name: 'file',
  listType: 'picture-card' as any,
  action: `${SETTINGS.API_BASE_URL}${SETTINGS.API_VERSION_PRODUCT_V1}/system/licenses`,
  showUploadList: false,
  withCredentials: true,
  headers: {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  },
  accept: '.txt,.bin',
};

const Countdown = ({ license }: { license: ILicenseInfo }) => {
  const { expiryTime } = license;

  if (!expiryTime) {
    return <span>--</span>;
  }

  // 服务器时间
  const serverTime = moment(window.systemTime);
  const diffDays = moment(expiryTime).diff(serverTime, 'days', true);

  // 已失效
  if (diffDays <= 0) {
    return (
      <span className={styles.warning}>
        <StopOutlined /> 已过期
      </span>
    );
  }

  // 不足30天
  const fixDays = Math.floor(diffDays);
  if (fixDays <= 30) {
    return (
      <span className={styles.warning}>
        <ExclamationCircleOutlined /> {fixDays} 天
      </span>
    );
  }

  // 正常
  return (
    <span className={styles.normal}>
      <CheckCircleOutlined /> {fixDays} 天
    </span>
  );
};

const LicenseMeta = ({ license }: { license: ILicenseInfo }) => {
  const { localSerialNo, signTime, licenseType, expiryTime } = license;

  return (
    <div className={styles.meta}>
      <div className={styles.left}>
        <Descriptions size="small" column={1}>
          <Descriptions.Item label="本机序列号">
            <span style={{ wordBreak: 'break-all' }}>{localSerialNo}</span>
          </Descriptions.Item>
          <Descriptions.Item label="授权时间">
            {signTime ? moment(signTime).format('YYYY-MM-DD HH:mm:ss') : '--'}
          </Descriptions.Item>
          <Descriptions.Item label="有效时间">
            {expiryTime ? moment(expiryTime).format('YYYY-MM-DD HH:mm:ss') : '--'}
          </Descriptions.Item>
          <Descriptions.Item label="剩余时间">
            <Countdown license={license} />
          </Descriptions.Item>
        </Descriptions>
      </div>
      <div className={styles.right}>
        {licenseType ? (
          <div>
            <img
              alt="license 类型"
              src={licenseType === ELicenseType.Demo ? licenseDemoImage : licenseCommercialImage}
            />
            <p>{licenseType === ELicenseType.Demo ? '测试演示版' : '正式商用版'}</p>
          </div>
        ) : (
          <div className={styles.empty}>
            <p>
              <ExclamationCircleOutlined /> 未找到有效的 License 文件
            </p>
            <p>License 文件上传之前，系统功能将无法正常使用</p>
          </div>
        )}
      </div>
    </div>
  );
};

interface ILicenseProps {
  dispatch: Dispatch;
  license: ILicenseInfo;
  systemTime: string;
  queryLicenseLoading?: boolean;
}

const License = ({ dispatch, license, queryLicenseLoading }: ILicenseProps) => {
  const [uploadLoading, setUploadLoading] = useState<boolean>(false);

  const queryLicense = () => {
    dispatch({
      type: 'licenseModel/queryLicense',
    });
  };

  useEffect(() => {
    queryLicense();
  }, []);

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
      queryLicense();
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

  return (
    <Card bordered={false}>
      {/* 上传 */}
      <div className={styles.uploadWrap}>
        <Upload
          disabled={uploadLoading}
          {...uploadProps}
          beforeUpload={handleBeforeUpload}
          onChange={handleUpload}
        >
          {uploadLoading ? (
            <LoadingOutlined style={{ fontSize: 20 }} />
          ) : (
            <PlusOutlined style={{ fontSize: 20 }} />
          )}
          <div className={styles.uploadText}>{uploadLoading ? '上传中...' : '上传License文件'}</div>
        </Upload>

        <div className={styles.warningInfoWrap}>
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
      {/* 已授权的节点序列号 */}
      <Skeleton active loading={queryLicenseLoading}>
        <LicenseMeta license={license} />
      </Skeleton>
    </Card>
  );
};

export default connect(
  ({ licenseModel: { license }, loading }: ConnectState & { licenseModel: LicenseStateType }) => ({
    license,
    queryLicenseLoading: loading.effects['licenseModel/queryLicense'],
  }),
)(License);
