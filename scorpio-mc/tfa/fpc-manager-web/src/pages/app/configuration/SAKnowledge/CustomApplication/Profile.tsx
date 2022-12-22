import { useEffect } from 'react';
import { connect } from 'dva';
import type { Dispatch } from 'redux';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Skeleton, Empty, Descriptions, Card } from 'antd';
import styles from './style.less';
import { getSignatureTypeName } from './Form';
import type { ConnectState } from '@/models/connect';
import type { CustomSAModelState, MetadataModelState, SAKnowledgeModelState } from 'umi';
import { ECustomSAApiType } from '../typings';

const FormItem = Form.Item;

interface CustomApplicationProfileProps {
  id: string;
  dispatch: Dispatch;
  queryDetailLoading: boolean;
  customSAModel: CustomSAModelState;
  SAKnowledgeModel: SAKnowledgeModelState;
  metadataProtocolMap: MetadataModelState['metadataProtocolMap'];
}

interface IRule {
  name: string;
  ipAddress?: string;
  protocol: string;
  port: string;
  signatureType?: string;
  signatureOffset?: string;
  signatureContent?: string;
}

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 19 },
};

const CustomApplicationProfile: React.FC<CustomApplicationProfileProps> = (props) => {
  const {
    id,
    dispatch,
    customSAModel,
    SAKnowledgeModel,
    metadataProtocolMap,
    queryDetailLoading,
  } = props;
  const { customApplicationDetail } = customSAModel;
  const { allCategoryMap, allSubCategoryMap } = SAKnowledgeModel;

  /**
   * constructor
   */

  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'customSAModel/queryCustomSADetail',
        payload: { id, type: ECustomSAApiType.APPLICATION },
      });
    }
  }, []);

  if (queryDetailLoading) {
    return <Skeleton active />;
  }

  if (!customApplicationDetail.id) {
    return <Empty description="自定义应用不存在或已被删除" />;
  }

  let ruleList = [];
  if (customApplicationDetail.rule) {
    try {
      ruleList = JSON.parse(customApplicationDetail.rule);
    } catch (error) {
      ruleList = [];
    }
  }

  return (
    <Form className={styles.profileWrap}>
      <FormItem {...formLayout} label="id" style={{ display: 'none' }}>
        <span className="ant-form-text">{customApplicationDetail.id}</span>
      </FormItem>
      <FormItem {...formLayout} label="applicationId" style={{ display: 'none' }}>
        <span className="ant-form-text">{customApplicationDetail.applicationId}</span>
      </FormItem>
      <FormItem {...formLayout} label="应用名称">
        <span className="ant-form-text">{customApplicationDetail.name}</span>
      </FormItem>
      <FormItem {...formLayout} label="分类">
        <span className="ant-form-text">
          {allCategoryMap[customApplicationDetail.categoryId]
            ? allCategoryMap[customApplicationDetail.categoryId].nameText
            : ''}
        </span>
      </FormItem>
      <FormItem {...formLayout} label="子分类">
        <span className="ant-form-text">
          {allSubCategoryMap[customApplicationDetail.subCategoryId]
            ? allSubCategoryMap[customApplicationDetail.subCategoryId].nameText
            : ''}
        </span>
      </FormItem>
      <FormItem {...formLayout} label="应用层协议">
        <span className="ant-form-text">
          {metadataProtocolMap[customApplicationDetail.l7ProtocolId]
            ? metadataProtocolMap[customApplicationDetail.l7ProtocolId].nameText
            : ''}
        </span>
      </FormItem>
      <FormItem {...formLayout} label="规则">
        <Card bordered={false} bodyStyle={{ padding: 0 }}>
          {ruleList.map((rule: IRule) => (
            <Descriptions size="small" bordered column={2}>
              <Descriptions.Item label="名称" span={2}>
                {rule.name}
              </Descriptions.Item>
              <Descriptions.Item label="IP地址" span={2}>
                {rule.ipAddress}
              </Descriptions.Item>
              <Descriptions.Item label="协议" span={1}>
                {rule.protocol && rule.protocol.toLocaleUpperCase()}
              </Descriptions.Item>
              <Descriptions.Item label="端口" span={1}>
                {rule.port}
              </Descriptions.Item>
              <Descriptions.Item label="签名类型" span={2}>
                {getSignatureTypeName(rule.signatureType)}
              </Descriptions.Item>
              <Descriptions.Item label="签名偏移" span={2}>
                {rule.signatureOffset}
              </Descriptions.Item>
              <Descriptions.Item label="签名内容" span={2}>
                {rule.signatureContent}
              </Descriptions.Item>
            </Descriptions>
          ))}
        </Card>
      </FormItem>
      <FormItem {...formLayout} label="描述">
        <span className="ant-form-text">{customApplicationDetail.description}</span>
      </FormItem>
    </Form>
  );
};

export default connect(
  ({
    customSAModel,
    SAKnowledgeModel,
    metadataModel: { metadataProtocolMap },
    loading,
  }: ConnectState) => ({
    customSAModel,
    SAKnowledgeModel,
    metadataProtocolMap,
    queryDetailLoading: loading.effects['customSAModel/queryCustomSADetail'] || false,
  }),
)(CustomApplicationProfile);
