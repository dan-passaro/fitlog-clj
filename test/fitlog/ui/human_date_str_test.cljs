;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.ui.human-date-str-test
  (:require
   [cljs.test :refer [deftest is use-fixtures]]
   ["mockdate" :as MockDate]
   [fitlog.ui.lib :refer [human-date-str]]))

(use-fixtures :each
  {:before #(.set MockDate "2010-01-01T12:00:00Z")
   :after  #(.reset MockDate)})

(deftest shows-today-for-same-day-dates
  (is (= "Today"
         (human-date-str (js/Date.)))))

(deftest shows-weekday-for-dates-within-a-week
  (.set MockDate "2024-01-15T12:00:00Z")
  (is (= "Saturday"
         (human-date-str (js/Date. "2024-01-13T12:00:00Z")))))

(deftest shows-full-date-for-dates-over-a-week-old
  (.set MockDate "2024-01-15T12:00:00Z")
  (is (= "Tuesday, Jan 2"
         (human-date-str (js/Date. "2024-01-02T12:00:00Z")))))
