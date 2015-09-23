package com.gonitro.meetup.scala.sep

import java.io.File
import java.net.URL

import com.nitro.platform.messages.DocumentMetadata
import com.nitro.scalaAvro.runtime.RecordDecoder
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.avro.generic.GenericRecord
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory


object DocumentProcessor extends App {

  val logger = LoggerFactory.getLogger(getClass)

  val sparkConf = new SparkConf().setAppName("Document ingestion processing").setMaster("local[2]").setAppName("Document processing")
  val sparkContext = new StreamingContext(sparkConf, Seconds(15))

  val recordDecoder = new RecordDecoder(Config.kafka.schemaRegistry)

  val stream = KafkaUtils.createStream[String, Array[Byte], StringDecoder, DefaultDecoder](
   ssc = sparkContext, topics = Map(Config.kafka.topics.document -> 1), storageLevel = StorageLevel.MEMORY_ONLY,
   kafkaParams = Map("zookeeper.connect" -> Config.kafka.zk, "group.id" -> Config.kafka.groupId)
  )

  val metaStream = stream.map { case (k, v) => (k, DocumentMetadata.fromMutable(recordDecoder.decode(v).asInstanceOf[GenericRecord])) }

  private val pdfFiles = metaStream.filter(_._2.contentType == "application/pdf")
    .map { case (k, meta) => (meta, fetchFileFromMessage(k, meta)) }
  val pdfDocs = pdfFiles.map { case (meta, file) => (meta, TextExtractor.parseFile(file)) }
  val texts = pdfDocs.map { case (meta, doc) => (meta, TextExtractor.extractText(doc)) }.cache()
  val wordStream = texts.map { case (meta, text) => (meta, text.split("""[\ \n\r\t\u00a0]+""").toList.map(_.replaceAll("""[,;\.]$""", "").trim.toLowerCase()).filter(_.length > 1)) }
  texts.foreachRDD( rdd => rdd.foreach { case (meta,text) => indexText(meta.id, text) } )

  val wordCountStream = wordStream.flatMap(_._2).map(word => (word, 1)).reduceByKey(_ + _)
  val totalWordCountStream = wordStream.map(_._2.size)
  val totalWords = totalWordCountStream.reduce(_+_)

  val sortedWordCount = wordCountStream.transform(rdd => rdd.sortBy(_._2, ascending = false))

  sortedWordCount.foreachRDD(rdd => println(rdd.toDebugString))
  sortedWordCount.print(30)
  totalWords.print()

  sparkContext.start()
  sparkContext.awaitTermination()

  def fetchFileFromMessage(key: String, meta: DocumentMetadata): File = {
    import sys.process._

    if (!Config.tmpFolder.exists()) Config.tmpFolder.mkdirs()

    logger.debug("Downloading file from {}", meta.uri)
    val file = new File(Config.tmpFolder, meta.id)
    new URL(meta.uri) #> file !!

    logger.info("Document downloaded from {} into {}{}", meta.uri, file.getCanonicalPath, "")

    file
  }

  def indexText(title: String, text: String) = { /* Here we could index the files in solr/es */ }

}
