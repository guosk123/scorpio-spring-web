import type { IScenarioTaskModelState } from '@/models/app/scenarioTask';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Divider, Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { Fragment, useEffect } from 'react';
import type { Dispatch } from 'redux';
import type { IScenarioCustomTemplate, IScenarioCustomTemplateFunction } from '../../typings';
import {
  DATA_SOURCE_LIST,
  EVAL_FUNCTION_BEACON,
  EVAL_FUNCTION_SUM,
  flowRecordComputableFields,
  GROUP_BY_LIST,
} from '../ScenarioTemplateForm';

const FormItem = Form.Item;

interface CustomTemplateProfileProps {
  id: string;
  dispatch: Dispatch;
  queryDetailLoading: boolean;
  scenarioCustomTemplateDetail: IScenarioCustomTemplate;
}

const formLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 19 },
  style: { marginBottom: 0 },
};

/**
 * 获取数据源的详情
 * @param {String} dataSource
 */
export const getDataSourceInfo = (dataSource: string) => {
  return (
    DATA_SOURCE_LIST.find((item) => item.value === dataSource) || {
      value: dataSource,
      label: dataSource,
    }
  );
};

/**
 * 获取分组详情
 * @param {String} groupBy
 */
export const getGroupByInfo = (groupBy: string) => {
  return (
    GROUP_BY_LIST.find((item) => item.value === groupBy) || {
      value: groupBy,
      label: groupBy,
    }
  );
};

/**
 * 获取可 Sum 的字段详情
 * @param {String} field
 */
export const getComputableFieldInfo = (field: string) => {
  return (
    flowRecordComputableFields.find((item) => item.value === field) || {
      value: field,
      label: field,
    }
  );
};

/**
 * 生成计算方法展示DOM
 * @param templateDetail 模板详情
 * @param type 布局方式：水平还是垂直
 */
export const getEvalFunctionDom = (
  templateDetail: IScenarioCustomTemplate,
  type: 'horizontal' | 'vertical' = 'horizontal',
) => {
  const { function: functionJson } = templateDetail;
  if (!functionJson) {
    return '';
  }
  let evalFunction = {} as IScenarioCustomTemplateFunction;
  try {
    evalFunction = JSON.parse(functionJson);
  } catch (error) {
    evalFunction = {} as IScenarioCustomTemplateFunction;
  }

  const { name, params = {} } = evalFunction;

  // 查找字段名称
  let fieldName = params.field;
  if (name === EVAL_FUNCTION_SUM && params.field) {
    const fieldInfo = getComputableFieldInfo(params.field);
    if (fieldInfo) {
      fieldName = fieldInfo.label;
    }
  }

  if (type === 'horizontal') {
    return (
      <Fragment>
        <div>方法: {name}</div>
        {name === EVAL_FUNCTION_SUM && <div>字段: {fieldName}</div>}
        {name === EVAL_FUNCTION_BEACON && <div>数量阈值: {params.numberThreshold}</div>}
      </Fragment>
    );
  }

  return (
    <Fragment>
      <span>方法: {name}</span>
      {name === EVAL_FUNCTION_SUM && (
        <Fragment>
          <Divider type="vertical" />
          <span>字段: {fieldName}</span>
        </Fragment>
      )}
      {name === EVAL_FUNCTION_BEACON && (
        <Fragment>
          <Divider type="vertical" />
          <span>数量阈值: {params.numberThreshold}</span>
        </Fragment>
      )}
    </Fragment>
  );
};

const CustomApplicationProfile: React.FC<CustomTemplateProfileProps> = (props) => {
  const { id, dispatch, scenarioCustomTemplateDetail, queryDetailLoading } = props;

  /**
   * constructor
   */

  useEffect(() => {
    if (dispatch) {
      dispatch({
        type: 'scenarioTaskModel/queryScenarioCustomTemplateDetail',
        payload: { id },
      });
    }
  }, []);

  if (queryDetailLoading) {
    return <Skeleton active />;
  }

  if (!scenarioCustomTemplateDetail.id) {
    return <Empty description="自定义模板不存在或已被删除" />;
  }

  return (
    <Form>
      <FormItem {...formLayout} key="id" label="id" style={{ display: 'none' }}>
        <span className="ant-form-text">{scenarioCustomTemplateDetail.id}</span>
      </FormItem>
      <FormItem {...formLayout} key="name" label="名称">
        <span className="ant-form-text">{scenarioCustomTemplateDetail.name}</span>
      </FormItem>
      <FormItem {...formLayout} key="dataSource" label="数据源">
        <span className="ant-form-text">
          {getDataSourceInfo(scenarioCustomTemplateDetail.dataSource).label}
        </span>
      </FormItem>
      <FormItem {...formLayout} key="filter" label="过滤条件">
        <span className="ant-form-text">{scenarioCustomTemplateDetail.filterSpl}</span>
      </FormItem>
      <FormItem {...formLayout} key="evalFunction" label="计算方法">
        <span className="ant-form-text">{getEvalFunctionDom(scenarioCustomTemplateDetail)}</span>
      </FormItem>
      <FormItem {...formLayout} key="avgTimeInterval" label="按时间平均">
        <span className="ant-form-text">
          {scenarioCustomTemplateDetail.avgTimeInterval === 0
            ? '不平均'
            : `${scenarioCustomTemplateDetail.avgTimeInterval}s`}
        </span>
      </FormItem>
      <FormItem {...formLayout} key="sliceTimeInterval" label="按时间分片">
        <span className="ant-form-text">
          {scenarioCustomTemplateDetail.sliceTimeInterval === 0
            ? '不切片'
            : `${scenarioCustomTemplateDetail.sliceTimeInterval}s`}
        </span>
      </FormItem>
      <FormItem {...formLayout} key="groupBy" label="分组">
        <span className="ant-form-text">
          {scenarioCustomTemplateDetail.groupBy
            ? getGroupByInfo(scenarioCustomTemplateDetail.groupBy).label
            : '不分组'}
        </span>
      </FormItem>
      <FormItem {...formLayout} key="description" label="描述信息">
        <span className="ant-form-text">{scenarioCustomTemplateDetail.description}</span>
      </FormItem>
    </Form>
  );
};

export default connect(
  ({
    scenarioTaskModel,
    loading,
  }: {
    scenarioTaskModel: IScenarioTaskModelState;
    loading: {
      effects: Record<string, boolean>;
    };
  }) => ({
    scenarioCustomTemplateDetail: scenarioTaskModel.scenarioCustomTemplateDetail,
    queryDetailLoading:
      loading.effects['scenarioTaskModel/queryScenarioCustomTemplateDetail'] || false,
  }),
)(CustomApplicationProfile);
