module.exports = {
  extends: [require.resolve('@umijs/fabric/dist/eslint')],
  globals: {
    page: true,
    REACT_APP_ENV: true,
    appConfig: true,
  },
  rules: {
    'no-restricted-properties': 0,
    'react/self-closing-comp': 1,
    'no-restricted-globals': 0,
    '@typescript-eslint/no-use-before-define': 1,
    // 'no-unused-vars': 1,
    '@typescript-eslint/no-unused-vars': 1,
    'no-nested-ternary': 1,
    'prefer-promise-reject-errors': 0,
    'react-hooks/exhaustive-deps': 1,
    /**
     * 允许存在空的接口定义
     */
    '@typescript-eslint/no-empty-interface': 0,
    'no-continue': 0,
    'react-hooks/rules-of-hooks': 2,
    '@typescript-eslint/no-shadow': 1,
  },
};
