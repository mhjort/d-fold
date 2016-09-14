(ns d-fold.lambda
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.core.async :refer [chan thread go go-loop >! <! <!!]]
            [clojure.java.io :as io]
            [d-fold.sqs :as sqs]
            [cheshire.core :refer [generate-stream parse-stream]]))

(defn message-loop [region f in-queue out-queue]
  (let [handler (fn [message]
                  (when (= "process" (:type message)
                           (let [result (f (:payload message))]
                             (sqs/send-message region out-queue {:index (:index message)
                                                                 :payload result}))))
                  (:type message))]
    (loop []
      (let [responses (sqs/receive-messages region in-queue 5 handler)]
        (when-not (some #(= "stop" %) responses)
          (recur))))))

(deflambdafn d-fold.LambdaFn
  [in out ctx]
  (load "/serializable/fn")
  (load "/clojure/core/reducers")
  (let [input (parse-stream (io/reader in) true)
        output (io/writer out)]
    (println "Running with function:" (:function input))
    (load (:function-namespace input))
    (message-loop (:region input)
                  (load-string (:function input))
                  (:in input)
                  (:out input))
    (println "Run successfully")
    (generate-stream {:success true} output)
    (.flush output)))
