import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions._

object AmazonFineFoodsParser {
	/**
		* The first argument is the path to the reviews csv file
		*
		* @param args
		*/
	def main(args: Array[String]): Unit = {
		val spark = SparkSession.builder()
			.config(new SparkConf().setMaster("local[2]").set("spark.executor.memory","512m"))
			.getOrCreate()

		import spark.implicits._

		val reviewDataset = spark.read
			.format("csv")
			.option("header", "true")
			.option("escape", "\"")
			.schema(StructType(
				Seq(
					StructField("Id", IntegerType),
					StructField("ProductId", StringType),
					StructField("UserId", StringType),
					StructField("ProfileName", StringType),
					StructField("HelpfulnessNumerator", IntegerType),
					StructField("HelpfulnessDenominator", IntegerType),
					StructField("Score", IntegerType),
					StructField("Time", LongType),
					StructField("Summary", StringType),
					StructField("Text", StringType)
				)
			))
			.csv(args(0))
			.as[Review]

		if (args.length > 1 && args(1) == "translate=true") {
			translate(reviewDataset.map(review => (review.Id, review.Text)), 1000, spark, new GoogleTranslateService(), "en", "fr")
		} else {
			findMostActiveUsers(reviewDataset, spark, 1000).show

			findMostCommentedFoodItems(reviewDataset, spark, 1000).show

			findMostUsedWords(reviewDataset, spark, 1000).show
		}
	}

	def findMostActiveUsers(reviewDataset: Dataset[Review], spark: SparkSession, limit: Int): Dataset[Row] = {
		reviewDataset.groupBy("ProfileName")
			.count
			.sort(desc("count"))
			.limit(limit)
			.orderBy("ProfileName")
	}

	// this method and one above can be rewritten to more generic function where groupBy field is an argument
	def findMostCommentedFoodItems(reviewDataset: Dataset[Review], spark: SparkSession, limit: Int): Dataset[Row] = {
		reviewDataset.groupBy("ProductId")
			.count
			.sort(desc("count"))
			.limit(limit)
			.orderBy("ProductId")
	}

	// to get more relevant result, it is better to filter out all stopwords
	def findMostUsedWords(reviewDataset: Dataset[Review], spark: SparkSession, limit: Int): Dataset[(String, Long)] = {
		import spark.implicits._

		reviewDataset.flatMap(_.Text.split("\\s+"))
			.map((_, 1))
			.groupByKey(_._1)
			.count
			.sort(desc("count(1)"))
			.limit(limit)
			.orderBy("value")
	}

	def translate(textDataSet: Dataset[(Integer, String)], chunkSize: Integer, spark: SparkSession,
								translateService: GoogleTranslateService, inputLanguage: String, outputLanguage: String
							 ): Array[(Integer, String)] = {
		import spark.implicits._

		textDataSet.map(tuple => {
			if (tuple._2.length > chunkSize) {
				(tuple._1, """(\.;!\?)""".r.findAllIn(tuple._2).map(chunk => translateService.translate(inputLanguage, outputLanguage, chunk)).mkString)
			} else {
				(tuple._1, translateService.translate(inputLanguage, outputLanguage, tuple._2))
			}
		}).collect()
	}
}