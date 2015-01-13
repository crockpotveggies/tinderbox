package utils

import utils.mcl.{MarkovClustering, SparseMatrix}


/**
 * MarkovClustering implements the Markov clustering (MCL) algorithm for graphs, using a HashMap-based sparse representation of a Markov matrix, i.e., an adjacency matrix m that is normalised to one. Elements in a column / node can be interpreted as decision probabilities of a random walker being at that node.
 *
 * @see http://java-ml.sourceforge.net/api/0.1.1/net/sf/javaml/clustering/mcl/MarkovClustering.html
 */
class MarkovClusterer(
  val maxResidual: Double,
  val pGamma: Double,
  val loopGain: Double,
  val maxZero: Double
) {

  /**
   * Default constructor.
   */
  def this() = this(0.001, 2.0, 0.0, 0.001)

  /**
   * Convenience method for using pixel arrays with the Markov clustering algorithm in JavaML.
   * @param dataset
   */
  def cluster(dataset: Array[Array[Double]]) = {
    val matrix = new MarkovClustering().run(new SparseMatrix(dataset), maxResidual, pGamma, loopGain, maxZero)
    println(dataset.size)
    dataset(0).foreach( o => println(o) )

    // convert matrix to output dataset
    val sparseMatrixSize = matrix.getSize
    // find number of attractors (non-zero values) in diagonal
    var attractors = 0
    (0 to (sparseMatrixSize(0)-1)).foreach { i =>
      var value = matrix.get(i, i)
      if(value != 0) attractors += 1
    }
    // create cluster for each attractor with value close to 1
    val finalClusters = Array[Array[Array[Double]]]()

    (0 to (sparseMatrixSize(0)-1)).foreach { i =>
      val cluster = Array[Array[Double]]()
      val value = matrix.get(i, i)
      if (value >= 0.98) {
        (0 to (sparseMatrixSize(0)-1)).foreach { j =>
          if(value != 0) {
            cluster ++ dataset(j)
          }
        }
        finalClusters :+ cluster
      }
    }

    finalClusters
  }
}
