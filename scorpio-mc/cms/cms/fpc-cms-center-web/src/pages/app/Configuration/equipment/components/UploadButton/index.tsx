import { UploadOutlined } from '@ant-design/icons';
import { Button, message, Modal, Progress, Upload } from 'antd';
import { connect } from 'dva';
import $ from 'jquery';
import { useEffect, useRef, useState } from 'react';

function UploadButton(props: any) {
  const { uploadUri, queryPcapList, pcapState } = props;
  // 控制modal是否展示
  const [isModalVisible, setIsModalVisible] = useState(false);
  // 控制进度条是否展示
  const [showProgress, setShowProgress] = useState<boolean>(false);
  // 进度条进度
  const [uploadProgress, setUploadProgress] = useState<number>(0);

  const [fileLists, setFileList] = useState<File[]>([]);

  const showModal = () => {
    setIsModalVisible(true);
  };

  // 上传对象
  const uploadRef = useRef<any>();

  // 上传文件
  const handleImport = () => {
    window.onbeforeunload = () => {
      return '上传未完成，是否离开页面';
    };
    const formData = new FormData();
    formData.append('filename', fileLists[0]);
    uploadRef.current = $.ajax({
      // TODO: 需要替换成uploadUri
      // url: 'https://www.mocky.io/v2/5cc8019d300000980a055e76',
      url: uploadUri,
      type: 'POST',
      processData: false,
      contentType: false,
      data: formData,
      xhr() {
        const xhr = new XMLHttpRequest();
        // 使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
        xhr.upload.addEventListener('progress', (e) => {
          // loaded代表上传了多少
          // total代表总数为多少
          const progressRate = (e.loaded / e.total) * 100;
          setUploadProgress(progressRate);
          if (progressRate === 100) {
            message.info('上传完成');
            uploadRef.current = undefined;
            window.onbeforeunload = () => {};
            setIsModalVisible(false);
            setShowProgress(false);
            setFileList([]);
            queryPcapList('', pcapState.number, pcapState.size);
          }
        });

        return xhr;
      },
    });
    if (fileLists.length) {
      setShowProgress(true);
    }
  };

  useEffect(() => {
    if (!fileLists.length) {
      setShowProgress(false);
    }
  }, [fileLists]);

  const handleCancel = () => {
    let isCancel = true;
    if (uploadRef.current) {
      isCancel = confirm('是否退出上传');
    }
    if (isCancel) {
      window.onbeforeunload = () => {};
      setFileList([]);
      setShowProgress(false);
      uploadRef.current = undefined;
      setIsModalVisible(false);
    }
  };

  const uploadProps: any = {
    accept: ['.TAR.GZ', '.tar.gz'], // 支持的文件
    maxCount: 1,
    fileList: fileLists,
    onRemove: () => {
      setFileList([]);
    },
    beforeUpload: (file: any) => {
      setFileList([file]);
      // queryUploadUri(file.name);
      // 手动上传
      return false;
    },
  };

  return (
    <>
      <Button type="primary" onClick={showModal} icon={<UploadOutlined />}>
        上传文件
      </Button>
      <Modal
        title="上传升级包"
        visible={isModalVisible}
        maskClosable={false}
        onCancel={handleCancel}
        footer={[
          <Button key="back" onClick={handleCancel}>
            取消
          </Button>,
          <Button
            key="submit"
            type="primary"
            disabled={!(fileLists.length && uploadUri.length)}
            onClick={handleImport}
          >
            上传
          </Button>,
        ]}
      >
        <Progress
          percent={uploadProgress}
          format={(percent) => `${percent?.toFixed(1)}%`}
          size="small"
          status="active"
          style={showProgress ? {} : { display: 'none' }}
        />
        <Upload {...uploadProps}>
          <div>
            <Button type="primary" icon={<UploadOutlined />}>
              选择文件
            </Button>
          </div>
        </Upload>
      </Modal>
    </>
  );
}
export default connect()(UploadButton);
