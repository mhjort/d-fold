(ns example.core
  (:require [d-fold.core :refer [dfold]]))

(defn d-sum [items]
  (dfold + ((map inc) +) items {:reduce-nodes 2 :aws-region "eu-west-1"}))

;(d-sum (range 1000))

;(dfold + ((map inc) +) (range 100000) {:reduce-nodes 2 :aws-region "eu-west-1"})
