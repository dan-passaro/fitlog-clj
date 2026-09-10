;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.ui.app-shell-test
  (:require
   [cljs.test :refer [deftest is]]
   [reagent.core :as r]
   ["@testing-library/react" :as rtl]
   ["@testing-library/dom" :refer [prettyDOM waitFor waitForElementToBeRemoved within]]
   ["@testing-library/user-event" :as user-event-mod]
   ["sinon" :as sinon]
   [tick.core :as t]
   [fitlog.data :as d]
   [fitlog.test-util :refer [deftest-async render set-exercises! set-workouts! setup-user-events use-fitlog-fixtures with-mock-date]]
   [fitlog.ui.app-shell :refer [app-shell]]
   [fitlog.ui.rest-timer :refer [start-rest-timer! timer-end]]))

(def clock (atom nil))

(use-fitlog-fixtures :each [{:before (fn [] (reset! clock (.useFakeTimers sinon)))
                             :after (fn [] (.restore @clock))}])

(deftest-async shows-rest-timer
  (let [c (render [app-shell])]
    ;; sanity check - no timer appears yet
    (is (nil? (.queryByText c "2:00")))

    (await (rtl/act (^:async fn []
                     (start-rest-timer! (t/of-minutes 2)))))
    (is (some? (.getByText c "2:00")))

    (await (rtl/act (^:async fn []
                     (.tick @clock 1000))))
    (is (some? (.getByText c "1:59")))

    (await (rtl/act (^:async fn []
                     (.tick @clock 3000))))
    (is (some? (.queryByText c "1:56")))))

(deftest-async removes-expired-rest-timer
  (let [c (render [app-shell])]
    (await (rtl/act (^:async fn []
                     (start-rest-timer! (t/of-seconds 2)))))
    (is (some? (.getByText c "0:02")))

    (await (rtl/act (^:async fn []
                     (.tick @clock 3000))))
    (let [timer-elt (.queryByText c #"\d?\d:\d\d")]
      (is (nil? timer-elt)
          (str "Timer unexpectedly found in DOM:\n"
               (prettyDOM (.-baseElement c)))))))
