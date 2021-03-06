package transformers

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}
import utils.ResourceLoader

import scala.collection.mutable

/**
  * Created by faiaz on 13.01.17.
  */
class WordsRemover(override val uid: String = Identifiable.randomUID("wordRemover"))
  extends Transformer
    with SingleTransformer {
  import WordsRemover._

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    val t  = udf { arr: mutable.WrappedArray[String] =>
      arr
        .map(_.split(' ')
          .map(_.toLowerCase)
          .filterNot(w => words.contains(w)))
        .map(_.mkString(" "))
    }
    dataset.select(col("*"), t(col($(inputCol))).as($(outputCol)))
  }

  override def transformSchema(schema: StructType): StructType = {
    StructType(schema.fields :+ StructField(outputCol.name, StringType, nullable = false))
  }

  override def copy(extra: ParamMap): TextCleaner = {defaultCopy(extra)}
}

object WordsRemover
  extends DefaultParamsReadable[WordsRemover]
    with ResourceLoader {

  val words: Array[String] = loadResources("/stopWords/english.txt")

  override def load(path: String): WordsRemover = super.load(path)
}
