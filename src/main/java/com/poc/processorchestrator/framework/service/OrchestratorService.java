package com.poc.processorchestrator.framework.service;


import com.poc.processorchestrator.common.model.EventInstance;
import com.poc.processorchestrator.common.service.CommonUtils;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrchestratorService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String topic;

    public String publishEvent(EventInstance eventInstance){

        log.info("Correlation Key : {}", eventInstance.getCorrelation_key());
        CompletableFuture<SendResult<String, String>> future  = kafkaTemplate.send(topic, eventInstance.getCorrelation_key(), CommonUtils.toJSON(eventInstance));
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Offset : {} for Correlation Key : {}",result.getRecordMetadata().offset(), eventInstance.getCorrelation_key());
            } else {
                log.info("Correlation key : {} - Unable to send message due to error : {}",eventInstance.getCorrelation_key(), ex.getMessage());
            }
        });

/*        zeebeClient.newPublishMessageCommand()
                .messageName(eventInstance.getEvent().getEventcode())
                .correlationKey(eventInstance.getCorrelation_key())
                .variables(eventInstance)
                .send()
                .join();*/
        return "Published";
    }
}
