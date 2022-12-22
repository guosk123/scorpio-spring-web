import type { ConnectState } from '@/models/connect';
import type { IProductInfo } from '@/models/frame/global';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Alert, Descriptions, Popover, Spin, Tag } from 'antd';
import { connect } from 'dva';
import { Fragment, useMemo } from 'react';
import type { ConnectProps, Dispatch } from 'umi';
import styles from '../../style.less';
import versionIcon from './version.svg';

export interface ProductInfoProps extends Partial<ConnectProps> {
  dispatch: Dispatch;
  productInfos?: IProductInfo;
  menu?: boolean;
  systemTime?: string;
}

const ProductInfo = (props: ProductInfoProps) => {
  const { productInfos, systemTime } = props;

  // 由于服务器时间是滚动的，所以这里只检查一次，不再依赖 systemTime
  const timeAlert = useMemo(() => {
    if (!systemTime) {
      return null;
    }
    const diffSeconds = Math.abs(new Date(systemTime).valueOf() - new Date().valueOf()) / 1000;
    // 如果服务器时间和当前时间相差超过5分钟，给出提示信息
    if (diffSeconds >= 5 * 60) {
      return (
        <Tag icon={<ExclamationCircleOutlined />} color="warning">
          客户端时间和服务器时间相差超过5分钟，请检查时间设置
        </Tag>
      );
    }
    return null;
  }, []);

  return (
    <>
      {timeAlert ? (
        <Alert
          message="客户端时间和服务器时间相差超过5分钟，请检查时间设置"
          banner
          style={{
            position: 'fixed',
            left: '50%',
            transform: 'translate(-50%, 13%)',
          }}
        />
      ) : (
        ''
      )}
      {productInfos ? (
        <Popover
          placement="bottomRight"
          arrowPointAtCenter
          overlayClassName={styles.customPopover}
          destroyTooltipOnHide={{ keepParent: false }}
          content={
            <Fragment>
              <Descriptions className={styles.versionPopover} bordered size="small" column={2}>
                <Descriptions.Item span={2} label="产品名称">
                  {productInfos.name}
                </Descriptions.Item>
                <Descriptions.Item label="产品版本">{productInfos.version}</Descriptions.Item>
                <Descriptions.Item label="产品型号">{productInfos.series}</Descriptions.Item>
                <Descriptions.Item label="CPU架构">{productInfos.cpuModel}</Descriptions.Item>
                <Descriptions.Item label="操作系统">{productInfos.osInfo}</Descriptions.Item>
                <Descriptions.Item span={2} label="版权所属">
                  {productInfos.corporation}
                </Descriptions.Item>
                {/* <Descriptions.Item label="授权形式">
                    {productInfos['license-form']}
                  </Descriptions.Item>
                  <Descriptions.Item label="授权使用期限">
                    {productInfos['license-deadline']}
                  </Descriptions.Item> */}
                <Descriptions.Item span={2} label="基础功能">
                  {productInfos.description}
                </Descriptions.Item>
                <Descriptions.Item span={2} label="服务器时间">
                  {systemTime}
                  <div>{timeAlert}</div>
                </Descriptions.Item>
              </Descriptions>
            </Fragment>
          }
        >
          <span className={`${styles.versionWrap} ${styles.action}`}>
            <img src={versionIcon} alt="version" />
            <span className={`${styles.version} anticon`}>{productInfos.version}</span>
          </span>
        </Popover>
      ) : (
        <span className={`${styles.action} ${styles.account}`}>
          <Spin
            size="small"
            style={{
              marginLeft: 8,
              marginRight: 8,
            }}
          />
        </span>
      )}
    </>
  );
};

export default connect(({ globalModel: { productInfos, systemTime } }: ConnectState) => ({
  productInfos,
  systemTime,
}))(ProductInfo);
