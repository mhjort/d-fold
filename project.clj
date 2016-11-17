(defproject d-fold "0.1.0"
  :description "AWS Lambda powered distributed fold"
  :url "https://github.com/mhjort/d-fold"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.5.0"]
                 [mhjort/serializable-fn "1.2.1-alpha"]
                 [org.clojure/core.async "0.2.374"]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.50"]
                 [com.amazonaws/aws-java-sdk-sqs "1.10.50"]
                 [com.amazonaws/aws-java-sdk-core "1.10.50"]
                 [uswitch/lambada "0.1.2"]]
  :aot :all)

