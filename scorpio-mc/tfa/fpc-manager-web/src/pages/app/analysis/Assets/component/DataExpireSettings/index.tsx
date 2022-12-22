import { Button, Form, message } from 'antd';
import { ModalForm, ProFormDigit } from '@ant-design/pro-form';
import { getCurrentExpireDays, settingExpireDays } from '../../service';
import { useState } from 'react';
import { useCallback } from 'react';

const DataExpireSettings = () => {
  const [form] = Form.useForm();
  // useEffect(() => {
  //   getCurrentExpireDays().then((res) => {
  //     const { success, result } = res;
  //     if (success) {
  //       form.setFieldsValue(result);
  //     }
  //   });
  //   // eslint-disable-next-line react-hooks/exhaustive-deps
  // }, []);

  const [loading, setLoading] = useState(false);

  const queryUsefulTimes = useCallback(() => {
    setLoading(true);
    getCurrentExpireDays().then((res) => {
      const { success, result } = res;
      if (success) {
        console.log(result, 'result');
        form.setFieldsValue(result);
      }
    });
    setLoading(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const submitData = async (values: any) => {
    console.log(values);
    const { usefulLife } = values;
    const { result, success } = await settingExpireDays({ time: usefulLife });
    console.log(result, 'result');
    if (success) {
      message.success('提交成功!');
    } else {
      message.error('提交失败!');
    }
    return true;
  };
  const helpMess = `单位为天，代表系统自动检测到的流量中的资产数据有效期限：
           1.如果资产超过配置天数没有数据，则认为资产下线。
           2.如果资产属性超过配置天数没有对应的属性数据，则认为资产下线，例如7天前检测到TCP 80端口开放，但7天都没有再检测到80端口开放，则认为80端口已经关闭。`;
  return (
    <>
      <ModalForm
        // labelWidth="auto"
        title="资产数据有效期设定"
        form={form}
        trigger={
          <Button type="primary" onClick={queryUsefulTimes}>
            配置
          </Button>
        }
        onFinish={submitData}
        initialValues={{}}
      >
        <ProFormDigit
          name="usefulLife"
          label="数据有效期"
          placeholder="请输入1-30之间的数值，默认为7"
          help={helpMess}
          fieldProps={{ precision: 0 }}
          disabled={loading}
          rules={[{ type: 'number', min: 1, max: 30 }]}
        />
      </ModalForm>
    </>
  );
};

export default DataExpireSettings;
