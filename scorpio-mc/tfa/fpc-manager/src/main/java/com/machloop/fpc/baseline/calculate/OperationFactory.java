package com.machloop.fpc.baseline.calculate;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.machloop.fpc.common.helper.AggsFunctionEnum;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
public class OperationFactory {

  public static Operation createOperation(AggsFunctionEnum operationType) {
    Operation operation;
    switch (operationType) {
      case MIN:
        operation = new MinOperation();
        break;
      case MAX:
        operation = new MaxOperation();
        break;
      case MEAN:
        operation = new MeanOperation();
        break;
      case MEDIAN:
        operation = new MedianOperation();
        break;
      default:
        throw new IllegalArgumentException("unsupport operation, failed to create operation");
    }
    return operation;
  }

  public static class MeanOperation implements Operation {
    @Override
    public double operate(long[] elements) {
      SummaryStatistics stats = new SummaryStatistics();
      for (long element : elements) {
        stats.addValue(element);
      }
      return stats.getMean();
    }
  }

  public static class MinOperation implements Operation {
    @Override
    public double operate(long[] elements) {
      SummaryStatistics stats = new SummaryStatistics();
      for (long element : elements) {
        stats.addValue(element);
      }
      return stats.getMin();
    }
  }

  public static class MaxOperation implements Operation {
    @Override
    public double operate(long[] elements) {
      SummaryStatistics stats = new SummaryStatistics();
      for (long element : elements) {
        stats.addValue(element);
      }
      return stats.getMax();
    }
  }

  public static class MedianOperation implements Operation {
    @Override
    public double operate(long[] elements) {
      DescriptiveStatistics stats = new DescriptiveStatistics();
      for (long element : elements) {
        stats.addValue(element);
      }
      return stats.getPercentile(50);
    }
  }
}
