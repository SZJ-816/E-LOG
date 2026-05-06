import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.hadoop.fs._

object SparkLogAnalysis {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("LogAnalysisPVUV")
      .master("spark://spark:7077")
      .config("spark.sql.streaming.checkpoint.location", "/tmp/spark-checkpoint")
      .getOrCreate()

    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://namenode:9000")

    val schema = StructType(Seq(
      StructField("timestamp", StringType),
      StructField("ip", StringType),
      StructField("userId", StringType),
      StructField("method", StringType),
      StructField("url", StringType),
      StructField("status", IntegerType),
      StructField("responseTime", LongType),
      StructField("message", StringType)
    ))

    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "kafka:9092")
      .option("subscribe", "log-topic")
      .option("startingOffsets", "latest")
      .load()

    val valueDF = kafkaDF.select(from_json(col("value").cast(StringType), schema).as("data")).select("data.*")

    val cleanDF = valueDF.filter(col("url").isNotNull)

    val pvDF = cleanDF.groupBy(window(col("timestamp"), "1 minute").as("window"))
      .agg(count("*").as("pv"))
      .withColumn("date", date_format(col("window.start"), "yyyy-MM-dd"))
      .withColumn("minute", date_format(col("window.start"), "HH:mm"))

    val uvDF = cleanDF.groupBy(window(col("timestamp"), "1 minute").as("window"), col("userId"))
      .agg(count("*").as("count"))
      .groupBy("window")
      .agg(count("*").as("uv"))
      .withColumn("date", date_format(col("window.start"), "yyyy-MM-dd"))
      .withColumn("minute", date_format(col("window.start"), "HH:mm"))

    val errorDF = cleanDF.filter(col("status") >= 400)
      .groupBy(window(col("timestamp"), "1 minute").as("window"))
      .agg(count("*").as("errorCount"))
      .withColumn("date", date_format(col("window.start"), "yyyy-MM-dd"))
      .withColumn("minute", date_format(col("window.start"), "HH:mm"))

    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    def ensurePath(path: String): Unit = {
      val hdfsPath = new org.apache.hadoop.fs.Path(path)
      if (!fs.exists(hdfsPath)) {
        fs.mkdirs(hdfsPath)
      }
    }

    ensurePath("/log-analysis/spark/pv")
    ensurePath("/log-analysis/spark/uv")
    ensurePath("/log-analysis/spark/error")

    val pvQuery = pvDF.writeStream
      .format("csv")
      .option("header", "true")
      .option("checkpointLocation", "/tmp/spark-pv-checkpoint")
      .outputMode("append")
      .start("/log-analysis/spark/pv")

    val uvQuery = uvDF.writeStream
      .format("csv")
      .option("header", "true")
      .option("checkpointLocation", "/tmp/spark-uv-checkpoint")
      .outputMode("append")
      .start("/log-analysis/spark/uv")

    val errorQuery = errorDF.writeStream
      .format("csv")
      .option("header", "true")
      .option("checkpointLocation", "/tmp/spark-error-checkpoint")
      .outputMode("append")
      .start("/log-analysis/spark/error")

    spark.streams.awaitAnyTermination()
  }
}
