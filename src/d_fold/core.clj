(ns d-fold.core
  (:require [cheshire.core :refer [generate-string parse-string parse-stream]]
            [clojure.core.async :refer [chan thread go go-loop >! <! <!!]]
            [clojure.java.io :as io]
            [clojure.core.reducers :as r]
            [d-fold.adjacent-queue :as aq]
            [d-fold.sqs :as sqs]
            [serializable.fn :as s])
  (:import [com.amazonaws ClientConfiguration]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.regions Regions]
           [com.amazonaws.services.lambda.model InvokeRequest]
           [com.amazonaws.services.lambda AWSLambdaClient]))

(def aws-credentials
  (.getCredentials (DefaultAWSCredentialsProviderChain.)))

(defn- parse-result [result]
  (-> result
      (.getPayload)
      (.array)
      (java.io.ByteArrayInputStream.)
      (io/reader)
      (parse-stream true)))

(defn- invoke-lambda [payload lambda-function-name region]
  (let [client-config (-> (ClientConfiguration.)
                          (.withSocketTimeout (* 6 60 1000)))
        client (-> (AWSLambdaClient. aws-credentials client-config)
                   (.withRegion (Regions/fromName region)))
        request (-> (InvokeRequest.)
                    (.withFunctionName lambda-function-name)
                    (.withPayload (generate-string payload)))]
    (parse-result (.invoke client request))))

(defn- send-ok-messages [c buffer]
  (if-let [result (aq/peek-head buffer)]
    (do
      (go (>! c result))
      (send-ok-messages c (aq/remove-one buffer)))
    buffer))

(defn- handler-loop [region in-queue]
  (let [continue? (atom true)
        stop-fn #(reset! continue? false)
        ret (chan 10)]
    (go-loop [buffer (aq/create :index)]
             (when @continue?
               (recur (if-let [message (sqs/receive-message region in-queue 3)]
                        (send-ok-messages ret (aq/add-one buffer message))
                        buffer))))
    {:results ret
     :stop-fn stop-fn}))

(defn- uuid []
  (str "d-fold-" (java.util.UUID/randomUUID)))

(defn- queue-reduce [region f xs in-queue out-queue node-count]
  (let [{:keys [results stop-fn]} (handler-loop region out-queue)
        batches (map (fn [a b] [a b]) (partition-all 4096 xs) (range))]
    (thread
      (doseq [[batch index] batches]
        (sqs/send-message region
                          in-queue
                          {:type "process" :index index :payload batch})))
    (let [response (reduce (fn [acc _]
                             (f acc (:payload (<!! results))))
                           (f)
                           batches)]
      (dotimes [_ node-count]
        (sqs/send-message region in-queue {:type "stop"}))
      (stop-fn)
      response)))

(defn run-d-fold [f serialized-f xs function-namespace node-count lambda-function-name region]
  (let [out (sqs/create-queue region (uuid))
        in (sqs/create-queue region (uuid))]
    (dotimes [_ node-count]
      (thread (invoke-lambda {:region region
                              :function serialized-f
                              :function-namespace (str function-namespace)
                              :in in
                              :out out}
                             lambda-function-name region)))
    (let [response (queue-reduce region f xs in out node-count)]
      (Thread/sleep 200)
      (sqs/delete-queue region in)
      (sqs/delete-queue region out)
      response)))

(defmacro dfold [combinef reducef xs {:keys [reduce-nodes aws-region]}]
  `(run-d-fold ~combinef
               (pr-str (s/fn [ys#] (r/fold ~combinef ~reducef ys#)))
               ~xs
               *ns*
               ~reduce-nodes
               "d-fold"
               ~aws-region))
