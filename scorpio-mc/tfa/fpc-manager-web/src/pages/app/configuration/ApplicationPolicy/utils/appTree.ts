import type { AppCategoryItem } from '../../SAKnowledge/typings';

/** 将简易appid转换成完整appid */
export function convertSimpleAppIdToComplex(applicationId: string[]) {
  return ((applicationId as string[]) || [])?.map((idSplitByMinus) => {
    const [categoryId, subCategoryId, appId] = idSplitByMinus.split('-');
    return {
      categoryId: categoryId,
      subCategoryId: subCategoryId ? subCategoryId : null,
      applicationId: appId ? appId : null,
    };
  });
}

/** 将复杂appid转换为简单appid */
export function convertComplexAppIdToSimple(
  appList: {
    categoryId: string | null;
    subCategoryId: string | null;
    applicationId: string | null;
  }[],
) {
  return appList?.map(({ categoryId, subCategoryId, applicationId }) => {
    return `${categoryId}${subCategoryId ? `-${subCategoryId}` : ''}${
      applicationId ? `-${applicationId}` : ''
    }`;
  });
}

/** 获得app树 */
export function getApplicationTree(allCategoryList: AppCategoryItem[]) {
  return allCategoryList?.map((category) => {
    return {
      title: category.nameText,
      value: category.categoryId,
      key: category.categoryId,
      children: category.subCategoryList?.map((subCategory) => {
        return {
          title: subCategory.nameText,
          value: `${category.categoryId}-${subCategory.subCategoryId}`,
          key: `${category.categoryId}-${subCategory.subCategoryId}`,
          children: subCategory.applicationList?.map((app) => {
            return {
              title: app.nameText,
              value: `${category.categoryId}-${subCategory.subCategoryId}-${app.applicationId}`,
              key: `${category.categoryId}-${subCategory.subCategoryId}-${app.applicationId}`,
            };
          }),
        };
      }),
    };
  });
}
