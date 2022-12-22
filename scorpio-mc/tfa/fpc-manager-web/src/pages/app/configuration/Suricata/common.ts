import { ERuleDirection, ERuleParseState, ERuleProtocol, ERuleSignatureSeverity, ERuleSource, ERuleState, ERuleTarget } from "./typings";

export const RuleProtocolOptions = Object.keys(ERuleProtocol).map((key) => {
  return {
    label: key,
    value: ERuleProtocol[key],
  };
});

export const RuleDirectionOptions = Object.keys(ERuleDirection)
  .filter((key) => Number(key).toString() === 'NaN')
  .map((key) => {
    return {
      label: key,
      value: ERuleDirection[key],
    };
  });

export const RuleTargetOptions = Object.keys(ERuleTarget)
  .filter((key) => Number(key).toString() === 'NaN')
  .map((key) => {
    return {
      label: key,
      value: ERuleTarget[key],
    };
  });

export const RuleStateOptions = Object.keys(ERuleState).map(key => {
  return {
    label: key,
    value: ERuleState[key]
  }
})

export const RuleSourceOptions = Object.keys(ERuleSource).map(key => {
  return {
    label: key,
    value: ERuleSource[key]
  }
})

export const RuleParseStateOptions = Object.keys(ERuleParseState).map(key => {
  return {
    label: key,
    value: ERuleParseState[key]
  }
})

export const RuleSignatureSeverityOptions = Object.keys(ERuleSignatureSeverity).map(key => {
  return {
    label: key,
    value: ERuleSignatureSeverity[key]
  }
})
