import type { ConnectState } from '@/models/connect';
import { enumObj2List } from '@/utils/utils';
import { Button, Cascader, Checkbox, Form, Input, Select, Space, TimePicker } from 'antd';
import type { Moment } from 'moment';
import moment from 'moment';
import { useEffect, useMemo } from 'react';
import { history, useSelector } from 'umi';
import type { ICountry } from '../../Geolocation/typings';
import { createMailLoginRule, updateMailLoginRule } from '../service';
import type { EWeek, IMailLoginRule } from '../typings';
import { EMailRuleStatus, RuleActionLabel, WeekLabel } from '../typings';

interface Props {
  detail?: IMailLoginRule;
}

const TIME_FORMAT = 'HH:mm:ss';

const MailRuleForm = ({ detail }: Props) => {
  const [form] = Form.useForm<
    Omit<IMailLoginRule, 'startTime' | 'endTime' | 'state' | 'period'> & {
      loginAddress: EWeek[];
      startTime: Moment;
      endTime: Moment;
      state: boolean;
      period: EWeek[];
    }
  >();

  useEffect(() => {
    if (detail) {
      const loginAddress = [];

      if (detail.countryId) {
        loginAddress.push(detail.countryId);
      }
      if (detail.provinceId !== '0') {
        loginAddress.push(detail.provinceId);
      }

      if (detail.cityId !== '0') {
        loginAddress.push(detail.cityId);
      }

      form.setFieldsValue({
        ...detail,
        startTime: moment(detail.startTime, TIME_FORMAT),
        endTime: moment(detail.endTime, TIME_FORMAT),
        loginAddress,
        state: detail.state === EMailRuleStatus.enable ? true : false,
        period: detail.period?.split(','),
      });
    }
  }, [detail, form]);

  const allCountryList = useSelector<ConnectState, ICountry[]>(
    (state) => state.geolocationModel.allCountryList,
  );

  const locationOptions = useMemo(() => {
    return allCountryList.map((country) => {
      return {
        value: country.countryId,
        label: country.nameText,
        children: country.children?.map((province) => {
          return {
            label: province.nameText,
            value: province.provinceId,
            children: province.children?.map((city) => {
              return {
                label: city.nameText,
                value: city.cityId,
              };
            }),
          };
        }),
      };
    });
  }, [allCountryList]);

  const handleBack = () => {
    history.push('/configuration/safety-analysis/mail-login');
  };

  const handleSubmit: Parameters<typeof Form>[0]['onFinish'] = (values) => {
    const result: Record<string, any> = { ...(values as Record<string, any>) };
    if (result.loginAddress) {
      const [countryId, provinceId, cityId] = result.loginAddress;
      if (countryId) {
        result.countryId = countryId;
      }
      if (provinceId) {
        result.provinceId = provinceId;
      }
      if (cityId) {
        result.cityId = cityId;
      }
      delete result.loginAddress;
    }

    result.startTime = (result.startTime as Moment).format(TIME_FORMAT);
    result.endTime = (result.endTime as Moment).format(TIME_FORMAT);
    if (result.period instanceof Array) {
      result.period = result.period.join(',');
    }

    result.state = result.state === true ? EMailRuleStatus.enable : EMailRuleStatus.disable;

    if (detail) {
      updateMailLoginRule(detail.id, result).then((res) => {
        const { success } = res;
        if (success) {
          handleBack();
        }
      });
    } else {
      createMailLoginRule(result).then((res) => {
        const { success } = res;
        if (success) {
          handleBack();
        }
      });
    }
  };

  return (
    <div>
      <Form
        form={form}
        onFinish={handleSubmit}
        wrapperCol={{ span: 16 }}
        labelCol={{ span: 4 }}
        initialValues={{ state: false }}
      >
        <Form.Item name="mailAddress" label="邮箱" rules={[{ required: true }]} required={true}>
          <Input />
        </Form.Item>
        <Form.Item
          name="loginAddress"
          label="登录位置"
          rules={[{ required: true }]}
          required={true}
        >
          <Cascader options={locationOptions} placeholder="选择邮件登录地区" />
        </Form.Item>
        <Form.Item
          name="startTime"
          label="登录起始时间"
          rules={[{ required: true }]}
          required={true}
        >
          <TimePicker />
        </Form.Item>
        <Form.Item name="endTime" label="登录结束时间" rules={[{ required: true }]} required={true}>
          <TimePicker />
        </Form.Item>
        <Form.Item name="action" label="动作" rules={[{ required: true }]} required={true}>
          <Select
            options={enumObj2List(RuleActionLabel)}
            allowClear
            style={{ width: '100%' }}
            placeholder="选择告警行为"
          />
        </Form.Item>
        <Form.Item name="period" label="每周生效时间" rules={[{ required: true }]} required={true}>
          <Select
            options={enumObj2List(WeekLabel)}
            placeholder="选择告警周期"
            mode="multiple"
            allowClear
            style={{ width: '100%' }}
          />
        </Form.Item>
        <Form.Item name="state" label="启用" valuePropName="checked">
          <Checkbox />
        </Form.Item>
        <Form.Item wrapperCol={{ offset: 4 }}>
          <Space direction="horizontal">
            <Button htmlType="submit" type="primary">
              保存
            </Button>
            <Button onClick={handleBack}>返回</Button>
          </Space>
        </Form.Item>
      </Form>
    </div>
  );
};

export default MailRuleForm;
