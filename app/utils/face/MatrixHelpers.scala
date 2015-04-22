package utils.face

import cern.colt.matrix.linalg.EigenvalueDecomposition
import cern.colt.matrix.{DoubleFactory2D, DoubleMatrix2D}

/**
 * Methods for converting image matrices to covariance matrices.
 *
 * @note Adapted from https://github.com/fredang/mahout-eigenface-example.
 */
object MatrixHelpers {

  /**
   * Merges a list of arrays of pixels for multiple images into a single matrix.
   * @param matrices
   * @param width
   * @param height
   * @return
   */
  def mergePixelMatrices(matrices: List[Array[Double]], width: Int, height: Int): Array[Array[Double]] = {
    val pixelLength = width * height

    val pixelMatrix = Array.ofDim[Double](pixelLength, matrices.size)

    var matrixNo = 0
    matrices.foreach { pixels =>
      (0 to (pixelLength - 1)).foreach { pixelNo =>
        pixelMatrix(pixelNo)(matrixNo) = pixels(pixelNo)
      }
      matrixNo += 1
    }
    pixelMatrix
  }

  /**
   * Computes the mean for each column in a pixel matrix.
   * @param pixelMatrix
   * @return
   */
  def computeMeanColumn(pixelMatrix: Array[Array[Double]]): Array[Double] = {
    val meanColumn = new Array[Double](pixelMatrix.length)
    val columnCount = pixelMatrix(0).length

    (0 to (pixelMatrix.length - 1)).foreach { i =>
      var sum: Double = 0.0
      (0 to (columnCount - 1)).foreach { j =>
        sum += pixelMatrix(i)(j)
      }
      meanColumn(i) = sum / columnCount
    }
    meanColumn
  }

  /**
   * Computes a difference matrix.
   * @param pixelMatrix
   * @param meanColumn
   * @return
   */
  def computeDifferenceMatrixPixels(pixelMatrix: Array[Array[Double]], meanColumn: Array[Double]): Array[Array[Double]] = {
    val rowCount = pixelMatrix.length
    val columnCount = pixelMatrix(0).length

    val diffMatrixPixels = Array.ofDim[Double](rowCount, columnCount)

    (0 to (pixelMatrix.length - 1)).foreach { i =>
      (0 to (columnCount - 1)).foreach { j =>
        diffMatrixPixels(i)(j) = pixelMatrix(i)(j) - meanColumn(i)
      }
    }

    diffMatrixPixels
  }

  /**
   * Computes a covariance multi-dimensional array.
   * @param pixelMatrix
   * @return
   */
  def computeCovarianceMatrix(pixelMatrix: Array[Array[Double]], diffMatrixPixels: Array[Array[Double]]): Array[Array[Double]] = {
    val rowCount = pixelMatrix.length
    val columnCount = pixelMatrix(0).length

    val covarianceMatrix = Array.ofDim[Double](columnCount, columnCount)

    (0 to (columnCount - 1)).foreach { i =>
      (0 to (columnCount - 1)).foreach { j =>
        var sum: Double = 0.0
        (0 to (rowCount - 1)).foreach { k =>
          sum += diffMatrixPixels(k)(i) * diffMatrixPixels(k)(j)
        }
        covarianceMatrix(i)(j) = sum
      }
    }

    covarianceMatrix
  }

  /**
   * Computes an Eigenvector matrix from the normalized pixel matrix
   * @param diffMatrix
   * @return
   */
  def computeEigenVectors(diffMatrix: Array[Array[Double]]): DoubleMatrix2D = {
    val factory = DoubleFactory2D.dense
    val diffDoubleMatrix = factory.make(diffMatrix.length, diffMatrix(0).length).assign(diffMatrix)

    val shortDoubleMatrix = diffDoubleMatrix.zMult(diffDoubleMatrix, null, 1.0, 0.0, false, true)

    val eigenVectorMatrix = new EigenvalueDecomposition(shortDoubleMatrix).getV
    diffDoubleMatrix.zMult(eigenVectorMatrix, null)
  }
}
