import { Collapse, Modal, Button, Radio, Alert, Checkbox, Col, Row, message } from 'antd';
import storage from '@/utils/frame/storage';
import { SettingOutlined } from '@ant-design/icons';
import React, { useEffect, useState } from 'react';
import { percentageRateTypeOptions, chartTrendTypeOptions, fieldList } from '../../typings';
// import type { BusinessPanelSettings } from '../../typings';
import type { RadioChangeEvent } from 'antd';
import { updateServiceDashboardSettings } from '../../../../service';
import ThresholdSetting from './components/ThresholdSetting';

export const SERVICE_USER_SETTING_LOCAL_KEY = 'service_user_setting';

const { Panel } = Collapse;

interface ISettingProps {
  // onChange: (
  //   fieldIds: string[],
  //   fieldIdThresholdMap: Record<string, number>,
  //   percentageConfigId: string,
  //   trendConfigId: string,
  // ) => void;
  config: {
    fieldIds: string[];
    fieldIdThresholdMap: Record<string, any[]>;
    percentageId: string;
    trendId: string;
  };
  isLoading: boolean;
  refreshConfig: () => void;
}
const Settings: React.FC<ISettingProps> = ({ config, isLoading, refreshConfig }) => {
  const { fieldIds, fieldIdThresholdMap, percentageId, trendId } = config;
  const [isConfigLoading, setIsConfigLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [userdefinedsetting, setUserdefinedsetting] = useState<string | string[]>([
    'MetaDataConfig',
  ]);
  const [displayFieldIdThresholdMap, setDisplayFieldIdThresholdMap] = useState<
    Record<string, any[]>
  >({});
  const [displayFieldIds, setDisplayFieldIds] = useState<string[]>([]);
  const [percentageConfigId, setPercentageConfigId] = useState<string>('');
  const [trendConfigId, setTrendConfigId] = useState<string>('');

  useEffect(() => {
    storage.put(
      SERVICE_USER_SETTING_LOCAL_KEY,
      typeof userdefinedsetting == 'string' ? userdefinedsetting : userdefinedsetting.join(','),
    );
    // if (onChange) {
    //   onChange(displayFieldIds, displayFieldIdThresholdMap, percentageConfigId, trendConfigId);
    // }
    setIsConfigLoading(true);
    if (!isLoading) {
      if (fieldIds) {
        setDisplayFieldIds(fieldIds);
      }
      if (percentageId) {
        setPercentageConfigId(percentageId);
      }
      if (trendId) {
        setTrendConfigId(trendId);
      }
      if (fieldIdThresholdMap) {
        setDisplayFieldIdThresholdMap(fieldIdThresholdMap);
      }
      setIsConfigLoading(false);
    }
  }, [userdefinedsetting, fieldIds, percentageId, trendId, fieldIdThresholdMap, isLoading]);

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  const handleConfigChange = (key: string | string[]) => {
    setUserdefinedsetting(key);
  };

  const handleFieldDisplayChange = (checkedValues: any[]) => {
    if (checkedValues.length > 0 && checkedValues.length <= 4) {
      setDisplayFieldIds(checkedValues);
      const newFileldIdThresholdMap: Record<string, any[]> = {};
      checkedValues.forEach((id) => {
        if (displayFieldIdThresholdMap[id]) {
          newFileldIdThresholdMap[id] = displayFieldIdThresholdMap[id];
        } else {
          newFileldIdThresholdMap[id] = [0, '0'];
        }
      });
      setDisplayFieldIdThresholdMap(newFileldIdThresholdMap);
    }
  };
  const updateThresholdConfig = (thresholdConfig: any[], key: string) => {
    if (displayFieldIds.includes(key)) {
      setDisplayFieldIdThresholdMap((prev: any) => {
        return { ...prev, [key]: thresholdConfig };
      });
    }
  };

  const handlePercentageConfigIdChange = ({ target: { value } }: RadioChangeEvent) => {
    setPercentageConfigId(value);
  };

  const handleTrendConfigIdChange = ({ target: { value } }: RadioChangeEvent) => {
    setTrendConfigId(value);
  };

  const handleSubmit = async () => {
    const data: any = {
      // parameters: JSON.stringify(displayFieldIds),
      parameters: JSON.stringify(displayFieldIdThresholdMap),
      percentParameter: percentageConfigId,
      timeWindowParameter: trendConfigId,
    };
    const { success } = await updateServiceDashboardSettings(data);
    if (success) {
      message.success('更新成功!');
      handleCancel();
    } else {
      message.error('更新失败!');
    }
    refreshConfig();
  };

  return (
    <>
      <Button
        type="primary"
        onClick={showModal}
        icon={<SettingOutlined />}
        loading={isConfigLoading}
      >
        设置
      </Button>

      <Modal
        width={860}
        title="设置"
        visible={isModalVisible}
        // onOk={handleOk}
        okText="确认"
        onCancel={handleCancel}
        closeIcon={false}
        destroyOnClose
        footer={[
          <Button key="submit" type="primary" onClick={handleSubmit}>
            确认
          </Button>,
          <Button key="cancel" type="primary" onClick={handleCancel}>
            关闭
          </Button>,
        ]}
      >
        <Collapse defaultActiveKey={userdefinedsetting} onChange={handleConfigChange}>
          <Panel header="指标数据设置" key="MetaDataConfig">
            <>
              <Alert
                message="至少显示1个指标；最多可显示4个指标; 大于或者小于配置阈值的指标将显示为红色;"
                type="info"
                style={{ marginBottom: 10 }}
              />
              <Checkbox.Group value={displayFieldIds} onChange={handleFieldDisplayChange}>
                <Row gutter={10}>
                  {fieldList.map((field) => (
                    <Col span={12} key={field.key}>
                      <Checkbox value={field.key}>{field.label}</Checkbox>
                      {displayFieldIds.includes(field.key) && (
                        <ThresholdSetting
                          field={field}
                          thresholdConfig={
                            Object.prototype.toString.call(
                              displayFieldIdThresholdMap[field.key],
                            ) === '[object Array]'
                              ? displayFieldIdThresholdMap[field.key]
                              : []
                          }
                          onChange={updateThresholdConfig}
                        />
                      )}
                    </Col>
                  ))}
                </Row>
              </Checkbox.Group>
            </>
          </Panel>
          <Panel header="百分比图设置" key="PercentageConfig">
            <Radio.Group
              onChange={handlePercentageConfigIdChange}
              options={percentageRateTypeOptions}
              value={percentageConfigId}
            />
          </Panel>
          <Panel header="趋势图设置" key="TrendChartConfig">
            <Radio.Group
              onChange={handleTrendConfigIdChange}
              options={chartTrendTypeOptions}
              value={trendConfigId}
            />
          </Panel>
        </Collapse>
      </Modal>
    </>
  );
};

export default Settings;
