(ns fitlog.main-btest
    (:require
     [cljs.test :refer-macros [deftest is testing]]))

(defn div [a b]
  (/ a b))

(deftest div-test
  (is (= 3 (div 6 2))))

(deftest div-test-2
  (is (= 40 (div 400 10))))
