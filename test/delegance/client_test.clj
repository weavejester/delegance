(ns delegance.client-test
  (:use clojure.test
        delegance.client
        delegance.memory
        delegance.worker))

(def client
  {:queue (memory-queue)
   :state (memory-state)})

(deftest test-delegate
  (dotimes [_ 10]
    (run-worker client))
  (testing "simple deref"
    (let [x (delegate client `(+ 1 1))]
      (is (= @x 2))))
  (testing "timeout values"
    (let [x (delegate client `(Thread/sleep 3000))]
      (is (= (deref x 1000 :timeout) :timeout))))
  (testing "timeout times"
    (let [x  (delegate client `(Thread/sleep 3000))
          t1 (System/currentTimeMillis)]
      (deref x 1500 :timeout)
      (let [t2 (System/currentTimeMillis)]
        (is (<= 1400 (- t2 t1) 1600)))))
  (testing "nil result"
    (let [x (delegate client nil)]
      (is (nil? (deref x 3000 :fail))))))
