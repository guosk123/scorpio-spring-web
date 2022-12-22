import { InfoCircleOutlined, UploadOutlined } from '@ant-design/icons';
import { Button, message, Modal, Popover, Progress, Upload } from 'antd';
import { connect } from 'dva';
import $ from 'jquery';
import { useEffect, useMemo, useRef, useState } from 'react';
import { queryUploadUri } from '../../../service';

function OfflinePcapUpload(props: any) {
  const { refresh, show = true } = props;
  // 控制modal是否展示
  const [isModalVisible, setIsModalVisible] = useState(false);
  // 控制进度条是否展示
  const [showProgress, setShowProgress] = useState<boolean>(false);
  // 进度条进度
  const [uploadProgress, setUploadProgress] = useState<number>(0);

  const [fileLists, setFileList] = useState<File[]>([]);

  const [isUpLoadFlag, setIsUpLoadFlag] = useState(false);

  const [uploadUri, setUploadUri] = useState('');

  const [uploadUriLoading, setUploadUriLoading] = useState(false);

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
    setIsUpLoadFlag(true);
    uploadRef.current = $.ajax({
      // TODO: 需要替换成uploadUri
      // url: 'https://www.mocky.io/v2/5cc8019d300000980a055e76',
      url: uploadUri,
      type: 'POST',
      processData: false,
      contentType: false,
      data: formData,
      success: () => {
        message.info('上传完成');
      },
      error: () => {
        message.error('上传失败');
      },
      xhr() {
        const xhr = new XMLHttpRequest();
        // 使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
        xhr.upload.addEventListener('progress', (e) => {
          // loaded代表上传了多少
          // total代表总数为多少
          const progressRate = (e.loaded / e.total) * 100;
          setUploadProgress(progressRate);
          if (progressRate >= 100) {
            // message.info('上传完成');
            uploadRef.current = undefined;
            window.onbeforeunload = () => {};
            setIsModalVisible(false);
            setShowProgress(false);
            setIsUpLoadFlag(false);
            setFileList([]);
            refresh();
            setTimeout(() => {
              refresh();
            }, 5000);
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
      if (uploadRef.current) {
        uploadRef.current.abort();
      }
      window.onbeforeunload = () => {};
      setFileList([]);
      setShowProgress(false);
      uploadRef.current = undefined;
      setIsModalVisible(false);
      setIsUpLoadFlag(false);
    }
  };

  const uploadProps: any = {
    accept: ['.pcap', '.pcapng'], // 只支持 pcap 文件
    maxCount: 1,
    fileList: fileLists,
    onRemove: () => {
      setFileList([]);
    },
    beforeUpload: (file: any) => {
      setFileList([file]);
      setUploadUri('');
      setUploadUriLoading(true);
      queryUploadUri(file.name).then((res) => {
        const { success, result } = res;
        setUploadUriLoading(false);
        if (success) {
          setUploadUri(result);
        } else {
          message.error('获取文件上传链接失败，请检查文件名合法性或者刷新页面重试');
        }
      });
      // 手动上传
      return false;
    },
  };

  const upLoadBtnDisable = useMemo(() => {
    if (isUpLoadFlag) {
      return isUpLoadFlag;
    }
    return !(fileLists.length && uploadUri.length);
  }, [fileLists, uploadUri, isUpLoadFlag]);

  return (
    <>
      <Button
        type="primary"
        onClick={showModal}
        style={show ? {} : { display: 'none' }}
        icon={<UploadOutlined />}
      >
        上传文件
      </Button>
      <Modal
        title={
          <div style={{ display: 'flex', alignItems: 'center' }}>
            上传离线文件
            <Popover
              content={
                <div>
                  <div>我们不推荐在文件名称中使用如下字符:</div>
                  <div>{`* ? > < ; & ! | \\ / ' " \` ( ) { } . .. + -`}</div>
                </div>
              }
              trigger="hover"
            >
              <span style={{ fontSize: '20px', margin: '0px 10px' }}>
                <InfoCircleOutlined />
              </span>
            </Popover>
          </div>
        }
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
            disabled={upLoadBtnDisable}
            loading={uploadUriLoading}
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
export default connect()(OfflinePcapUpload);
