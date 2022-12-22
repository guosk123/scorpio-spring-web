import Terminal from '@/components/Terminal';
import { Alert, Button, Col, Divider, message, Row, Spin, Upload } from 'antd';
import type { UploadFile } from 'antd/lib/upload/interface';
import { connect } from 'dva';
import moment from 'moment';
import React, { Fragment, useEffect, useState } from 'react';
import type { Dispatch } from 'redux';
import type { IUpgradeInfo } from './typings';
import { EUpgradeState } from './typings';
import type { ISystemUpgradeModelState } from './model';

import styles from './style.less';
import type { FormComponentProps } from '@ant-design/compatible/lib/form';
import { Form } from '@ant-design/compatible';
import { FileZipOutlined, Loading3QuartersOutlined, SyncOutlined } from '@ant-design/icons';

const FormItem = Form.Item;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 18 },
  },
};

interface ISystemUpgradeProps extends FormComponentProps {
  dispatch: Dispatch<any>;
  upgradeInfos: IUpgradeInfo;
  upgradeLog?: string;
  upgrageState?: EUpgradeState;
  uploadUpgradeLoading: boolean;
  queryUpgradeLogsLoading: boolean;
  queryUpgradeInfosLoading: boolean;
}

const SystemUpgrade: React.SFC<ISystemUpgradeProps> = ({
  dispatch,
  upgradeInfos,
  upgradeLog,
  upgrageState,
  uploadUpgradeLoading,
  queryUpgradeInfosLoading,
}) => {
  const [fileList, setFileList] = useState<UploadFile<any>[]>([]);

  let queryLogTimer: number | undefined = undefined;

  /**
   * 获取升级包版本信息
   */
  const queryUpgradeInfos = () => {
    if (dispatch) {
      dispatch({
        type: 'systemUpgradeModel/queryUpgradeInfos',
      });
    }
  };

  /**
   * 清除日志定时器
   */
  const clearLogInterval = () => {
    if (queryLogTimer) {
      window.clearInterval(queryLogTimer);
    }
  };

  /**
   * 获取升级日志
   */
  const queryUpgradeLog = (cursor: number = 0) => {
    if (dispatch) {
      // 先清除定时器
      clearLogInterval();

      (
        dispatch({
          type: 'systemUpgradeModel/queryUpgradeLogs',
          payload: {
            cursor,
          },
        }) as unknown as Promise<any>
      ).then(({ success, state, nextCursor = 0 }) => {
        // 成功并且状态为进行中，下一秒再查询
        if (success && state === EUpgradeState.RUNNING) {
          // 重新设置定时器
          queryLogTimer = window.setInterval(() => queryUpgradeLog(nextCursor), 1000);
        }
        if (success && state !== EUpgradeState.RUNNING) {
          queryUpgradeInfos();
        }
      });
    }
  };

  useEffect(() => {
    queryUpgradeInfos();
    queryUpgradeLog();

    return () => {
      window.clearInterval(queryLogTimer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /**
   * 检查升级包
   */
  const checkFile = (file: UploadFile): boolean => {
    // 检查文件后缀
    const fileType = file.name.split('.').splice(-2);
    // 文件名后缀转大写
    const fileType2UpperCase = fileType?.join('.')?.toLocaleUpperCase();
    if (fileType2UpperCase !== 'TAR.GZ') {
      message.error('文件后缀不支持');
      return false;
    }

    // 检查文件大小
    if ((file.size ?? 0) <= 0) {
      message.error('文件内容不能为空');
      return false;
    }
    // const isOK = (file.size ?? 0) / 1024 / 1024 <= 1024;
    // if (!isOK) {
    //   message.error('文件大小不能超过1GB');
    //   return false;
    // }

    return true;
  };

  const handleUpload = () => {
    // TODO: 检查文件
    if (!fileList[0]) {
      return;
    }
    const [file] = fileList;
    if (!checkFile(file)) {
      return;
    }

    const formData = new FormData();
    formData.append('file', file as any);
    if (dispatch) {
      (
        dispatch({
          type: 'systemUpgradeModel/uploadUpgrade',
          payload: { formData },
        }) as unknown as Promise<any>
      ).then((success: boolean) => {
        if (success) {
          // 清空上传的升级包
          setFileList([]);
          // 重新拉取升级日志
          queryUpgradeLog();
        }
      });
    }
  };

  const uploadProps = {
    showUploadList: false,
    accept: '.gz',
    beforeUpload: (file: UploadFile) => {
      const isPass = checkFile(file);
      if (isPass) {
        setFileList([file]);
      }
      return false;
    },
  };

  const isRunning = uploadUpgradeLoading || upgrageState === EUpgradeState.RUNNING;

  const renderBtnText = () => {
    if (uploadUpgradeLoading) {
      return '上传中';
    }
    if (upgrageState === EUpgradeState.RUNNING) {
      return '升级中';
    }
    return '升级';
  };

  return (
    <Form {...formItemLayout} className={styles.upgradeWrap}>
      {(upgrageState === EUpgradeState.EXCEPTION || upgrageState === EUpgradeState.FAILED) && (
        <Alert
          message={
            <div>
              在
              {upgradeInfos.upgradeStartTime
                ? moment(upgradeInfos.upgradeStartTime).format('YYYY-MM-DD HH:mm:ss')
                : ''}
              尝试更新，更新失败。
            </div>
          }
          type="error"
        />
      )}

      {uploadUpgradeLoading && (
        <Alert
          message="升级包上传中..."
          description="升级包正在上传，请勿离开本页面。"
          type="info"
          showIcon
          icon={<Loading3QuartersOutlined spin />}
        />
      )}
      {/* 更新中 */}
      {upgrageState === EUpgradeState.RUNNING && (
        <Alert
          message="系统升级中..."
          description={
            <Fragment>
              <div>升级过程中，程序将停止，无法对外提供服务。</div>
              <div>请等待10分钟（升级的具体时间和升级内容有关系）后尝试刷新页面，重新登录。</div>
            </Fragment>
          }
          type="info"
          showIcon
          icon={<SyncOutlined type="sync" spin />}
        />
      )}

      <Spin spinning={queryUpgradeInfosLoading}>
        <FormItem label="首次安装版本" wrapperCol={{ sm: { span: 20 } }}>
          <Row>
            <Col span={6}>
              <span className="ant-form-text">{upgradeInfos.installVersion}</span>
            </Col>
            <Col span={9}>
              <span>安装包发布时间：</span>
              <span className="ant-form-text">
                {upgradeInfos.installReleaseTime
                  ? moment(upgradeInfos.installReleaseTime).format('YYYY-MM-DD HH:mm:ss')
                  : ''}
              </span>
            </Col>
          </Row>
        </FormItem>
        <FormItem label="当前运行升级包版本" wrapperCol={{ sm: { span: 20 } }}>
          <Row>
            <Col span={6}>
              <span className="ant-form-text">{upgradeInfos.upgradeVersion}</span>
            </Col>
            <Col span={9}>
              <span>升级包发布时间：</span>
              <span className="ant-form-text">
                {upgradeInfos.upgradeReleaseTime
                  ? moment(upgradeInfos.upgradeReleaseTime).format('YYYY-MM-DD HH:mm:ss')
                  : ''}
              </span>
            </Col>
            <Col span={9}>
              <span>升级完成时间：</span>
              <span className="ant-form-text">
                {upgradeInfos.upgradeEndTime
                  ? moment(upgradeInfos.upgradeEndTime).format('YYYY-MM-DD HH:mm:ss')
                  : ''}
              </span>
            </Col>
          </Row>
        </FormItem>
      </Spin>
      <Divider dashed />
      <FormItem label="上传升级包" style={{ marginBottom: 10 }}>
        <div className={styles.uploadWrap}>
          <div className={styles.selectFile}>
            <Upload {...uploadProps} disabled={isRunning}>
              <Button type="primary" disabled={isRunning}>
                <FileZipOutlined /> 选择升级包
              </Button>
            </Upload>
          </div>
          <div className={styles.fileName}>{fileList[0]?.name}</div>
          <div className={styles.upgradeBtn}>
            <Button
              type="primary"
              icon="upload"
              onClick={handleUpload}
              disabled={fileList.length === 0}
              loading={isRunning}
            >
              {renderBtnText()}
            </Button>
          </div>
        </div>
      </FormItem>
      <div style={{ marginLeft: 20 }}>
        <Terminal title="升级日志" defaultValue={upgradeLog} />
      </div>
    </Form>
  );
};

export default connect(
  ({
    systemUpgradeModel: { upgradeInfos, upgradeLog, upgrageState },
    loading: { effects },
  }: {
    systemUpgradeModel: ISystemUpgradeModelState;
    loading: { effects: Record<string, boolean> };
  }) => ({
    upgradeInfos,
    upgradeLog,
    upgrageState,
    uploadUpgradeLoading: effects['systemUpgradeModel/uploadUpgrade'] || false,
    queryUpgradeLogsLoading: effects['systemUpgradeModel/queryUpgradeLogs'] || false,
    queryUpgradeInfosLoading: effects['systemUpgradeModel/queryUpgradeInfos'] || false,
  }),
)(SystemUpgrade);
