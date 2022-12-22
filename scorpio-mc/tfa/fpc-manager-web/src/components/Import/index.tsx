import { API_BASE_URL, API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import { DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, message, Modal, Upload } from 'antd';
import type { ButtonProps } from 'antd/es/button';
import type { UploadProps } from 'antd/es/upload/interface';
import { connect } from 'dva';
import type { ReactNode } from 'react';
import React, { useState } from 'react';
import type { Dispatch } from 'umi';
import styles from './index.less';

interface IImportProps {
  dispatch: Dispatch;
  /** 导入loading */
  loading: boolean | undefined;
  /** 弹出框标题 */
  modalTitle: string;
  /** 按钮的文字 */
  buttonText?: ReactNode | string;
  buttonProps?: ButtonProps;
  /** 描述信息 */
  description?: ReactNode | string;
  /** 导入时所使用的方法 */
  importFunc?: string;
  /** 自定义导入函数 */
  customImportFunc?: (formData: any, handleCloseModal: () => void) => void
  /** 导入成功后的回调 */
  importSuccessCallback?: () => void;
  /** 导入模板下载地址 */
  tempDownloadUrl: string;

  extraData?: Record<string, any>;
  disabled?: boolean;
  /** 导入支持的文件后缀，默认csv, 请使用小写 */
  acceptFileTypes?: string[];
  /** 是否禁用导出按钮 */
  importDisabled?: boolean;
}

const Import: React.FC<IImportProps> = ({
  dispatch,
  modalTitle,
  description,

  buttonText,
  buttonProps = {},

  loading,
  importFunc,
  importSuccessCallback,
  tempDownloadUrl,
  extraData = {},
  disabled,
  importDisabled,
  acceptFileTypes = ['csv'],
  customImportFunc
}) => {
  const [visible, setVisible] = useState<boolean>(false);
  const [fileList, setFileList] = useState<any[]>([]);

  const handleOpenModal = () => {
    setVisible(true);
    setFileList([]);
  };

  const handleCloseModal = () => {
    setVisible(false);
    setFileList([]);
  };

  /** 下载模板 */
  const handleDownloadTemp = () => {
    if (!tempDownloadUrl) {
      message.warning('暂无模板下载地址');
      return;
    }
    const url = `${API_BASE_URL}${API_VERSION_PRODUCT_V1}${tempDownloadUrl}`;
    window.open(url);
  };
  /** 导入 */
  const handleImport = () => {

    if (fileList.length === 0) {
      message.warning('请选择导入文件');
      return;
    }

    const formData = new FormData();
    formData.append('file', fileList[0]);
    if (extraData) {
      Object.keys(extraData).forEach((key) => {
        formData.append(key, extraData[key]);
      });
    }

    if (customImportFunc) {
      customImportFunc(formData, handleCloseModal)
      return
    }

    if (!importFunc) {
      message.warning('没有找到导入地址');
      return;
    } else {
      dispatch({
        type: importFunc,
        payload: formData,
      }).then((success: boolean) => {
        if (success) {
          handleCloseModal();
          if (importSuccessCallback) {
            importSuccessCallback();
          }
        }
      });
    }
  };

  const uploadProps: UploadProps = {
    accept: `${acceptFileTypes.map((fileType) => `.${fileType}`).join(',')}`, // 只支持 csv 文件
    onRemove: (file) => {
      const index = fileList.indexOf(file);
      const newFileList = fileList.slice();
      newFileList.splice(index, 1);
      setFileList(newFileList);
    },
    beforeUpload: (file) => {
      const filename = file.name;
      const postfix = filename.split('.').pop() || '';

      const isSuffixValid = acceptFileTypes.includes(postfix.toLocaleLowerCase());
      if (!isSuffixValid) {
        message.error(`请选择${acceptFileTypes.join(',')}文件`);
      } else {
        setFileList([file]);
      }
      // 手动上传
      return false;
    },
    fileList,
  };

  const renderButton = () => {
    return (
      <Button
        className="import-button"
        disabled={disabled}
        onClick={handleOpenModal}
        {...buttonProps}
      >
        {buttonText || (
          <>
            <UploadOutlined /> 导入
          </>
        )}
      </Button>
    );
  };

  return (
    <>
      {renderButton()}

      <Modal
        width={600}
        title={modalTitle}
        visible={visible}
        destroyOnClose
        maskClosable={false}
        keyboard={false}
        onCancel={handleCloseModal}
        footer={[
          tempDownloadUrl && (
            <Button
              key="download"
              type="link"
              className={styles.tempLink}
              icon={<DownloadOutlined />}
              onClick={handleDownloadTemp}
            >
              下载模板
            </Button>
          ),
          <Button key="back" onClick={handleCloseModal}>
            取消
          </Button>,
          <Button
            key="submit"
            type="primary"
            loading={loading}
            disabled={importDisabled || fileList.length === 0}
            onClick={handleImport}
          >
            导入
          </Button>,
        ]}
      >
        <Upload {...uploadProps} className={styles.uploadWrap}>
          <div>
            <Button type="primary" icon={<UploadOutlined />}>
              选择文件
            </Button>
            <span className={styles.extra}>导入{acceptFileTypes.join(', ')}文件</span>
          </div>
        </Upload>
        <div className={styles.desc}>{description}</div>
      </Modal>
    </>
  );
};

export default connect()(Import);
