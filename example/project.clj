(defproject example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [d-fold "0.1.0"]]
  :plugins [[lein-clj-lambda "0.8.2"]]
  :lambda  {"example" [{:handler "d-fold.LambdaFn" ;Do not change this
                        :memory-size 1536 ;Do not change this
                        :timeout 300 ;Do not change this
                        :function-name "d-fold" ;Do not change this
                        :region "eu-west-1"}]})
