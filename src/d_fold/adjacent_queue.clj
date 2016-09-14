(ns d-fold.adjacent-queue
  (:import [java.util PriorityQueue]))

(defn create [key-fn]
  {:next-items []
   :next-index 0
   :key-fn key-fn
   :queue (PriorityQueue. 10 (comparator (fn [x y] (< (key-fn x) (key-fn y)))))})

(defn add-one [{:keys [key-fn queue] :as adjacent-queue} element]
  (.add queue element)
  (loop [{:keys [next-items next-index] :as result} adjacent-queue]
    (if (not (= next-index (key-fn (.peek queue))))
      result
      (do
        (let [e (.poll queue)]
          (recur {:next-items (sort-by :index (cons e next-items))
                  :next-index (inc next-index)
                  :key-fn key-fn
                  :queue queue}))))))

(defn add-all [{:keys [next-items next-index key-fn queue] :as adjacent-queue} elements]
  (if (empty? elements)
    adjacent-queue
    (add-all (add-one adjacent-queue (first elements)) (drop 1 elements))))

(defn remove-one [adjacent-queue]
  (update adjacent-queue :next-items (partial drop 1)))

(defn peek-head [{:keys [next-items]}]
  (first next-items))
