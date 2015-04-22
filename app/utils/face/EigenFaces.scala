package utils.face

import cern.colt.matrix.DoubleMatrix2D
import cern.colt.matrix.impl.DenseDoubleMatrix2D

/**
 * Helper object for creating EigenFaces from matrices.
 */
object EigenFaces {

  /**
   * Convenience method for computing the average face of multiple faces.
   * @param pixelMatrix
   */
  def computeAverageFace(pixelMatrix: Array[Array[Double]]): Array[Double] = MatrixHelpers.computeMeanColumn(pixelMatrix)

  /**
   * Computes the EigenFaces matrix using a pixel matrix of multiple images.
   * @param pixelMatrix
   * @param meanColumn
   */
  def computeEigenFaces(pixelMatrix: Array[Array[Double]], meanColumn: Array[Double]): DoubleMatrix2D = {
    val diffMatrix = MatrixHelpers.computeDifferenceMatrixPixels(pixelMatrix, meanColumn)
    val eigenVectors = MatrixHelpers.computeEigenVectors(diffMatrix)
    computeEigenFaces(eigenVectors, diffMatrix)
  }

  /**
   * Computes the EigenFaces matrix for a dataset of Eigen vectors and a diff matrix.
   * @param eigenVectors
   * @param diffMatrix
   */
  def computeEigenFaces(eigenVectors: DoubleMatrix2D, diffMatrix: Array[Array[Double]]): DoubleMatrix2D = {
    val pixelCount = diffMatrix.length
    val imageCount = eigenVectors.columns()
    val rank = eigenVectors.rows()
    val eigenFaces = Array.ofDim[Double](pixelCount, rank)

    (0 to (rank-1)).foreach { i =>
      var sumSquare = 0.0
      (0 to (pixelCount-1)).foreach { j =>
        (0 to (imageCount-1)).foreach { k =>
          eigenFaces(j)(i) += diffMatrix(j)(k) * eigenVectors.get(i,k)
        }
        sumSquare += eigenFaces(j)(i) * eigenFaces(j)(i)
      }
      var norm = Math.sqrt(sumSquare)
      (0 to (pixelCount-1)).foreach { j =>
        eigenFaces(j)(i) /= norm
      }
    }
    val eigenFacesMatrix = new DenseDoubleMatrix2D(pixelCount, rank)
    eigenFacesMatrix.assign(eigenFaces)
  }

  /**
   * Calculates a distance score between a mean Pixels/EigenFaces model in comparison to an image subject.
   * @param meanPixels
   * @param eigenFaces
   * @param subjectPixels
   */
  def computeDistance(meanPixels: Array[Double], eigenFaces: DoubleMatrix2D, subjectPixels: Array[Double]): Double = {
    val diffPixels = computeDifferencePixels(subjectPixels, meanPixels)
    val weights = computeWeights(diffPixels, eigenFaces)
    val reconstructedEigenPixels = reconstructImageWithEigenFaces(weights, eigenFaces, meanPixels)
    computeImageDistance(subjectPixels, reconstructedEigenPixels)
  }

  /**
   * Computes the distance between two images.
   * @param pixels1
   * @param pixels2
   */
  private def computeImageDistance(pixels1: Array[Double], pixels2: Array[Double]): Double = {
    var distance = 0.0
    val pixelCount = pixels1.length
    (0 to (pixelCount-1)).foreach { i =>
      var diff = pixels1(i) - pixels2(i)
      distance += diff * diff
    }
    Math.sqrt(distance / pixelCount)
  }

  /**
   * Computes the weights of faces vs. EigenFaces.
   * @param diffImagePixels
   * @param eigenFaces
   */
  private def computeWeights(diffImagePixels: Array[Double], eigenFaces: DoubleMatrix2D): Array[Double] = {
    val pixelCount = eigenFaces.rows()
    val eigenFaceCount = eigenFaces.columns()

    val weights = new Array[Double](eigenFaceCount)
    (0 to (eigenFaceCount-1)).foreach { i=>
      (0 to (pixelCount-1)).foreach { j =>
        weights(i) += diffImagePixels(j) * eigenFaces.get(j,i)
      }
    }
    weights
  }

  /**
   * Computes the difference pixels between a subject image and a mean image.
   * @param subjectPixels
   * @param meanPixels
   */
  private def computeDifferencePixels(subjectPixels: Array[Double], meanPixels: Array[Double]): Array[Double] = {
    val pixelCount = subjectPixels.length
    val diffPixels = new Array[Double](pixelCount)

    (0 to (pixelCount-1)).foreach { i =>
      diffPixels(i) = subjectPixels(i) - meanPixels(i)
    }
    diffPixels
  }

  /**
   * Reconstructs an image using Eigen Faces and weights.
   * @param weights
   * @param eigenFaces
   * @param meanPixels
   */
  private def reconstructImageWithEigenFaces(weights: Array[Double], eigenFaces: DoubleMatrix2D, meanPixels: Array[Double]) = {
    val pixelCount = eigenFaces.rows()
    val eigenFaceCount = eigenFaces.columns()

    // reconstruct image from weight and eigenfaces
    val reconstructedPixels = new Array[Double](pixelCount)
    (0 to (eigenFaceCount-1)).foreach { i =>
      (0 to (pixelCount-1)).foreach { j =>
        reconstructedPixels(j) += weights(i) * eigenFaces.get(j, i)
      }
    }

    // add mean
    (0 to (pixelCount-1)).foreach { i =>
      reconstructedPixels(i) += meanPixels(i)
    }

    var min = Double.MaxValue
    var max = -Double.MaxValue
    (0 to (reconstructedPixels.length-1)).foreach { i =>
      min = Math.min(min, reconstructedPixels(i))
      max = Math.max(max, reconstructedPixels(i))
    }

    val normalizedReconstructedPixels = new Array[Double](pixelCount)
    (0 to (reconstructedPixels.length-1)).foreach { i=>
      normalizedReconstructedPixels(i) = (255.0 * (reconstructedPixels(i) - min)) / (max - min)
    }
    normalizedReconstructedPixels
  }

}
