(ns d-fold.sqs
  (:require [cheshire.core :refer [generate-string parse-string]])
  (:import [com.amazonaws.regions Regions]
           [com.amazonaws.services.sqs AmazonSQSClient]
           [com.amazonaws.services.sqs.model ReceiveMessageRequest]))

(defn- sqs-client [region]
  (-> (AmazonSQSClient.)
      (.withRegion (Regions/fromName region))))

(defn create-queue [region queue-name]
  (let [queue-url (-> (sqs-client region)
                      (.createQueue queue-name)
                      (.getQueueUrl))]
    (loop []
      (let [urls (.getQueueUrls (.listQueues (sqs-client region) queue-name))]
        (when (empty? urls)
          (Thread/sleep 200)
          (recur))))
    queue-url))

(defn send-message [region queue-url payload]
  (.sendMessage (sqs-client region)
                queue-url
                (generate-string payload)))

(defn receive-message [region queue-url wait-in-seconds]
  (let [from-json #(parse-string % true)
        raw-message (-> (.receiveMessage (sqs-client region) (-> (ReceiveMessageRequest.)
                                                                 (.withQueueUrl queue-url)
                                                                 (.withWaitTimeSeconds (int wait-in-seconds))))
                        (.getMessages)
                        (first))]
    (when raw-message
      (.deleteMessage (sqs-client region) queue-url (.getReceiptHandle raw-message))
      (-> raw-message .getBody from-json))))

(defn receive-messages [region queue-url wait-in-seconds handler-fn]
  (let [to-json #(parse-string % true)
        raw-messages (-> (.receiveMessage (sqs-client region) (-> (ReceiveMessageRequest.)
                                                                  (.withQueueUrl queue-url)
                                                                  (.withWaitTimeSeconds (int wait-in-seconds))))
                         (.getMessages))]
    (map (fn [raw-message]
           (let [handle (.getReceiptHandle raw-message)
                 payload (-> raw-message .getBody to-json)
                 ret-value (handler-fn payload)]
             (.deleteMessage (sqs-client region) queue-url handle)
             ret-value))
         raw-messages)))

(defn delete-queue [region queue-url]
  (.deleteQueue (sqs-client region) queue-url))
