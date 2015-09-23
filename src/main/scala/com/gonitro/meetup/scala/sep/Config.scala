package com.gonitro.meetup.scala.sep

import java.io.File

import com.typesafe.config.ConfigFactory


object Config {
  private lazy val cfg = ConfigFactory.load().getConfig("nitro.document.processing")

  lazy val documentGw = cfg.getString("document_gateway")
  lazy val tmpFolder = new File(cfg.getString("tmp_folder"))

  object elasticseach {
    private lazy val esConf = cfg.getConfig("elasticsearch")
    lazy val host = esConf.getString("host")
    lazy val port = esConf.getInt("port")
  }

  object kafka {
    private lazy val kafka = cfg.getConfig("kafka")
    lazy val groupId = kafka.getString("consumer_group")
    lazy val zk = kafka.getString("zookeeper")
    lazy val brokers = kafka.getString("brokers")
    lazy val schemaRegistry = kafka.getString("schema_registry")

    object topics {
      private lazy val tcfg = kafka.getConfig("topics")
      lazy val document = tcfg.getString("document")
      lazy val rasterization = tcfg.getString("rasterization")
      lazy val distillation = tcfg.getString("distillation")
    }

  }

}
