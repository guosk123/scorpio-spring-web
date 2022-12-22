import SETTINGS from '@/common/applicationConfig';
import { getCookie } from '@/utils/frame/cookie';
import { Form, Icon as LegacyIcon } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Card, Descriptions, message, notification, Skeleton, Upload } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { Fragment, memo, PureComponent } from 'react';
import ConnectCmsState from '@/pages/app/configuration/components/ConnectCmsState';
import styles from './index.less';

const uploadProps = {
  name: 'file',
  listType: 'picture-card',
  action: `${SETTINGS.API_BASE_URL}/fpc-v1/appliance/sa/knowledges`,
  showUploadList: false,
  withCredentials: true,
  headers: {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  },
  accept: '.txt', // 只支持 text 文件
};

const MetaInfo = memo(({ saKnowledgeInfo }) => {
  const { releaseDate, uploadDate, version } = saKnowledgeInfo;

  if (version) {
    return (
      <Fragment>
        <div className={styles.left}>
          <Descriptions size="small" column={1}>
            <Descriptions.item label="当前版本">{version}</Descriptions.item>
            <Descriptions.item label="发布时间">
              {releaseDate ? moment(releaseDate).format('YYYY-MM-DD HH:mm:ss') : '--'}
            </Descriptions.item>
            <Descriptions.item label="导入时间">
              {uploadDate ? moment(uploadDate).format('YYYY-MM-DD HH:mm:ss') : '--'}
            </Descriptions.item>
          </Descriptions>
        </div>
      </Fragment>
    );
  }
  return (
    <div className={styles.empty}>
      <p>
        <ExclamationCircleOutlined /> 未找到有效的SA规则库文件
      </p>
      <p>SA规则库文件上传之前，应用识别功能将无法正常使用</p>
    </div>
  );
});

@Form.create()
@connect(({ SAKnowledgeModel: { saKnowledgeInfo }, loading }) => ({
  saKnowledgeInfo,
  querySaKnowledgeInfoLoading: loading.effects['SAKnowledgeModel/querySaKnowledgeInfo'],
}))
class SAKnowledgeImport extends PureComponent {
  state = {
    uploadLoading: false,
    cmsConnectFlag: false,
  };

  async componentDidMount() {
    this.querySaKnowledgeInfo();
  }

  querySaKnowledgeInfo = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'SAKnowledgeModel/querySaKnowledgeInfo',
    });
  };

  handleBeforeUpload = (file) => {
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

    const isLt100M = file.size / 1024 / 1024 <= 10;
    if (!isLt100M) {
      message.error('文件大小不能超过10MB');
      return false;
    }

    return true;
  };

  handleUpload = (info) => {
    const { dispatch } = this.props;
    if (info.file.status === 'uploading') {
      this.setState({ uploadLoading: true });
      return;
    }

    if (info.file.status === 'done') {
      message.success('上传成功');
      // 刷新SA规则库
      dispatch({
        type: 'SAKnowledgeModel/refreshSaKnowledge',
      });
    } else if (info.file.status === 'error') {
      message.error('上传失败');
      const { response } = info.file;
      notification.error({
        message: '出现了一个问题',
        description: response.message || 'SA规则库文件上传失败',
      });
    }

    this.setState({
      uploadLoading: false,
    });
  };

  render() {
    const { uploadLoading, cmsConnectFlag } = this.state;
    const { saKnowledgeInfo, querySaKnowledgeInfoLoading } = this.props;

    return (
      <Card bordered={false}>
        <ConnectCmsState
          onConnectFlag={(flag) => {
            this.setState({
              cmsConnectFlag: flag,
            });
          }}
        />
        {/* 上传 */}
        <div className={styles.uploadWrap}>
          <Upload
            disabled={uploadLoading || cmsConnectFlag}
            {...uploadProps}
            beforeUpload={this.handleBeforeUpload}
            onChange={this.handleUpload}
          >
            <LegacyIcon type={uploadLoading ? 'loading' : 'plus'} className={styles.uploadIcon} />
            <div className={styles.uploadText}>
              {uploadLoading ? '上传中...' : '上传SA规则库文件'}
            </div>
          </Upload>

          <div className={styles.warningInfoWrap}>
            <ul>
              <li>
                文件后缀为 <b>txt</b>；
              </li>
              <li>
                文件大小不能超过 <b>10MB</b>。
              </li>
            </ul>
          </div>
        </div>
        <div className={styles.meta}>
          <Skeleton active loading={querySaKnowledgeInfoLoading}>
            <MetaInfo saKnowledgeInfo={saKnowledgeInfo} />
          </Skeleton>
        </div>
      </Card>
    );
  }
}

export default SAKnowledgeImport;
