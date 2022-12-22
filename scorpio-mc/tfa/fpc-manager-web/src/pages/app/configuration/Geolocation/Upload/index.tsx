import { API_BASE_URL } from '@/common/applicationConfig';
import type { ConnectState } from '@/models/connect';
import { getCookie } from '@/utils/frame/cookie';
import { ExclamationCircleOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { Card, Descriptions, message, notification, Skeleton, Upload } from 'antd';
import type { RcFile, UploadChangeParam, UploadProps } from 'antd/lib/upload';
import { connect } from 'dva';
import moment from 'moment';
import React, { Fragment, memo, useEffect, useState } from 'react';
import type { Dispatch } from 'umi';
import ConnectCmsState from '../../components/ConnectCmsState';
import type { IGeolocationKnowledge } from '../typings';
import styles from './index.less';

const uploadProps: UploadProps = {
  name: 'file',
  listType: 'picture-card',
  action: `${API_BASE_URL}/fpc-v1/appliance/geolocation/knowledges`,
  showUploadList: false,
  withCredentials: true,
  headers: {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  },
  accept: '.txt', // 只支持 text 文件
};

const MetaInfo = memo((knowledgeInfo: IGeolocationKnowledge) => {
  const { releaseDate, uploadDate, version } = knowledgeInfo;
  if (version) {
    return (
      <Fragment>
        <div className={styles.left}>
          <Descriptions size="small" column={1}>
            <Descriptions.Item label="当前版本">{version}</Descriptions.Item>
            <Descriptions.Item label="发布时间">
              {releaseDate ? moment(releaseDate).format('YYYY-MM-DD HH:mm:ss') : '--'}
            </Descriptions.Item>
            <Descriptions.Item label="导入时间">
              {uploadDate ? moment(uploadDate).format('YYYY-MM-DD HH:mm:ss') : '--'}
            </Descriptions.Item>
          </Descriptions>
        </div>
      </Fragment>
    );
  }
  return (
    <div className={styles.empty}>
      <p>
        <ExclamationCircleOutlined /> 未找到有效的地区库
      </p>
      <p>地区库上传之前，地区识别功能将无法正常使用</p>
    </div>
  );
});

interface IGeoUploadProps {
  dispatch: Dispatch;
  queryLoading?: boolean;
  geolocationKnowledge: IGeolocationKnowledge;
}
const GeoUpload: React.FC<IGeoUploadProps> = ({ dispatch, geolocationKnowledge, queryLoading }) => {
  const [uploadLoading, setUploadLoading] = useState<boolean>(false);

  useEffect(() => {
    queryKnowledgeInfo();
  }, []);

  const handleBeforeUpload = (file: RcFile) => {
    const fileSplit = file.name.split('.');
    const fileType = fileSplit[fileSplit.length - 1];

    // 文件名后缀转大写
    const fileType2UpperCase = fileType.toLocaleUpperCase();
    // 1. 后缀校验：仅支持txt和bin两种格式
    if (fileType2UpperCase !== 'TXT') {
      message.error('文件后缀不支持');
      return false;
    }

    // 2. 校验文件大小
    if (file.size <= 0) {
      message.error('文件内容不能为空');
      return false;
    }

    const isLt50M = file.size / 1024 / 1024 <= 50;
    if (!isLt50M) {
      message.error('文件大小不能超过50MB');
      return false;
    }

    return true;
  };

  const handleUpload = (info: UploadChangeParam) => {
    if (info.file.status === 'uploading') {
      setUploadLoading(true);
      return;
    }

    if (info.file.status === 'done') {
      message.success('上传成功');
      queryKnowledgeInfo();
      // 重新拉取地理列表
      queryGeolocations();
    } else if (info.file.status === 'error') {
      message.error('上传失败');
      const { response } = info.file;
      notification.error({
        message: '出现了一个问题',
        description: response.message || '地区库上传失败',
      });
    }

    setUploadLoading(false);
  };

  const queryKnowledgeInfo = () => {
    if (dispatch) {
      dispatch({
        type: 'geolocationModel/queryGeolocationKnowledge',
      });
    }
  };

  const queryGeolocations = () => {
    if (dispatch) {
      dispatch({
        type: 'geolocationModel/queryGeolocations',
      });
    }
  };
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);

  return (
    <Card bordered={false}>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      {/* 上传 */}
      <div className={styles.uploadWrap}>
        <Upload
          disabled={uploadLoading || cmsConnectFlag}
          {...uploadProps}
          beforeUpload={handleBeforeUpload}
          onChange={handleUpload}
        >
          {uploadLoading ? (
            <LoadingOutlined className={styles.uploadIcon} />
          ) : (
            <PlusOutlined className={styles.uploadIcon} />
          )}
          <div className={styles.uploadText}>{uploadLoading ? '上传中...' : '上传地区库'}</div>
        </Upload>

        <div className={styles.warningInfoWrap}>
          <ul>
            <li>
              文件后缀为 <b>txt</b>；
            </li>
            <li>
              文件大小不能超过 <b>50MB</b>。
            </li>
          </ul>
        </div>
      </div>
      <div className={styles.meta}>
        <Skeleton active loading={queryLoading}>
          <MetaInfo {...geolocationKnowledge} />
        </Skeleton>
      </div>
    </Card>
  );
};

export default connect(
  ({ geolocationModel: { geolocationKnowledge }, loading: { effects } }: ConnectState) => ({
    geolocationKnowledge,
    queryLoading: effects['geolocationModel/queryGeolocationKnowledge'],
  }),
)(GeoUpload);
