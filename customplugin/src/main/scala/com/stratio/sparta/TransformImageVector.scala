package com.stratio.sparta


import scala.util.Try

import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods.parse

import com.stratio.sparta.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.lite.streaming.LiteCustomStreamingTransform
import com.stratio.sparta.sdk.lite.streaming.models.OutputStreamingTransformData
import com.stratio.sparta.sdk.lite.streaming.models.ResultStreamingData
import com.stratio.sparta.sdk.lite.validation.ValidationResult
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField

import com.stratio.sparta.ImageParser
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import collection.mutable
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.Milliseconds

class TransformImageVector( 
  sparkSession: SparkSession,
  streamingContext: StreamingContext,
  properties: Map[String, String]) extends LiteCustomStreamingTransform(sparkSession, streamingContext, properties) {

  lazy val resize: Option[String] = Try(Option(properties.getString("resize"))).getOrElse(None).notBlank
  
  
  override def validate(): ValidationResult = {
    var validation = ValidationResult(valid = true, messages = Seq.empty)
    
    if (resize.isEmpty) {
      validation = ValidationResult(
        valid = false,
        messages = validation.messages :+ "Specify an option key 'resize' with the name of the second column ")
    }else{
      logger.info(s"resize: $resize")
    }
    
//    if (!resize.contains("x")){
//      validation = ValidationResult(
//        valid = false,
//        messages = validation.messages :+ "resize field must be like 800x600")
//    }
    
    validation
  }
  
  override def transform(inputData: Map[String, ResultStreamingData]): OutputStreamingTransformData = {

//    logger.error("TransformImageVector: obtaining data")
    val (_, value) = inputData.head

    logger.error("TransformImageVector: transforming data")
    val kafkaResult = value.data.mapPartitions(rows => 
      rows.map(row => Row(ImageParser.getImageVector(row.getString(0), "28x28"))) 
      )
 
//    val kafkaResult = value.data.mapPartitions(rows => 
//      rows.map(row => Row("1,2,2,3,324,23,423,423,423,423")) 
//      )
      
    //logger.error("TransformImageVector: method transfor generating output",kafkaResult.count())

    OutputStreamingTransformData(kafkaResult,Some(StructType(Seq(StructField("field1", StringType)))))
     
    
    
  }

}
