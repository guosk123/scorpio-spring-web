import {
  queryAllApplicaiton,
  querySaKnowledgeInfo,
} from '@/pages/app/configuration/SAKnowledge/service';
import type {
  AppCategoryItem,
  AppCategoryResponseData,
  ApplicationItem,
  AppSubCategoryItem,
  IApplicationMap,
  ICustomCategory,
  ICustomSubCategory,
  ISaKnowledgeInfo,
} from '@/pages/app/configuration/SAKnowledge/typings';
import { pageModel } from '@/utils/frame/model';
import type { Effect } from 'umi';
import modelExtend from 'dva-model-extend';
import lodash from 'lodash';
import type { ConnectState } from '../connect';

/**
 * 僵木蠕子分类ID
 */
export const MALICIOUS_SUB_CATEGORY_IDS = ['30', '31'];
/**
 * 僵木蠕应用最小ID（包括）
 */
const BOTNET_WORM_APPLICATION_ID_MIN = 10000;
/**
 * 僵木蠕应用最大ID（不包括）
 */
const BOTNET_WORM_APPLICATION_ID_MAX = 20000;

export interface SAKnowledgeModelState {
  saKnowledgeInfo: ISaKnowledgeInfo;

  allCategoryList: AppCategoryItem[];
  allCategoryMap: Record<string, AppCategoryItem>;
  customCategoryList: ICustomCategory[];

  allSubCategoryList: AppSubCategoryItem[];
  allSubCategoryMap: Record<string, AppSubCategoryItem>;
  customSubCategoryList: ICustomSubCategory[];

  // 所有应用(包括自定义应用，排除掉僵木蠕应用)
  allApplicationList: ApplicationItem[];
  allApplicationMap: IApplicationMap;

  /**
   * 默认的 SA 应用 + 自定义应用
   */
  applicationList: ApplicationItem[];
  /**
   * 默认SA应用
   */
  defaultApplicationList: ApplicationItem[];

  /**
   * 自定义应用
   */
  customApplicationList: ApplicationItem[];

  /**
   * 僵木蠕子分类
   */
  maliciousSubCategoryList: AppSubCategoryItem[];
  /**
   * 僵木蠕应用等恶意应用
   */
  maliciousApplicationList: ApplicationItem[];
}

export interface SAKnowledgeModel {
  namespace: string;
  state: SAKnowledgeModelState;
  effects: {
    querySaKnowledgeInfo: Effect;
    queryAllApplications: Effect;
    /**
     * 刷新和SA规则库相关的内容
     */
    refreshSaKnowledge: Effect;
    getApplicationAndCategoryInfo: Effect;
  };
  reducers: {};
}

