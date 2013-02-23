(ns delegance.test.core
  (:use clojure.test
        delegance.core
        delegance.memory
        delegance.worker))

(def client
  {:queue (memory-queue)
   :state (memory-state)})

(deftest test-delegate
  (worker client)
  (let [x (delegate client (+ 1 1))]
    (is (= @x 2))))