const Model = modelExtend(pageModel, {
  namespace: 'SAKnowledgeModel',

  state: {
    saKnowledgeInfo: {},
    // 所有大类
    allCategoryList: [],
    allCategoryMap: {},
    customCategoryList: [],
    // 所有的子分类
    allSubCategoryList: [],
    allSubCategoryMap: {},
    customSubCategoryList: [],

    maliciousSubCategoryList: [],
    // 所有的应用
    allApplicationList: [],
    allApplicationMap: {},

    applicationList: [],
    defaultApplicationList: [],
    maliciousApplicationList: [],

    customApplicationList: [],
  },

  effects: {
    *querySaKnowledgeInfo(_, { call, put }) {
      const { success, result } = yield call(querySaKnowledgeInfo);
      yield put({
        type: 'updateState',
        payload: {
          saKnowledgeInfo: success ? result : {},
        },
      });
    },

    *refreshSaKnowledge(_, { put }) {
      // 刷新知识库信息
      yield put({
        type: 'querySaKnowledgeInfo',
      });
      // 刷新SA规则库中的应用
      yield put({
        type: 'SAKnowledgeModel/queryAllApplications',
      });
      // 刷新SA规则库中的协议
      yield put({
        type: 'metadataModel/queryAllProtocols',
      });
    },

    *queryAllApplications(_, { call, put }) {
      const { success, result }: { success: boolean; result: AppCategoryResponseData } = yield call(
        queryAllApplicaiton,
      );

      let allCategoryList = [] as AppCategoryItem[];
      let allSubCategoryList = [] as AppSubCategoryItem[];
      let customCategoryList = [] as ICustomCategory[];
      // 所有的应用，包括默认的应用、僵木蠕应用、自定义应用
      let allApplicationList = [] as ApplicationItem[];
      let applicationList = [] as ApplicationItem[];
      let customSubCategoryList = [] as ICustomSubCategory[];
      // 默认应用
      const defaultApplicationList = [] as ApplicationItem[];
      // 僵木蠕应用
      const maliciousApplicationList = [] as ApplicationItem[];
      // 自定义应用
      let customApplicationList = [] as ApplicationItem[];

      const allCategoryMap = {};
      const allSubCategoryMap = {};
      const allApplicationMap = {};

      if (success) {
        // 所有的大类列表
        allCategoryList = Array.isArray(result.categoryList) ? result.categoryList : [];
        // 添加自定义的大类
        customCategoryList = Array.isArray(result.customCategoryList)
          ? result.customCategoryList
          : [];
        allCategoryList = [
          ...allCategoryList,
          ...customCategoryList.map((cate) => ({
            ...cate,
            isCustom: true,
            nameText: `${cate.name}[自定义]`,
            descriptionText: cate.description,
          })),
        ];

        // 所有的子分类列表
        allSubCategoryList = Array.isArray(result.subCategoryList) ? result.subCategoryList : [];
        // 添加自定义的子分类
        customSubCategoryList = Array.isArray(result.customSubCategoryList)
          ? result.customSubCategoryList
          : [];
        // 优先以自定义子分类为主
        allSubCategoryList = allSubCategoryList.filter((item1) => {
          return !customSubCategoryList.find(
            (item2) => item2.subCategoryId === item1.subCategoryId,
          );
        });

        allSubCategoryList = [
          ...allSubCategoryList,
          ...customSubCategoryList.map((subCate) => ({
            ...subCate,
            isCustom: true,
            nameText: `${subCate.name}[自定义]`,
            descriptionText: subCate.description,
          })),
        ];

        // 排除掉僵木蠕子分类，这是个威胁
        // 以后有必要的话，可以单独一个页面展示
        allSubCategoryList = allSubCategoryList.filter(
          (subCategory) => !MALICIOUS_SUB_CATEGORY_IDS.includes(subCategory.subCategoryId),
        );
        // 自定义应用
        customApplicationList = Array.isArray(result.customApplicationList)
          ? result.customApplicationList.map((app) => ({
              ...app,
              isCustom: true,
              nameText: `${app.name}[自定义]`,
              descriptionText: app.description as string,
            }))
          : [];

        // sa库里面包含了默认的应用 + 僵木蠕应用，需要分离开
        const saApplication: ApplicationItem[] = Array.isArray(result.applicationList)
          ? result.applicationList
          : [];
        // 分离默认的应用和僵木蠕应用
        for (let index = 0; index < saApplication.length; index += 1) {
          const curApp = saApplication[index];
          // 僵木蠕应用
          if (MALICIOUS_SUB_CATEGORY_IDS.includes(curApp.subCategoryId)) {
            if (
              maliciousApplicationList.find((app) => app.applicationId === curApp.applicationId)
            ) {
              continue;
            }
            maliciousApplicationList.push(curApp);
          } else {
            // 已经包含在自定义应用中，将以自定义的为主
            if (customApplicationList.find((app) => app.applicationId === curApp.applicationId)) {
              continue;
            }
            // 去重
            if (defaultApplicationList.find((app) => app.applicationId === curApp.applicationId)) {
              continue;
            }
            defaultApplicationList.push(curApp);
          }
        }

        // 所有的应用列表
        allApplicationList = ([] as ApplicationItem[]).concat(
          defaultApplicationList,
          customApplicationList,
          maliciousApplicationList,
        );

        // 默认应用 + 自定义应用
        applicationList = ([] as ApplicationItem[]).concat(
          defaultApplicationList,
          customApplicationList,
        );

        // 应用根据子分类组合
        const applicationGroupBySubCategoryId = lodash.groupBy(allApplicationList, 'subCategoryId');
        // 子分类根据大类组合
        const subCategoryGroupByCategoryId = lodash.groupBy(allSubCategoryList, 'categoryId');
        allSubCategoryList.forEach((item: AppSubCategoryItem) => {
          // eslint-disable-next-line no-param-reassign
          item.applicationList = applicationGroupBySubCategoryId[item.subCategoryId];
        });
        allCategoryList.forEach((item: AppCategoryItem) => {
          // eslint-disable-next-line no-param-reassign
          item.subCategoryList = subCategoryGroupByCategoryId[item.categoryId];
        });
        allCategoryList.forEach((category) => {
          allCategoryMap[category.categoryId] = category;
        });
        allSubCategoryList.forEach((subCategory) => {
          allSubCategoryMap[subCategory.subCategoryId] = subCategory;
        });
        allApplicationList.forEach((app) => {
          allApplicationMap[app.applicationId] = app;
        });
      }

      yield put({
        type: 'updateState',
        payload: {
          allCategoryList,
          allCategoryMap,
          customCategoryList,

          allSubCategoryList,
          allSubCategoryMap,
          customSubCategoryList,

          allApplicationList,
          allApplicationMap,

          applicationList,
          defaultApplicationList,
          maliciousApplicationList,
          customApplicationList,
        },
      });

      return {
        allCategoryList,
        allSubCategoryList,
        allApplicationList,
        allCategoryMap,
        allSubCategoryMap,
        allApplicationMap,
      };
    },
    /**
     * 根据应用ID获取应用名称、应用描述、应用大类和应用子分类的名称和描述信息
     */
    *getApplicationAndCategoryInfo({ payload }, { select }) {
      const { applicationId, maliciousApplicationId } = payload;
      const { allCategoryMap, allSubCategoryMap, allApplicationMap }: SAKnowledgeModelState =
        yield select((state: ConnectState) => state.SAKnowledgeModel);

      let applicationName = '';
      let applicationDescription = '';
      // 分类
      const categoryId = '';
      let categorynName = '';
      let categorynDescription = '';
      // 子分类
      let subCategoryId = '';
      let subCategorynName = '';
      let subCategorynDescription = '';

      const applicationInfo = allApplicationMap[applicationId];
      if (applicationInfo) {
        applicationName = applicationInfo.nameText;
        applicationDescription = applicationInfo.descriptionText || '';

        subCategoryId = applicationInfo.subCategoryId;

        const subCategoryInfo = allSubCategoryMap[subCategoryId];

        const cateId = applicationInfo.categoryId;
        const cateInfo = allCategoryMap[cateId];

        if (subCategoryInfo) {
          subCategorynName = subCategoryInfo.nameText;
          subCategorynDescription = subCategoryInfo.descriptionText || '';

          // 因为子分类也可以自定义，所以要根据子分类查找分类
          const categoryInfo = allCategoryMap[subCategoryInfo.categoryId];
          if (categoryInfo) {
            categorynName = cateInfo.nameText;
            categorynDescription = cateInfo.descriptionText || '';
          }
        }
      }

      let maliciousApplicationName = '';
      let maliciousApplicationDescription = '';
      if (maliciousApplicationId) {
        // 处理僵木蠕应用ID
        const maliciousApplicationInfo = allApplicationMap[maliciousApplicationId];

        maliciousApplicationName = maliciousApplicationInfo?.nameText;
        maliciousApplicationDescription = maliciousApplicationInfo?.descriptionText || '';
      }

      return {
        categoryId,
        application_category_id: categoryId,
        categorynName,
        application_category_name: categorynName,
        categorynDescription,
        application_category_description: categorynDescription,

        subCategoryId,
        application_subcategory_id: subCategoryId,
        subCategorynName,
        application_subcategory_name: subCategorynName,
        subCategorynDescription,
        application_subcategory_description: subCategorynDescription,

        applicationId,
        application_id: applicationId,
        applicationName,
        application_name: applicationName,
        applicationDescription,
        application_description: applicationDescription,

        maliciousApplicationId,
        malicious_application_id: maliciousApplicationId,
        maliciousApplicationName,
        malicious_application_name: maliciousApplicationName,
        maliciousApplicationDescription,
        malicious_application_description: maliciousApplicationDescription,
      };
    },
  },

  reducers: {},
} as SAKnowledgeModel);

export default Model;
